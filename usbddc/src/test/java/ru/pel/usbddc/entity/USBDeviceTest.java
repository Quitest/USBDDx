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
        testUsbDevice = new USBDevice()
                .setSerial(SERIAL)
                .setVidPid(VID, PID);
    }

    @Test
    @DisplayName("Копирование не нулевых полей")
    void copyNonNullProperties() {
        USBDevice dst = new USBDevice()
                .setSerial("12345")
                .setGuid(null)
                .addVolumeLabel("oldVolumeName");
        USBDevice src = new USBDevice()
                .setSerial(null)
                .addVolumeLabel(null);
                src.setGuid("{1}");

        dst.mergeProperties(src);

        assertAll(                                                              //проверяем качество заполнения полей:
                () -> assertEquals("12345", dst.getSerial()),              //явный null и
                () -> assertEquals("{1}", dst.getGuid()),                  //неявный null переписывается значением,
//                () -> assertEquals("oldVolumeName", dst.getVolumeLabelList()),  //но значение НЕ переписывается null'ем.
                () -> dst.setGuid("{2}"),                                         //Изменение одного из объектов
                () -> assertEquals("{2}", dst.getGuid()),                  //не влияет
                () -> assertNotEquals("{2}", src.getGuid())             //на состояние другого.
        );
    }

    @Test
    @DisplayName("Создание объекта, передавая null-значения")
    void creatingUSBDeviceWithNullProperties() {
        assertDoesNotThrow(() -> new USBDevice()
                        .setUserAccountsList(null)
                        .setFriendlyName(null)
                        .setVidPid(null, null)
//                        .setGuid(null)
                        .setSerial(null)
                        .setDateTimeFirstInstall(null)
                        .setRevision(null)
                        .addVolumeLabel(null) //FIXME вероятно надо setVolumeLabel
                , "Исключения не должны возникать");

    }

    @Test
    @DisplayName("Определение названий по VID/PID")
    void determineVendorNameAndProductName() {
        assertAll("Определение имен производителя и продукта по vid/pid",
                () -> assertEquals(EXPECTED_VENDOR_NAME, testUsbDevice.getVendorName()),
                () -> assertEquals(EXPECTED_PRODUCT_NAME, testUsbDevice.getProductName()));
    }

    @Test
    @DisplayName("Проверка алгоритма определения типа серийного номера (сгенерирован ОС или нет)")
    void isSerialOSGenerated() {
        USBDevice OSGeneratedSerial1 = new USBDevice().setSerial("0&123456789");
        USBDevice OSGeneratedSerial2 = new USBDevice().setSerial("0&123456789&0");
        USBDevice vendorGeneratedSerial1 = new USBDevice().setSerial("0123456789");
        USBDevice vendorGeneratedSerial2 = new USBDevice().setSerial("0123456789&0");

        assertAll(
                () -> assertTrue(OSGeneratedSerial1.isSerialOSGenerated(), "Символ '&' ДОЛЖЕН стоять во второй позиции"),
                () -> assertTrue(OSGeneratedSerial2.isSerialOSGenerated(), "Символ '&' ДОЛЖЕН стоять во второй позиции"),
                () -> assertFalse(vendorGeneratedSerial1.isSerialOSGenerated(), "Символ '&' НЕ должен стоять во второй позиции"),
                () -> assertFalse(vendorGeneratedSerial2.isSerialOSGenerated(), "Символ '&' НЕ должен стоять во второй позиции")
        );
    }

    @Test
    @DisplayName("Проверка эквивалентности объектов")
    void testEquals() {
        USBDevice sameUsbDevice = testUsbDevice;
        USBDevice copyOfTestUsbDevice = new USBDevice()
                .setSerial(SERIAL)
                .setVidPid(VID, PID);
        USBDevice otherUsbDevice = new USBDevice()
                .setSerial("other" + SERIAL)
                .setVidPid(VID, PID);

        assertAll(
                () -> assertEquals(testUsbDevice, sameUsbDevice, "Переменные должны ссылаться на один объект"),
                () -> assertEquals(testUsbDevice, copyOfTestUsbDevice, "Переменные должны указывать на разные, но идентичные объекты"),
                () -> assertNotEquals(testUsbDevice, otherUsbDevice, "Переменные должны указывать на разные и не идентичные объекты"),
                () -> assertNotEquals("", testUsbDevice, "Должна происходить попытка сравнения объектов разного типа")
        );
    }
}