package ru.pel.usbddc.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.pel.usbddc.entity.USBDevice;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SetupapiDevLogAnalyzerTest {
    private static Map<String, USBDevice> usbDeviceMap;
    //Вариант 1
    private final String SERIAL = "1492710242260098";

    //Вариант 2
//    private final String SERIAL = "EFF732B1";
    private final LocalDateTime expectedDateTimeInstall = LocalDateTime.of(2021, 8, 7, 16, 27, 36);

    @BeforeAll
    static void beforeAll() {
//        usbDeviceMap = new RegistryAnalyzer().getRegistryAnalysis(true);
        usbDeviceMap = new HashMap<>();
    }

    @Test
    void getInstallDateTime() {
//        Optional<LocalDateTime> installDateTime = new SetupapiDevLogAnalyzer().getInstallDateTime(SERIAL);
    }

    @Test
    void parse() throws IOException {
        SetupapiDevLogAnalyzer setupapiDevLogAnalyzer = new SetupapiDevLogAnalyzer(usbDeviceMap);
        setupapiDevLogAnalyzer.parse();
        Map<String, USBDevice> usbDeviceMap = setupapiDevLogAnalyzer.getUsbDeviceMap();

        List<String> collect = usbDeviceMap.values().stream()
                .filter(v -> v.getDateTimeFirstInstall().equals(LocalDateTime.MIN))
                .map(USBDevice::getSerial)
                .collect(Collectors.toList());

        assertAll(
                () -> assertEquals(expectedDateTimeInstall, usbDeviceMap.get(SERIAL).getDateTimeFirstInstall()),
                () -> assertEquals(LocalDateTime.of(2021, 8, 26, 21, 17, 56),
                        usbDeviceMap.get("Alaska_Day_2006").getDateTimeFirstInstall(), "Alaska_Day_2006 НЕ совпало"),
                () -> assertNotEquals(LocalDateTime.of(2021, 9, 2, 22, 36, 8),
                        usbDeviceMap.get("Alaska_Day_2006").getDateTimeFirstInstall(), "Alaska_Day_2006 ДОЛЖНО совпасть")
        );
    }
}