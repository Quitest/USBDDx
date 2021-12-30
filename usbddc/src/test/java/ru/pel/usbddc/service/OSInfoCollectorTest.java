package ru.pel.usbddc.service;

import org.junit.jupiter.api.Test;

import java.net.SocketException;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class OSInfoCollectorTest {

    @Test
    void getAllInfo() throws SocketException {
        new OSInfoCollector().getNetworkInterfaceList();
    }

    @Test
    void getPathToSetupapiDevLog() {
    }

    @Test
    void getSetupapiDevLogList() {
        List<Path> setupapiDevLogList = new OSInfoCollector().getSetupapiDevLogList();

        assertThat(setupapiDevLogList, anyOf(
                containsInAnyOrder(
                        Path.of("C:\\WINDOWS\\inf\\setupapi.dev.20200401_103934.log"),
                        Path.of("C:\\WINDOWS\\inf\\setupapi.dev.log")),
                contains(
                        Path.of("C:\\WINDOWS\\inf\\setupapi.dev.log")
                )));

    }
}