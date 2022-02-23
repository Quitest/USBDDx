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
    private static SystemInfo systemInfo;
    private static SystemInfoCollector systemInfoCollector;

    @BeforeAll
    static void init() throws IOException {
        systemInfoCollector = new SystemInfoCollector().collectSystemInfo();
        systemInfo = systemInfoCollector.getSystemInfo();
        systemInfo.setComment("test comment");
        json = systemInfoCollector.systemInfoToJSON();
    }

    @Test
    void whenJsonContainsComment_thenTrue(){
        assertThat(json, containsString("\"comment\" : \"test comment\""));
    }

    @Test
    void jsonContainsData() throws JsonProcessingException {
        SystemInfo systemInfo = new ObjectMapper().findAndRegisterModules().readValue(json, SystemInfo.class);
        double osVersion = systemInfo.getOsInfo().getOsVersion();

        assertThat(systemInfo.getUsbDeviceMap(), anyOf(hasKey("EFF732B1"), hasKey("1492710242260098")));
        assertThat(osVersion, anyOf(is(10.0), is(6.3)));
    }

    @Test
    void convertingSystemInfoToJSONContainsOsId() throws JsonProcessingException {
        String osId = systemInfo.getOsInfo().getOsId();
        assertThat(systemInfoCollector.systemInfoToJSON(), containsString(osId));
    }

    @Test
    void mappingToJSON_doesNotThrow() {
        assertDoesNotThrow(() -> new ObjectMapper().readTree(json));
    }

    @Test
    void whenGettingUUID_thenTrue() {
        //паттерн для UUID XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX, где X - символ один из A-Fa-f0-9
        String pattern = ".*[A-Fa-f\\d]{8}-([A-Fa-f\\d-]{5}){3}[A-Fa-f\\d]{12}.*";
        assertThat(systemInfo.getUuid(), matchesRegex(pattern));
    }

    //TODO подсчет количества запускаемых потоков. См. https://stackoverflow.com/questions/11642630/junit-test-the-correct-number-of-threads-has-started
}