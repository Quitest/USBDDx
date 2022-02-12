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
import java.util.stream.Collectors;

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
    private final String expectedFriendlyName = "ADATA USB Flash Drive USB Device";
    private final String expectedRevision = "0.00";
    private final String expectedVolumeName = "QUITEST";

    //Вариант 2
//    private final String expectedPid = "6387";
//    private final String expectedVid = "058f";
//    private final String expectedSerial = "EFF732B1";
//    private final String mountedDeviceKey = "\\??\\Volume{92e41808-1e77-11e7-8268-d86126b14266}";
//    private final String expectedMountedDeviceValue = "\\??\\USBSTOR#CdRom&Ven_ASUS&Prod_Device_CD-ROM&Rev_0310#7&1f412d32&0&FCAZCY04R051&0#{53f5630d-b6bf-11d0-94f2-00a0c91efb8b}";
//    private final String expectedMountPoints2 = "{2b3985d3-70cc-11e5-8259-18cf5e52a036}";
//    private final String expectedGuid = expectedMountPoints2;
//    private final String expectedFriendlyName = "Generic Flash Disk USB Device";
//    private final String expectedRevision = "8.07";
//    private final String expectedVolumeName = "USB0005";

    @BeforeAll
    static void beforeAll() {
        usbDeviceMap = new RegistryAnalyzer().getAnalysis(true);
    }

    @Test
    @DisplayName("Поиск подключенных к системе устройств. Ожидается успех. ")
    void associateSerialToGuidTest() {
        USBDevice usbDevice = new RegistryAnalyzer().associateSerialToGuid().entrySet().stream()
                .filter(entry -> entry.getKey().equals(expectedSerial))
                .map(Map.Entry::getValue)
                .findFirst().orElse(USBDevice.getBuilder().build());

        assertEquals(expectedGuid, usbDevice.getGuid());
    }

    @Test
    @DisplayName("Определение устройств, подключенных текущим пользователем")
    void determineDeviceUsers() {
        List<UserProfile> userAccountsList = usbDeviceMap.get(expectedSerial).getUserAccountsList();
        String currentUserHomedir = System.getProperty("user.home");
        UserProfile user = userAccountsList.stream()
                .filter(userProfile -> currentUserHomedir.equals(userProfile.getProfileImagePath().toString()))
                .findFirst().orElseThrow();

        assertTrue(userAccountsList.contains(user));
    }

    @Test
    @DisplayName("Получение информации о всех пользователях устройств")
    void findDevicesWithManyUsers() {
        List<USBDevice> collect = usbDeviceMap.values().stream()
                .filter(usbDevice -> usbDevice.getUserAccountsList().size() > 1)
                .collect(Collectors.toList());

        assertTrue(collect.size() > 0, "В системе есть USB-устройство, использовавшееся несколькими пользователями? Права админа есть?");
    }

    @Test
    @DisplayName("Определение friendlyName устройства")
    void getFriendlyNamesTest() {
        Map<String, USBDevice> friendlyName = new RegistryAnalyzer().getFriendlyName();
        assertEquals(expectedFriendlyName, friendlyName.get(expectedSerial).getFriendlyName());
    }

    @Test
    @DisplayName("Поиск правильного GUID'а. Ожидается успех.")
    void getMountedDevicesTest() {
        USBDevice usbDevice = usbDeviceMap.entrySet().stream()
                .filter(entry -> entry.getKey().equals(expectedSerial))
                .map(Map.Entry::getValue)
                .findFirst().orElse(USBDevice.getBuilder().build());

        assertEquals(expectedGuid, usbDevice.getGuid());
    }

    @Test
    @DisplayName("Получение GUID устройств, подключенных ТЕКУЩИ пользователем. Метод getMountedGUIDsOfCurrentUser()")
    void getMountedGUIDsOfCurrentUserTest() {
        List<String> mountPoints2 = new RegistryAnalyzer().getMountedGUIDsOfCurrentUser();
        String actualGuid = mountPoints2.stream().filter(mp -> mp.equals(expectedMountPoints2)).findFirst().orElse("<NOT FOUND>");

        assertEquals(expectedMountPoints2, actualGuid, "Устройства с такими GUID не нашлось.");
    }

    @Test
    @DisplayName("Получение GUID устройств, подключенных ДРУГИМ пользователем. Метод getMountedGUIDsOfUser()")
    void getMountedGUIDsOfOtherUserTest() {
        UserProfile tmpUser = UserProfile.getBuilder()
                .withUsername("User 2.Lenovo-PC")
                .withProfileImagePath(Path.of("C:\\Users\\User 2.Lenovo-PC")).build();
        //находим профиль текущего пользователя
//        List<UserProfile> userProfileList = new RegistryAnalyzer().getUserProfileList();
//        UserProfile userProfile = userProfileList.get(userProfileList.size() - 1);
        //получаем GUID устройств последнего в спике пользователя
        List<String> mountPoints2 = new RegistryAnalyzer().getMountedGUIDsOfUser(tmpUser);
        String actualGuid = mountPoints2.stream()
                .filter(mp -> mp.equals(expectedMountPoints2))
                .findFirst().orElse("<NOT FOUND>");

        assertEquals(expectedMountPoints2, actualGuid, "Устройств не найдено. Временный профиль существует? Права админа есть?");
    }

    @Test
    @DisplayName("Получение GUID устройств, подключенных ТЕКУЩИМ пользователем. Метод getMountedGUIDsOfUser()")
    void getMountedGUIDsOfUserTest() {
        //находим профиль текущего пользователя
        UserProfile currentUserProfile = new RegistryAnalyzer().getUserProfileList().stream()
                .filter(userProfile -> userProfile.getProfileImagePath().toString().equals(System.getProperty("user.home")))
                .findFirst().orElse(UserProfile.getBuilder().build());
        //получаем GUID устройств текущего пользователя
        List<String> mountPoints2 = new RegistryAnalyzer().getMountedGUIDsOfUser(currentUserProfile);
        String actualGuid = mountPoints2.stream().filter(mp -> mp.equals(expectedMountPoints2)).findFirst().orElse("<NOT FOUND>");

        assertEquals(expectedMountPoints2, actualGuid, "Устройства с такими GUID не нашлось.");
    }

    @Test
    @DisplayName("Заполненность всех полей")
    void getRegistryAnalysisTest() {
        USBDevice usbDevice = usbDeviceMap.get(expectedSerial);
        assertAll(
                () -> assertNotEquals("", usbDevice.getSerial()),
                () -> assertNotEquals("", usbDevice.getPid()),
                () -> assertNotEquals("", usbDevice.getProductName()),
                () -> assertNotEquals("", usbDevice.getVendorName()),
                () -> assertNotEquals("", usbDevice.getVid()),
                () -> assertNotEquals("", usbDevice.getGuid()),
                () -> assertNotEquals("", usbDevice.getFriendlyName()),
                () -> assertNotEquals("", usbDevice.getVolumeName()),
                () -> assertNotEquals("", usbDevice.getRevision()),
                () -> assertFalse(usbDevice.getUserAccountsList().isEmpty())
        );
    }

    @Test
    @DisplayName("Определение ревизии устройства")
    void getRevisionTest() {
        String actualRevision = new RegistryAnalyzer().getFriendlyName().get(expectedSerial).getRevision();
        assertEquals(expectedRevision, actualRevision);
    }

    @Test
    @DisplayName("Сбор всех подключенных USB устройств. Ожидается успех - найдено хотя бы одно устройство")
    void getUsbDevicesTest() throws InvocationTargetException, IllegalAccessException {
        Map<String, USBDevice> usbDevices = new RegistryAnalyzer().getUsbDevices();


        assertAll(
                () -> assertTrue(usbDevices.containsKey(expectedSerial)),
                () -> assertEquals(expectedVid, usbDevices.get(expectedSerial).getVid()),
                () -> assertEquals(expectedPid, usbDevices.get(expectedSerial).getPid())
        );
    }

    @Test
    @DisplayName("Порядок вызова анализирующих методов не влияет на результат?")
    void orderInvokingRegistryAnalysisMethod() {
        //Вариант 0
        RegistryAnalyzer registryAnalyzer = new RegistryAnalyzer();
            registryAnalyzer.getUsbDevices();
        registryAnalyzer.associateSerialToGuid();
        registryAnalyzer.getFriendlyName();
        registryAnalyzer.determineDeviceUsers();
        registryAnalyzer.parseWindowsPortableDevice();
        Map<String, USBDevice> registryAnalysis = registryAnalyzer.getAnalysis(false);

        //Вариант 1
        RegistryAnalyzer registryAnalyzer1 = new RegistryAnalyzer();
        registryAnalyzer1.associateSerialToGuid();
            registryAnalyzer1.getUsbDevices();
        registryAnalyzer1.determineDeviceUsers();
        registryAnalyzer1.getFriendlyName();
        registryAnalyzer1.parseWindowsPortableDevice();
        Map<String, USBDevice> registryAnalysis1 = registryAnalyzer1.getAnalysis(false);

        //Вариант 2
        RegistryAnalyzer registryAnalyzer2 = new RegistryAnalyzer();
        registryAnalyzer2.associateSerialToGuid();
        registryAnalyzer2.determineDeviceUsers();
        registryAnalyzer2.getFriendlyName();
        registryAnalyzer2.parseWindowsPortableDevice();
        Map<String, USBDevice> registryAnalysis2 = registryAnalyzer2.getAnalysis(false);

        //Вариант 3
        RegistryAnalyzer registryAnalyzer3 = new RegistryAnalyzer();
        registryAnalyzer3.associateSerialToGuid();
            registryAnalyzer3.getUsbDevices();
        registryAnalyzer3.parseWindowsPortableDevice();
        registryAnalyzer3.getFriendlyName();
        registryAnalyzer3.determineDeviceUsers();
        Map<String, USBDevice> registryAnalysis3 = registryAnalyzer3.getAnalysis(false);

        assertAll(
                () -> assertEquals(usbDeviceMap, registryAnalysis),
                () -> assertEquals(usbDeviceMap, registryAnalysis1),
                () -> assertEquals(usbDeviceMap, registryAnalysis2),
                () -> assertEquals(usbDeviceMap, registryAnalysis3)
        );
    }

    @Test
    void parseWindowsPortableDevice() {
//        Map<String, USBDevice> stringUSBDeviceMap = new RegistryAnalyzer().parseWindowsPortableDevice();
        List<USBDevice> collect = usbDeviceMap.values().stream()
                .filter(val -> !val.getVolumeName().isEmpty())
                .collect(Collectors.toList());
        assertEquals(expectedVolumeName, usbDeviceMap.get(expectedSerial).getVolumeName());
    }
}