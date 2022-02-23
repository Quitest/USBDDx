package ru.pel.usbddc.service;

import org.apache.tomcat.jni.OS;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsSame;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.pel.usbddc.entity.USBDevice;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
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
        Map<String, USBDevice> analysis = new SetupapiDevLogAnalyzer(new OSInfoCollector().getSetupapiDevLogList(),true)
                .getAnalysis();

        assertTrue(analysis.size() > 0);
    }

    @Test
    @DisplayName("Когда doNewAnalysis = false возвращается прежний результат")
    void whenDoNewAnalysisIsFALSEGetAnalysisReturnOLDResult() throws IOException {
        SetupapiDevLogAnalyzer setupapiDevLogAnalyzer = new SetupapiDevLogAnalyzer(new OSInfoCollector().getSetupapiDevLogList(),
                true);
        Map<String, USBDevice> newAnalysis = setupapiDevLogAnalyzer.getAnalysis();
        setupapiDevLogAnalyzer.setDoNewAnalysis(false);
        Map<String, USBDevice> prevAnalysis = setupapiDevLogAnalyzer.getAnalysis();

        assertSame(newAnalysis, prevAnalysis);
    }

    @Test
    void whenDoNewAnalysisIsTRUEGetAnalysisReturnNEWResult() throws IOException {
        SetupapiDevLogAnalyzer setupapiDevLogAnalyzer = new SetupapiDevLogAnalyzer(new OSInfoCollector().getSetupapiDevLogList(),
                true);
        Map<String, USBDevice> newAnalysis = setupapiDevLogAnalyzer.getAnalysis();
        setupapiDevLogAnalyzer.setDoNewAnalysis(true);
        Map<String, USBDevice> prevAnalysis = setupapiDevLogAnalyzer.getAnalysis();

        assertThat(newAnalysis, not(sameInstance(prevAnalysis)));
    }

    @Test
    void whenDateTimeFirstInstallNotMIN_thenTrue() throws IOException {
        Map<String, USBDevice> usbDeviceMap = new SetupapiDevLogAnalyzer(new OSInfoCollector().getSetupapiDevLogList(),true)
                .parseAllSetupapiDevLogs();

        assertThat(usbDeviceMap, hasEntry(
                anyOf(is(SERIAL)),
                hasProperty("dateTimeFirstInstall", is(not(LocalDateTime.MIN)))));
    }

    @Test
    void parseDateTimeFirstInstallOfSerial() throws IOException {
        Map<String, USBDevice> usbDeviceMap = new SetupapiDevLogAnalyzer(new OSInfoCollector().getSetupapiDevLogList(),true)
                .parseAllSetupapiDevLogs();

        assertEquals(LocalDateTime.of(2016,11,28,11,3,24),
                usbDeviceMap.get("00000").getDateTimeFirstInstall());
    }
    //строка в которой не распознается серийный номер 00000
    //>>>  [Device Install (Hardware initiated) - SWD\WPDBUSENUM\_??_USBSTOR#Disk&Ven_Generic-&Prod_xD#SD#M.S.&Rev_1.00#00000#{53f56307-b6bf-11d0-94f2-00a0c91efb8b}]
    //>>>  Section start 2016/11/28 11:03:24.136
}