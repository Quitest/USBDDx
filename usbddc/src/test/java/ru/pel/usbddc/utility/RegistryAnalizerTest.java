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
    @DisplayName("Хотя бы одно устройство находится?")
    void getUSBDevices() {
//        List<USBDevice> usbDevices = RegistryAnalizer.getUSBDevices();
        assertNotEquals(0, usbDevices.size(), "USB устройства не получены или вообще отсутствуют в системе");
    }

    @Test
    @DisplayName("Выборочный тест USBDevice.Builder")
    void getWorkFlash(){
        String expectedPid = "6387";
        String expectedVid = "058f";
        String expectedSerial = "EFF732B1";

        assertTrue(usbDevices.stream().anyMatch(d->d.getVid().equals(expectedVid)));
        assertTrue(usbDevices.stream().anyMatch(d->d.getPid().equals(expectedPid)));
        assertTrue(usbDevices.stream().anyMatch(d->d.getSerial().equals(expectedSerial)));
    }


}