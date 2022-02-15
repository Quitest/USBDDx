package ru.pel.usbddc.entity;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SystemInfoTest {
    private static Map<String, USBDevice> map1;
    private static Map<String, USBDevice> map2;
    private static final String SERIAL = "1492710242260098";
    private static final LocalDateTime INSTALL_DATE = LocalDateTime.now();

    @BeforeAll
    static void init() {
        USBDevice device1 = USBDevice.getBuilder()
                .withDateTimeFirstInstall(LocalDateTime.MIN)
                .withFriendlyName("ADATA USB Flash Drive USB Device")
                .withGuid("{5405623b-31de-11e5-8295-54a0503930d0}")
                .withVidPid("125f", "312b")
                .withRevision("0.00")
                .withSerial(SERIAL)
                .addVolumeLabel("My flash").build();
        map1 = new HashMap<>();
        map1.put(SERIAL, device1);

        USBDevice device2 = USBDevice.getBuilder()
                .withDateTimeFirstInstall(INSTALL_DATE)
                .withSerial(SERIAL)
                .build();
        map2 = new HashMap<>();
        map2.put(SERIAL, device2);

    }

    @Test
    void mergeUsbDeviceInfo() {
        SystemInfo systemInfo = new SystemInfo();
        systemInfo
                .mergeUsbDeviceInfo(map1)
                .mergeUsbDeviceInfo(map2);

        assertThat(systemInfo.getUsbDeviceMap(),hasKey(SERIAL));
        assertThat(systemInfo.getUsbDeviceMap().get(SERIAL).getVid(), is(not(blankOrNullString())));
        assertThat(systemInfo.getUsbDeviceMap().get(SERIAL).getDateTimeFirstInstall(), is(INSTALL_DATE));
    }
}