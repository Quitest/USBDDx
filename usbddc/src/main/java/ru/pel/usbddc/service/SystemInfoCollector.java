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

    static {
        //TODO Вероятно, в данном случае размер пула потоков стоило бы определять исходя из количества запускаемых анализаторов?
        THREAD_POOL_SIZE = UsbddcConfig.getInstance().getThreadPoolSize();
        LOGGER.debug("Размер пула потоков = {}", THREAD_POOL_SIZE);
    }

    private SystemInfo systemInfo;

    public SystemInfoCollector() {
        systemInfo = new SystemInfo();
    }

    /**
     * Собирает всю необходимую информацию о системе, анализируя все доступные источники (логи, реестр, журналы и т.д.)
     *
     * @return текущий объект, наполненный информацией об устройствах и ОС.
     */
    public SystemInfoCollector collectSystemInfo() {
        long startTime = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        Future<OSInfo> osInfoFuture = executorService.submit(() -> new OSInfoCollector().collectInfo());
        Future<String> uuidFuture = executorService.submit(this::getSystemUUID);

        List<Callable<Map<String, USBDevice>>> taskList = new ArrayList<>();
        Callable<Map<String, USBDevice>> registryAnalysisCallable = () -> new RegistryAnalyzer().getAnalysis(true);
        List<Path> logList = new OSInfoCollector().getSetupapiDevLogList();
        Callable<Map<String, USBDevice>> logAnalysisCallable = () -> new SetupapiDevLogAnalyzer(logList).getAnalysis(true);

        taskList.add(logAnalysisCallable);
        taskList.add(registryAnalysisCallable);
        try {
            List<Future<Map<String, USBDevice>>> futures = executorService.invokeAll(taskList);
            for (Future<Map<String, USBDevice>> future : futures) {
                Map<String, USBDevice> usbDeviceMap = future.get();
                systemInfo.mergeUsbDeviceInfo(usbDeviceMap);
            }
            systemInfo.setUuid(uuidFuture.get());
            systemInfo.setOsInfo(osInfoFuture.get(30, TimeUnit.SECONDS));

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.error("Exception occurred: {}", e.getLocalizedMessage());
            LOGGER.debug("Exception occurred: {}", e.toString());
            Thread.currentThread().interrupt();
        }

        executorService.shutdown();
        LOGGER.trace("Время работы общее - {}мс", System.currentTimeMillis() - startTime);
        return this;
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
            LOGGER.error("ОШИБКА при попытке получить UUID. Присвоено значение \"{}\". {}",error, e.getLocalizedMessage());
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
