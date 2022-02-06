package ru.pel.usbddc.service;

import org.junit.jupiter.api.Test;

import java.net.SocketException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class OSInfoCollectorTest {

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

        assertThat(setupapiDevLogList, hasItem(Path.of("C:\\WINDOWS\\inf\\setupapi.dev.log")));
        assertThat(setupapiDevLogList.size(), greaterThanOrEqualTo(1));

    }

    @Test
    void performance() throws SocketException, ExecutionException, InterruptedException, TimeoutException {
        new OSInfoCollector().getNetworkInterfaceList();
    }
}