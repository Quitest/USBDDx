package ru.pel.usbddc.entity;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void isSerialOSGenerated() {
        USBDevice OSGeneratedSerial1 = USBDevice.getBuilder().withSerial("0&123456789").build();
        USBDevice OSGeneratedSerial2 = USBDevice.getBuilder().withSerial("0&123456789&0").build();
        USBDevice vendorGeneratedSerial1 = USBDevice.getBuilder().withSerial("0123456789").build();
        USBDevice vendorGeneratedSerial2 = USBDevice.getBuilder().withSerial("0123456789&0").build();

        assertAll(
                ()->assertTrue(OSGeneratedSerial1.isSerialOSGenerated(),"Символ '&' ДОЛЖЕН стоять во второй позиции"),
                ()->assertTrue(OSGeneratedSerial2.isSerialOSGenerated(),"Символ '&' ДОЛЖЕН стоять во второй позиции"),
                ()->assertFalse(vendorGeneratedSerial1.isSerialOSGenerated(),"Символ '&' НЕ должен стоять во второй позиции"),
                ()->assertFalse(vendorGeneratedSerial2.isSerialOSGenerated(),"Символ '&' НЕ должен стоять во второй позиции")
        );
    }

    @Test
    void testEquals() {
        USBDevice sameUsbDevice = testUsbDevice;
        USBDevice copyOfTestUsbDevice = USBDevice.getBuilder()
                .withSerial(SERIAL)
                .withVidPid(VID,PID).build();
        USBDevice otherUsbDevice = USBDevice.getBuilder()
                .withSerial("other" + SERIAL)
                .withVidPid(VID,PID).build();

        assertAll(
                () -> assertEquals(testUsbDevice, sameUsbDevice, "Переменные должны ссылаться на один объект"),
                ()-> assertEquals(testUsbDevice, copyOfTestUsbDevice, "Переменные должны указывать на разные, но идентичные объекты"),
                () -> assertNotEquals(testUsbDevice, otherUsbDevice, "Переменные должны указывать на разные и не идентичные объекты"),
                ()-> assertNotEquals("", testUsbDevice, "Должна происходить попытка сравнения объектов разного типа")
        );
    }
}