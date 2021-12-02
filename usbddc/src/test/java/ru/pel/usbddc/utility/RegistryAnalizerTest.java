package ru.pel.usbddc.utility;

import org.junit.jupiter.api.BeforeAll;
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
    void getUSBDevices() {
//        List<USBDevice> usbDevices = RegistryAnalizer.getUSBDevices();
        assertNotEquals(0, usbDevices.size(), "USB устройства не получены или вообще отсутствуют в системе");
    }


}