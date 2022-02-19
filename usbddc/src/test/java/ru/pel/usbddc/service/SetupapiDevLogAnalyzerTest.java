package ru.pel.usbddc.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.pel.usbddc.entity.USBDevice;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SetupapiDevLogAnalyzerTest {
    //Вариант 1
    private final String SERIAL = "1492710242260098";
    private final LocalDateTime expectedDateTimeInstall = LocalDateTime.of(2021, 8, 7, 16, 27, 36);

    //Вариант 2
//    private final String SERIAL = "EFF732B1";
//    private final LocalDateTime expectedDateTimeInstall = LocalDateTime.of(2016, 11, 24, 9, 22, 15);

    @Test
    @DisplayName("Выполнение нового анализа")
    void getNewAnalysis() throws IOException {
        Map<String, USBDevice> analysis = new SetupapiDevLogAnalyzer().getAnalysis(true);

        assertTrue(analysis.size() > 0);
    }

    @Test
    @DisplayName("Получение ранее выполненного анализа")
    void getPrevAnalysis() throws IOException {
        SetupapiDevLogAnalyzer setupapiDevLogAnalyzer = new SetupapiDevLogAnalyzer();
        Map<String, USBDevice> newAnalysis = setupapiDevLogAnalyzer.getAnalysis(true);
        Map<String, USBDevice> prevAnalysis = setupapiDevLogAnalyzer.getAnalysis(false);

        assertSame(newAnalysis, prevAnalysis);
    }

    @Test
    void parse() throws IOException {
//        SetupapiDevLogAnalyzer setupapiDevLogAnalyzer = new SetupapiDevLogAnalyzer();
//        setupapiDevLogAnalyzer.parseAllSetupapiDevLogs();
//        Map<String, USBDevice> usbDeviceMap = setupapiDevLogAnalyzer.getUsbDeviceMap();
        Map<String, USBDevice> usbDeviceMap = new SetupapiDevLogAnalyzer().parseAllSetupapiDevLogs();

        assertAll(
                () -> assertEquals(expectedDateTimeInstall, usbDeviceMap.get(SERIAL).getDateTimeFirstInstall())
//                () -> assertEquals(LocalDateTime.of(2020,4,13, 14,49,24),
//                        usbDeviceMap.get("00000000000E07").getDateTimeFirstInstall())
        );
    }

    @Test
    void parseDateTimeFirstInstallOfSerial() throws IOException {
        Map<String, USBDevice> usbDeviceMap = new SetupapiDevLogAnalyzer().parseAllSetupapiDevLogs();

        assertEquals(LocalDateTime.of(2016,11,28,11,3,24),
                usbDeviceMap.get("00000").getDateTimeFirstInstall());
    }
    //строка в которой не распознается серийный номер 00000
    //>>>  [Device Install (Hardware initiated) - SWD\WPDBUSENUM\_??_USBSTOR#Disk&Ven_Generic-&Prod_xD#SD#M.S.&Rev_1.00#00000#{53f56307-b6bf-11d0-94f2-00a0c91efb8b}]
    //>>>  Section start 2016/11/28 11:03:24.136
}