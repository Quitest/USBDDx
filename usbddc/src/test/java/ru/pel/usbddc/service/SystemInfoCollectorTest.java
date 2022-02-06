package ru.pel.usbddc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.pel.usbddc.entity.SystemInfo;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SystemInfoCollectorTest {
    private static String json;

    @BeforeAll
    static void init() throws IOException {
        SystemInfoCollector systemInfoCollector = new SystemInfoCollector().collectSystemInfo();
        json = systemInfoCollector.systemInfoToJSON();
    }

    @Test
    void analysisTime() {
    }

    @Test
    void jsonContainsData() throws JsonProcessingException {
        SystemInfo systemInfo = new ObjectMapper().findAndRegisterModules().readValue(json, SystemInfo.class);
        double osVersion = systemInfo.getOsInfo().getOsVersion();
        assertThat(systemInfo.getUsbDeviceMap(), anyOf(hasKey("EFF732B1"), hasKey("1492710242260098")));
        assertThat(osVersion, is(10.0));
    }

    @Test
    void toJSON() {
        assertDoesNotThrow(() -> new ObjectMapper().readTree(json));
    }
}