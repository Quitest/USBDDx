package ru.pel.usbddc.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.pel.usbddc.entity.OSInfo;

import java.net.SocketException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class OSInfoCollectorTest {
    private static OSInfo osInfo;

    @BeforeAll
    static void init() {
        osInfo = new OSInfoCollector().collectInfo();
    }

    @Test
    void getOsId() {
        //паттерн для UUID XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX, где X - символ один из A-Fa-f0-9
        String pattern = ".*[A-Fa-f\\d]{8}-([A-Fa-f\\d-]{5}){3}[A-Fa-f\\d]{12}.*";
        assertThat(osInfo.getOsId(), matchesRegex(pattern));
    }

    @Test
    void getPathToSetupapiDevLog() {
        String pathToSetupapiDevLog = new OSInfoCollector().getPathToSetupapiDevLog().toString();
        assertThat(pathToSetupapiDevLog, anyOf(
                equalToIgnoringCase("C:\\WINDOWS\\inf"),
                equalToIgnoringCase("C:\\WINDOWS")));
    }

    @Test
    void getSetupapiDevLogList() {
        List<Path> setupapiDevLogList = new OSInfoCollector().getSetupapiDevLogList();

        assertThat(setupapiDevLogList, anyOf(
                hasItem((Path.of("C:\\WINDOWS\\inf\\setupapi.dev.log"))),
                hasItem(Path.of("C:\\WINDOWS\\setupapi.dev.log"))));

    }
}