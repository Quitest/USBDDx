package ru.pel.usbddc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SystemInfoCollectorTest {

    @Test
    void garbage() {
//        new SystemInfoCollector().collectSystemInfo();
    }

    @Test
    void toJSON() throws IOException {
        String json = new SystemInfoCollector().collectSystemInfo().toJSON();

        assertDoesNotThrow(() -> new ObjectMapper().readTree(json));
    }
}