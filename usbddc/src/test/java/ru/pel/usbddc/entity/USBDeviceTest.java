package ru.pel.usbddc.entity;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.pel.usbddc.utility.RegistryAnalizer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class USBDeviceTest {
    private static List<USBDevice> usbDeviceList;
    private final String SERIAL = "09FTZ683GCLP0HVS";
    private final String EXPECTED_PRODUCT_NAME = "JetFlash";
    private final String EXPECTED_VENDOR_NAME = "Transcend Information, Inc.";

    @BeforeAll
    static void beforeAll() {
        usbDeviceList = RegistryAnalizer.getUSBDevices();
    }

    @Test
    void determineProductName() {
        USBDevice device = usbDeviceList.stream()
                .filter(d -> d.getSerial().equals(SERIAL))
                .findAny().orElseThrow();
        assertEquals(EXPECTED_PRODUCT_NAME, device.getProductName(), "Возможно, тест запущен на другом ПК? Проверьте константы.");
    }

    @Test
    void determineVendorName() {
        USBDevice device = usbDeviceList.stream()
                .filter(d -> d.getSerial().equals(SERIAL))
                .findAny().orElseThrow();
        assertEquals(EXPECTED_VENDOR_NAME, device.getVendorName(), "Возможно, тест запущен на другом ПК? Проверьте константы.");
    }
}