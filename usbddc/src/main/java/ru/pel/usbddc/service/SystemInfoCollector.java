package ru.pel.usbddc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pel.usbddc.entity.OSInfo;
import ru.pel.usbddc.entity.SystemInfo;
import ru.pel.usbddc.entity.USBDevice;

import java.io.IOException;
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
    private static final Logger logger = LoggerFactory.getLogger(SystemInfoCollector.class);
    private SystemInfo systemInfo;

    public SystemInfoCollector() {
        systemInfo = new SystemInfo();
    }

    /**
     * Собирает всю необходимую информацию о системе, анализируя все доступные источники (логи, реестр, журналы и т.д.)
     * @return текущий объект, наполненный информацией об устройствах и ОС.
     * @throws IOException
     */
    public SystemInfoCollector collectSystemInfo() throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        long startTime = System.currentTimeMillis();
        Future<OSInfo> osInfoFuture = executorService.submit(()-> new OSInfoCollector().collectInfo());
        logger.trace("Время работы OsInfoCollector - {}мс", System.currentTimeMillis()-startTime);

        List<Callable<Map<String,USBDevice>>> taskList = new ArrayList<>();
        long startTimeRegAnalysis = System.currentTimeMillis();
        Callable<Map<String,USBDevice>> registryAnalysisCallable = ()->new RegistryAnalyzer().getAnalysis(true);
        logger.trace("Время работы RegistryAnalyze() - {}мс", System.currentTimeMillis()-startTimeRegAnalysis);

        List<Path> logList = new OSInfoCollector().getSetupapiDevLogList();
        long startTimeLogAnalysis = System.currentTimeMillis();

        Callable<Map<String ,USBDevice>> logAnalysisCallable = ()->new SetupapiDevLogAnalyzer(logList).getAnalysis(true);
        logger.trace("Время работы SetupapiDevLogAnalyzer() - {}мс", System.currentTimeMillis()-startTimeLogAnalysis);

        taskList.add(logAnalysisCallable);
        taskList.add(registryAnalysisCallable);
        try {
            List<Future<Map<String, USBDevice>>> futures = executorService.invokeAll(taskList);
            for (Future<Map<String, USBDevice>> future : futures) {
                Map<String, USBDevice> usbDeviceMap = future.get();
                systemInfo.mergeUsbDeviceInfo(usbDeviceMap);
            }
            systemInfo.setOsInfo(osInfoFuture.get(30,TimeUnit.SECONDS));

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Exception occurred: {}", e.getLocalizedMessage());
            logger.debug("Exception occurred: ", e);

        }

        logger.trace("Время работы общее - {}мс", System.currentTimeMillis()-startTime);
        executorService.shutdown();
        return this;
    }

    /**
     * Конвертирует SystemInfo в JSON представление.
     * @return строку формата JSON, содержащую собранные данные
     * @throws JsonProcessingException
     */
    public String systemInfoToJSON() throws JsonProcessingException {
        ObjectWriter ow = new ObjectMapper().findAndRegisterModules().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(systemInfo);
    }
}
