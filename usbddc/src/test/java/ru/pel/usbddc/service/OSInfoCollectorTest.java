package ru.pel.usbddc.service;

import org.junit.jupiter.api.Test;

import java.net.SocketException;

class OSInfoCollectorTest {

    @Test
    void getSetupapiDevLogList() {
    }

    @Test
    void getPathToSetupapiDevLog() {
    }

    @Test
    void getAllInfo() throws SocketException {
        new OSInfoCollector().getNetworkInterfaceList();
    }
}