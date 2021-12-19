package ru.pel.usbddc.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.pel.usbddc.entity.USBDevice;
import ru.pel.usbddc.entity.UserProfile;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RegistryAnalyzerTest {
    private static Map<String, USBDevice> usbDeviceMap;
    private final String testFailedMsg = "Возможно, тест запущен на другом ПК? Проверьте константы.";

    //Вариант 1
    private final String expectedPid = "312b";
    private final String expectedVid = "125f";
    private final String expectedSerial = "1492710242260098";
    private final String mountedDeviceKey = "\\??\\Volume{5405623b-31de-11e5-8295-54a0503930d0}";
    private final String expectedMountedDeviceValue = "_??_USBSTOR#Disk&Ven_ADATA&Prod_USB_Flash_Drive&Rev_0.00#1492710242260098&0#{53f56307-b6bf-11d0-94f2-00a0c91efb8b}";
    private final String expectedMountPoints2 = "{5405623b-31de-11e5-8295-54a0503930d0}";
    private final String expectedGuid = expectedMountPoints2;

    //Вариант 2
//    private final String expectedPid = "6387";
//    private final String expectedVid = "058f";
//    private final String expectedSerial = "EFF732B1";
//    private final String mountedDeviceKey = "\\??\\Volume{92e41808-1e77-11e7-8268-d86126b14266}";
//    private final String expectedMountedDeviceValue = "\\??\\USBSTOR#CdRom&Ven_ASUS&Prod_Device_CD-ROM&Rev_0310#7&1f412d32&0&FCAZCY04R051&0#{53f5630d-b6bf-11d0-94f2-00a0c91efb8b}";
//    private final String expectedMountPoints2 = "{2b3985d3-70cc-11e5-8259-18cf5e52a036}";
//    private final String expectedGuid = expectedMountPoints2;

    @BeforeAll
    static void beforeAll() {
        usbDeviceMap = new RegistryAnalyzer().getRegistryAnalysis(true);
    }

    @Test
    @DisplayName("Определение устройств, подключенных текущим пользователем")
    void determineDeviceUsers() {
        List<UserProfile> userAccountsList = usbDeviceMap.get(expectedSerial).getUserAccountsList();
        Path currentUserHomedir = new OSInfoCollector().getHomedir();
        UserProfile user = userAccountsList.stream()
                .filter(userProfile -> userProfile.getProfileImagePath().equals(currentUserHomedir))
                .findFirst().orElseThrow();

        assertTrue(userAccountsList.contains(user));
    }

    @Test
    void getMountPoints2OfCurrentUser() {
        List<String> mountPoints2 = new RegistryAnalyzer().getMountPoints2OfCurrentUser();
        assertTrue(mountPoints2.stream().anyMatch(mp -> mp.contains(expectedMountPoints2)), testFailedMsg);
    }

    @Test
    @DisplayName("Сбор смонтированных устройств")
    void getMountedDevicesTest1() {
        String actual = new RegistryAnalyzer().getMountedDevices().get(mountedDeviceKey);
        assertEquals(expectedMountedDeviceValue, actual, testFailedMsg);
    }

    @Test
    @DisplayName("Тест на заполнение GUID'ов")
    void getMountedDevicesTest2() {
        USBDevice usbDevice = usbDeviceMap.entrySet().stream()
                .filter(entry -> entry.getKey().equals(expectedSerial))
                .map(Map.Entry::getValue)
                .findFirst().orElse(USBDevice.getBuilder().build());

        assertEquals(expectedGuid, usbDevice.getGuid(), testFailedMsg);

    }

    @Test
    @DisplayName("Хотя бы одно устройство находится?")
    void getUSBDevicesTest1() {
        assertAll("Поиск устройства по серийному номеру",
                () -> assertTrue(usbDeviceMap.containsKey(expectedSerial)),
                () -> assertEquals(expectedVid, usbDeviceMap.get(expectedSerial).getVid()),
                () -> assertEquals(expectedPid, usbDeviceMap.get(expectedSerial).getPid()));
    }

    @Test
    @DisplayName("Сбор профилей пользователей")
    void getUserProfileList() {
        List<UserProfile> userProfileList = new RegistryAnalyzer().getUserProfileList();

        assertTrue(userProfileList.stream().anyMatch(u -> u.getUsername().matches("[\\w\\d]+")), testFailedMsg);
        assertTrue(userProfileList.stream().allMatch(p -> p.getSecurityId().matches("S[-\\d]+")), testFailedMsg);
        assertTrue(userProfileList.stream().allMatch(p -> p.getProfileImagePath().toString().matches("[\\w\\d\\\\%:]+")), testFailedMsg);

    }

    @Test
    @DisplayName("Порядок вызова анализирующих методов не влияет на результат?")
    void orderInvokingRegistryAnalysisMethod() throws InvocationTargetException, IllegalAccessException {
        RegistryAnalyzer registryAnalyzer1 = new RegistryAnalyzer();
        registryAnalyzer1.getUsbDevices();
        registryAnalyzer1.getMountedDevices();
        Map<String, USBDevice> map1 = registryAnalyzer1.getRegistryAnalysis(false);

        RegistryAnalyzer registryAnalyzer2 = new RegistryAnalyzer();
        registryAnalyzer2.getMountedDevices();
        registryAnalyzer2.getUsbDevices();
        Map<String, USBDevice> map2 = registryAnalyzer2.getRegistryAnalysis(false);

        assertEquals(map1, map2);
    }
}