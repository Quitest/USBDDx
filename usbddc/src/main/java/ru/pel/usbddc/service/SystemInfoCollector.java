package ru.pel.usbddc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pel.usbddc.config.UsbddcConfig;
import ru.pel.usbddc.entity.OSInfo;
import ru.pel.usbddc.entity.SystemInfo;
import ru.pel.usbddc.entity.USBDevice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Сборщик всей необходимой и доступной информации о системе - идентификационные данные системы, подключаемые
 * устройства и т.д.
 */
@Getter
@Setter
public class SystemInfoCollector {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemInfoCollector.class);
    private static final int THREAD_POOL_SIZE;
    private static final ExecutorService executorService;

    static {
        //TODO Вероятно, в данном случае размер пула потоков стоило бы определять исходя из количества запускаемых анализаторов?
        THREAD_POOL_SIZE = UsbddcConfig.getInstance().getThreadPoolSize();
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        LOGGER.debug("Размер пула потоков = {}", THREAD_POOL_SIZE);
    }

    private List<Callable<Map<String, USBDevice>>> analyzerTaskList = new ArrayList<>();
    private SystemInfo systemInfo;
    //TODO #52
    // private String scannedWithAdminPrivileges;

    public SystemInfoCollector() {
        systemInfo = new SystemInfo();
    }

    private int addAnalyzer(Analyzer analyzer) {
        Callable<Map<String, USBDevice>> task = analyzer::getAnalysis;
        analyzerTaskList.add(task);
        return analyzerTaskList.size();
    }

    /**
     * Собирает всю необходимую информацию о системе, анализируя все доступные источники (логи, реестр, журналы и т.д.)
     *
     * @return текущий объект, наполненный информацией об устройствах и ОС.
     */
    public SystemInfoCollector collectSystemInfo() {
        long startTime = System.currentTimeMillis();

        Future<OSInfo> osInfoFuture = executorService.submit(() -> new OSInfoCollector().collectInfo());
        Future<String> uuidFuture = executorService.submit(this::getSystemUUID);

        List<Path> logList = new OSInfoCollector().getSetupapiDevLogList();

        addAnalyzer(new RegistryAnalyzer(true));
        addAnalyzer(new SetupapiDevLogAnalyzer(logList, true));
        try {
            executeAnalysis();
            systemInfo.setUuid(uuidFuture.get());
            systemInfo.setOsInfo(osInfoFuture.get(30, TimeUnit.SECONDS));

        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Текущий поток был прерван во время работы анализатора. Причина: {}", e.getLocalizedMessage());
            LOGGER.debug("Текущий поток был прерван во время работы анализатора.", e);
            Thread.currentThread().interrupt();
        } catch (TimeoutException e) {
            LOGGER.error("Анализатор работал слишком долго. {}", e.getLocalizedMessage());
            LOGGER.debug("Анализатор работал слишком долго. ", e);
            Thread.currentThread().interrupt();
        }

        executorService.shutdown();
        LOGGER.trace("Время работы общее - {}мс", System.currentTimeMillis() - startTime);
        return this;
    }

    private void executeAnalysis() {
        List<Future<Map<String, USBDevice>>> futures;
        try {
            futures = executorService.invokeAll(analyzerTaskList);
            for (Future<Map<String, USBDevice>> future : futures) {
                Map<String, USBDevice> usbDeviceMap = future.get();
                systemInfo.mergeUsbDeviceInfo(usbDeviceMap);
            }
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Текущий поток был прерван во время работы анализатора. Причина: {}", e.getLocalizedMessage());
            LOGGER.debug("Текущий поток был прерван во время работы анализатора.", e);
            Thread.currentThread().interrupt();
        }
    }

    private String getSystemUUID() {
        try {
            Process process = Runtime.getRuntime().exec("wmic csproduct get UUID");
            BufferedReader sNumReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = sNumReader.readLine()) != null) {
                output.append(line).append("\n");
            }
            String uuid = output.substring(output.indexOf("\n"), output.length()).trim();
            LOGGER.debug("UUID системы успешно получен [{}]", uuid);
            return uuid;
        } catch (IOException e) {
            String error = "error";
            LOGGER.error("ОШИБКА при попытке получить UUID. Присвоено значение \"{}\". {}", error, e.getLocalizedMessage());
            return error;
        }
    }

    /**
     * Конвертирует SystemInfo в JSON представление.
     *
     * @return строку формата JSON, содержащую собранные данные
     * @throws JsonProcessingException Из документации: Intermediate base class for all problems encountered when
     *                                 processing (parsing, generating) JSON content that are not pure I/O problems. Regular IOExceptions will be passed
     *                                 through as is. Sub-class of IOException for convenience.
     */
    public String systemInfoToJSON() throws JsonProcessingException {
        ObjectWriter ow = new ObjectMapper().findAndRegisterModules().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(systemInfo);
    }
}
