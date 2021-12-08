package ru.pel.usbddc.utility;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.pel.usbddc.entity.USBDevice;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RegistryAnalizerTest {
    private static List<USBDevice> usbDevices;
    //Вариант 1
    String expectedPid = "312b";
    String expectedProductName = "Superior S102 Pro";
    String expectedVid = "125f";
    String expectedVendorName = "A-DATA Technology Co., Ltd.";
    String expectedSerial = "1492710242260098";
    String mountedDeviceKey = "\\??\\Volume{5405623b-31de-11e5-8295-54a0503930d0}";
    String expectedMountedDeviceValue = "_??_USBSTOR#Disk&Ven_ADATA&Prod_USB_Flash_Drive&Rev_0.00#1492710242260098&0#{53f56307-b6bf-11d0-94f2-00a0c91efb8b}";
    String expectedMountPoints2 = "{5405623b-31de-11e5-8295-54a0503930d0}";

    //Вариант 2
//    String expectedPid = "6387";
//    String expectedVid = "058f";
//    String expectedVendorName = "";
//    String expectedProductName = "";
//    String expectedSerial = "EFF732B1";
//    String mountedDeviceKey = "\\??\\Volume{92e41808-1e77-11e7-8268-d86126b14266}";
//    String expectedMountedDeviceValue = "\\??\\USBSTOR#CdRom&Ven_ASUS&Prod_Device_CD-ROM&Rev_0310#7&1f412d32&0&FCAZCY04R051&0#{53f5630d-b6bf-11d0-94f2-00a0c91efb8b}";
//    String expectedMountPoints2 = "";

    @BeforeAll
    static void beforeAll() {
        usbDevices = RegistryAnalizer.getUSBDevices();
    }

    @Test
    void getMountPoints2() {
        List<String> mountPoints2 = RegistryAnalizer.getMountPoints2();
        assertTrue(mountPoints2.stream().anyMatch(mp -> mp.contains(expectedMountPoints2)));
    }

    @Test
    @DisplayName("Сбор смонтированных устройств")
    void getMountedDevices() {
        String actual = RegistryAnalizer.getMountedDevices().get(mountedDeviceKey);
        assertEquals(expectedMountedDeviceValue, actual, "Возможно, тест запущен на другом АРМ?");
    }

    @Test
    @DisplayName("Хотя бы одно устройство находится?")
    void getUSBDevices() {
        assertTrue(usbDevices.stream().anyMatch(d -> d.getVid().equalsIgnoreCase(expectedVid)));
        assertTrue(usbDevices.stream().anyMatch(d -> d.getPid().equalsIgnoreCase(expectedPid)));
        assertTrue(usbDevices.stream().anyMatch(d -> d.getSerial().equalsIgnoreCase(expectedSerial)));
    }

    @Test
    @DisplayName("Тест заполнения vendorName и productName на основе VID/PID")
    void setVendorProductName() {
        assertNotEquals("", expectedVendorName);
        assertNotEquals("", expectedProductName);

        assertTrue(usbDevices.stream().anyMatch(d -> d.getVendorName().equalsIgnoreCase(expectedVendorName)));
        assertTrue(usbDevices.stream().anyMatch(d -> d.getProductName().equalsIgnoreCase(expectedProductName)));
    }

}