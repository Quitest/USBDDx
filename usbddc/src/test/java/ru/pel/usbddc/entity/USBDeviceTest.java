package ru.pel.usbddc.entity;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.pel.usbddc.utility.RegistryAnalyzer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class USBDeviceTest {
    private static List<USBDevice> usbDeviceList;
    private final String testFailedMsg = "Возможно, тест запущен на другом ПК? Проверьте константы.";

    //Вариант 1
    private final String SERIAL = "1492710242260098";
    private final String EXPECTED_PRODUCT_NAME = "Superior S102 Pro";
    private final String EXPECTED_VENDOR_NAME = "A-DATA Technology Co., Ltd.";

    //Вариант 2
//    private final String SERIAL = "09FTZ683GCLP0HVS";
//    private final String EXPECTED_PRODUCT_NAME = "JetFlash";
//    private final String EXPECTED_VENDOR_NAME = "Transcend Information, Inc.";

    @BeforeAll
    static void beforeAll() {
        usbDeviceList = new RegistryAnalyzer().getUsbDeviceList();
    }

    @Test
    void determineProductName() {
        USBDevice device = usbDeviceList.stream()
                .filter(d -> d.getSerial().equals(SERIAL))
                .findAny().orElse(USBDevice.getBuilder().build());
        assertEquals(EXPECTED_PRODUCT_NAME, device.getProductName(), testFailedMsg);
    }

    @Test
    void determineVendorName() {
        USBDevice device = usbDeviceList.stream()
                .filter(d -> d.getSerial().equals(SERIAL))
                .findAny().orElse(USBDevice.getBuilder().build());
        assertEquals(EXPECTED_VENDOR_NAME, device.getVendorName(), testFailedMsg);
    }
}