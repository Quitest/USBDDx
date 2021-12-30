package ru.pel.usbddc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.Getter;
import lombok.Setter;
import ru.pel.usbddc.entity.OSInfo;
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
    private Map<String, USBDevice> usbDeviceMap;
    private OSInfo osInfo;

    public SystemInfoCollector collectSystemInfo() throws IOException {
        osInfo = new OSInfoCollector().collectInfo();
        usbDeviceMap = new RegistryAnalyzer().getRegistryAnalysis(true);
        List<Path> logList = new OSInfoCollector().getSetupapiDevLogList();
        usbDeviceMap = new SetupapiDevLogAnalyzer(usbDeviceMap, logList)
                .getAnalysis(true);

        return this;
    }

    public String toJSON() throws JsonProcessingException {
        ObjectWriter ow = new ObjectMapper().findAndRegisterModules().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(this);
    }
}
