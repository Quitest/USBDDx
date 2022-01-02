package ru.pel.usbddc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.Getter;
import lombok.Setter;
import ru.pel.usbddc.entity.SystemInfo;
import ru.pel.usbddc.entity.USBDevice;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Сборщик всей необходимой и доступной информации о системе - идентификационные данные системы, подключаемые
 * устройства и т.д.
 */
@Getter
@Setter
public class SystemInfoCollector {
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
        systemInfo.setOsInfo(new OSInfoCollector().collectInfo());

        Map<String, USBDevice> registryAnalysis = new RegistryAnalyzer().getRegistryAnalysis(true);

        List<Path> logList = new OSInfoCollector().getSetupapiDevLogList();
        Map<String, USBDevice> logAnalysis = new SetupapiDevLogAnalyzer(logList).getAnalysis(true);

        systemInfo
                .mergeUsbDeviceInfo(registryAnalysis)
                .mergeUsbDeviceInfo(logAnalysis);

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
