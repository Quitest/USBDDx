package ru.pel.usbddc.utility;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.pel.usbddc.entity.USBDevice;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RegistryAnalizerTest {
    private static List<USBDevice> usbDevices;

    @BeforeAll
    static void beforeAll() {
        usbDevices = RegistryAnalizer.getUSBDevices();
    }

    @Test
    @DisplayName("Выборочный тест домашнего USBDevice.Builder")
    void getHomeFlash() {
        String expectedPid = "312b";
        String expectedVid = "125f";
        String expectedSerial = "1492710242260098";

        assertTrue(usbDevices.stream().anyMatch(d -> d.getVid().equalsIgnoreCase(expectedVid)));
        assertTrue(usbDevices.stream().anyMatch(d -> d.getPid().equalsIgnoreCase(expectedPid)));
        assertTrue(usbDevices.stream().anyMatch(d -> d.getSerial().equalsIgnoreCase(expectedSerial)));
    }

    @Test
    @DisplayName("Сбор смонтированных устройств")
    void getMountedDevices() {
//        String key = "\\??\\Volume{92e41808-1e77-11e7-8268-d86126b14266}";
//        String expected = "\\??\\USBSTOR#CdRom&Ven_ASUS&Prod_Device_CD-ROM&Rev_0310#7&1f412d32&0&FCAZCY04R051&0#{53f5630d-b6bf-11d0-94f2-00a0c91efb8b}";
        String key = "\\??\\Volume{5405623b-31de-11e5-8295-54a0503930d0}";
        String expected = "_??_USBSTOR#Disk&Ven_ADATA&Prod_USB_Flash_Drive&Rev_0.00#1492710242260098&0#{53f56307-b6bf-11d0-94f2-00a0c91efb8b}";

        String actual = RegistryAnalizer.getMountedDevices().get(key);

        assertEquals(expected, actual, "Возможно, тест запущен на другом АРМ?");
    }

    @Test
    @DisplayName("Хотя бы одно устройство находится?")
    void getUSBDevices() {
//        List<USBDevice> usbDevices = RegistryAnalizer.getUSBDevices();
        assertNotEquals(0, usbDevices.size(), "USB устройства не получены или вообще отсутствуют в системе");
    }

    @Test
    @DisplayName("Выборочный тест рабочего USBDevice.Builder")
    void getWorkFlash() {
        String expectedPid = "6387";
        String expectedVid = "058f";
        String expectedSerial = "EFF732B1";

        assertTrue(usbDevices.stream().anyMatch(d -> d.getVid().equalsIgnoreCase(expectedVid)));
        assertTrue(usbDevices.stream().anyMatch(d -> d.getPid().equalsIgnoreCase(expectedPid)));
        assertTrue(usbDevices.stream().anyMatch(d -> d.getSerial().equalsIgnoreCase(expectedSerial)));
    }

}