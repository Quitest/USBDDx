package ru.pel.usbddc.service;

import lombok.Getter;
import lombok.Setter;
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
    private Map<String, String> systemEnvironment;
    private Map<String, USBDevice> usbDeviceMap;

    public void collectSystemInfo() throws IOException {
        systemEnvironment = System.getenv();
        usbDeviceMap = new RegistryAnalyzer().getRegistryAnalysis(true);
        List<Path> logList = new OSInfoCollector().getSetupapiDevLogList();
        usbDeviceMap = new SetupapiDevLogAnalyzer(usbDeviceMap, logList)
                .getAnalysis(true);
    }
}
