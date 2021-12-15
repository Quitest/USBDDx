package ru.pel.usbddc.entity;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class USBDeviceTest {
    private final static String SERIAL = "EFF732B1";
    private final static String VID = "058f";
    private final static String PID = "6387";
    private final static String EXPECTED_PRODUCT_NAME = "Flash Drive";
    private final static String EXPECTED_VENDOR_NAME = "Alcor Micro Corp.";
    private static USBDevice testUsbDevice;

    @BeforeAll
    static void beforeAll() {
        testUsbDevice = USBDevice.getBuilder()
                .withSerial(SERIAL)
                .withVidPid(VID, PID).build();
    }

    @Test
    @DisplayName("Определение названий по VID/PID")
    void determineVendorNameAndProductName() {
        assertAll("Определение имен производителя и продукта по vid/pid",
                () -> assertEquals(EXPECTED_VENDOR_NAME, testUsbDevice.getVendorName()),
                () -> assertEquals(EXPECTED_PRODUCT_NAME, testUsbDevice.getProductName()));
    }
}