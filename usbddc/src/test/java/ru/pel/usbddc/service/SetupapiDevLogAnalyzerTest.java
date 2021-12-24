package ru.pel.usbddc.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SetupapiDevLogAnalyzerTest {
    private final String SERIAL = "EFF732B1";

    @BeforeAll
    static void beforeAll() {
        Path pathToSetupapiDevLog = new OSInfoCollector().getPathToSetupapiDevLog();
//        SetupapiDevLogAnalyzer.setPathToLog(pathToSetupapiDevLog);
        SetupapiDevLogAnalyzer.setPathToLog(Path.of("C:\\Windows\\Inf\\setupapi.dev.20200401_103934.log"));
    }

    @Test
    void getInstallDateTime() {
        Optional<LocalDateTime> installDateTime = new SetupapiDevLogAnalyzer().getInstallDateTime(SERIAL);
    }
}