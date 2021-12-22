package ru.pel.usbddc.service;

import ru.pel.usbddc.entity.USBDevice;
import ru.pel.usbddc.entity.UserProfile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Предназначен для сбора информации о USB устройствах из рестра ОС Windows.
 */
public class RegistryAnalyzer {
    private static final String REG_KEY_USB = "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Enum\\USB";
    private static final String REG_KEY_MOUNTED_DEVICES = "HKEY_LOCAL_MACHINE\\SYSTEM\\MountedDevices";
    private static final String REG_PROFILE_LIST = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\ProfileList";

    private final Map<String, USBDevice> usbDeviceMap;

    public RegistryAnalyzer() {
        this.usbDeviceMap = new HashMap<>();
    }

    /**
     * Декодирование строки текста из HEX-представления в читаемый формат.
     *
     * @param hexStr декодируемая строка
     * @return строка в текстовом читаемом виде.
     */
    private String decodeHexToString(String hexStr) {
        return Arrays.stream(hexStr.split("(?<=\\G..)"))// разбиваем строку на парные числа - байты
                .filter(str -> !str.equals("00")) //отбрасываем нулевые байты, что бы в результате не было "пробельных" символов
                .map(str -> Character.toString(Integer.parseInt(str, 16))) // преобразуем HEX в строковые значения
                .collect(Collectors.joining());
    }

    /**
     * <p>Определяет под какой учетной записью осуществлялось использование устройства.</p>
     * <p>Определение происходит путем сопоставления имеющегося GUID и
     * из куста реестра пользователя \Software\Microsoft\Windows\CurrentVersion\Explorer\MountPoints2
     * </p>
     *
     * @return число устройств, пользователей которых удалось определить.
     */
    public long determineDeviceUsers() {
        List<UserProfile> userProfileList = getUserProfileList();
        String currentUserHomeDir = System.getProperty("user.home");
        long counter = 0;
        for (UserProfile userProfile : userProfileList) {
            List<String> mountedGUIDsOfUser = userProfile.getProfileImagePath().toString().equals(currentUserHomeDir) ?
                    getMountedGUIDsOfCurrentUser() : getMountedGUIDsOfUser(userProfile);
            counter += usbDeviceMap.values().stream()
                    .filter(usbDevice -> mountedGUIDsOfUser.contains(usbDevice.getGuid()))
                    .map(usbDevice -> {
                        usbDevice.addUserProfile(userProfile);
                        return usbDevice;
                    }).count();
        }
        return counter;
    }

    /**
     * <p>Метод собирает сведения о смонтированных устройствах. Основная задача - установить соответствие между GUID и
     * серийными номерами устройств.</p>
     * <p>Если устройство с серийным номером уже имеется, то сведения о нем обновляются. Если устройства с серийным
     * номером нет, то оно запоминается.</p>
     * <p>Информация берется из HKEY_LOCAL_MACHINE\SYSTEM\MountedDevices</p>
     *
     * @return мапу смонтированных устройств. Key - серийный номер, value - устройство со всеми известными сведениями о
     * нем на текущий момент.
     */
    public Map<String, USBDevice> associateSerialToGuid() {
        Map<String, String> mountedDevices = WinRegReader.getAllValuesInKey(REG_KEY_MOUNTED_DEVICES).orElseThrow();

        for (Map.Entry<String, String> entry : mountedDevices.entrySet()) {
            String encodedValue = entry.getValue();
            String decodedValue = decodeHexToString(encodedValue);

            String serial = Arrays.stream(decodedValue.split("#"))
                    .skip(2)
                    .map(s -> s.substring(0, s.length() - 2)) //в серийном номере отбрасываем последние два символа - это, как правило, &0 или &1 - какие-то системные суффиксы
                    .findFirst().orElse(decodedValue);
            entry.setValue(decodedValue);

            String key = entry.getKey();
            String deviceGuid = Arrays.stream(key.split(""))
                    .dropWhile(ch -> !ch.equals("{"))
                    .collect(Collectors.joining());

            //FIXME заменить код на метод копирования_ненулевых_свойств()
            USBDevice tmp = usbDeviceMap.get(serial);
            if (tmp == null) {
                usbDeviceMap.put(serial, USBDevice.getBuilder().withSerial(serial).withGuid(deviceGuid).build());
            } else {
                tmp.setGuid(deviceGuid);
                usbDeviceMap.put(serial, tmp);
            }
        }
        return usbDeviceMap;
    }

    /**
     * Получить GUID устройств, которые использовались ТЕКУЩИМ пользователем.
     *
     * @return список GUID всех когда-либо подключенных устройств.
     */
    public List<String> getMountedGUIDsOfCurrentUser() {
        String mountPoints2 = "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\MountPoints2";
        List<String> result = new ArrayList<>();
        try {
            result = WinRegReader.getSubkeys(mountPoints2).stream()
                    .filter(e -> e.matches(".+\\{[a-fA-F0-9-]+}"))
                    .map(e -> e.substring(e.lastIndexOf("{")))
                    .collect(Collectors.toList());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * <p>Получение GUID устройств подключенных указанным пользователем. Данные берутся из загруженного соответствующего
     * куста NTUSER.DAT.</p>
     * <p>Если указан профиль текущего пользователя, то вызывается метод {@link #getMountedGUIDsOfCurrentUser() } </p>
     *
     * @param userProfile профиль пользователя, из которого необходимо получить GUID'ы.
     * @return список GUID всех когда-либо подключенных указанным пользователем устройств.
     */
    public List<String> getMountedGUIDsOfUser(UserProfile userProfile) {
        String currentUserHomedir = System.getProperty("user.home");
        String profileHomedir = userProfile.getProfileImagePath().toString();
        if (profileHomedir.equals(currentUserHomedir)){
            return getMountedGUIDsOfCurrentUser();
        }

        List<String> guidList = new ArrayList<>();
        String username = userProfile.getUsername().replaceAll("[\\s\\.-]+", "");
        String nodeName = "HKEY_LOCAL_MACHINE\\userHive_" + username;
        String userHive = userProfile.getProfileImagePath().toString() + "\\NTUSER.DAT";
        try {
            WinRegReader.loadHive(nodeName, userHive);

            guidList = WinRegReader.getSubkeys(nodeName + "\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\MountPoints2").stream()
                    .filter(e -> e.matches(".+\\{[a-fA-F0-9-]+}"))
                    .map(e -> e.substring(e.lastIndexOf("{")))
                    .collect(Collectors.toList());

            WinRegReader.unloadHive(nodeName);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return guidList;
    }

    /**
     * Получить результат анализа реестра. Результат содержит в себе все данные, которые удалось получить путем чтения
     * реестра.
     *
     * @param doNewAnalysis true - собирает все данные об устройствах из реестра заново, false - возвращает ранее полученные
     *                      данные
     * @return
     */
    public Map<String, USBDevice> getRegistryAnalysis(boolean doNewAnalysis) {
        if (doNewAnalysis) {
            try {
                getUsbDevices();
            } catch (InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
            associateSerialToGuid();
            determineDeviceUsers();
        }
        return usbDeviceMap;
    }

    /**
     * Позволяет получить список USB устройств, когда-либо подключаемых к системе. Заполнение полей USBDevice происходит
     * автоматически из полей реестра имеющих такие же наименования.
     *
     * @return Список USBDevice с полями заполненными из ветки реестра HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Enum\USB
     * @deprecated использует рефлексию. Тесты на корректность работы не проходил.
     */
    @Deprecated(forRemoval = true)
    public List<USBDevice> getUSBDevicesWithAutoFilling() {
        List<USBDevice> usbDevices = new ArrayList<>();
        try {
            List<String> subkeys = WinRegReader.getSubkeys(REG_KEY_USB);
            USBDevice.setUsbIds("usb.ids");

            for (String pidvid : subkeys) {
                List<String> serials = WinRegReader.getSubkeys(pidvid);
                for (String serial : serials) {
                    Map<String, String> valueList = WinRegReader.getAllValuesInKey(serial).orElseThrow();
                    USBDevice.Builder currDevice = USBDevice.getBuilder();
                    valueList.forEach(currDevice::setField);
                    usbDevices.add(currDevice.build());
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return usbDevices;
    }

    /**
     * Получить список USB устройств когда-либо подключенных к АРМ.
     * Информация берется из HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Enum\USB
     *
     * @return список USB устройств, когда-либо подключенных и зарегистрированных в ОС.
     */
    public Map<String, USBDevice> getUsbDevices() throws InvocationTargetException, IllegalAccessException {
        USBDevice.setUsbIds("usb.ids");
        List<String> pidVidList = null;
        try {
            pidVidList = WinRegReader.getSubkeys(REG_KEY_USB);

            for (String pidvid : pidVidList) {
                List<String> listSerialKeys = WinRegReader.getSubkeys(pidvid);
                for (String serialKey : listSerialKeys) {
                    String pid = parsePid(pidvid.toLowerCase()).orElse("<N/A>");
                    String vid = parseVid(pidvid.toLowerCase()).orElse("<N/A>");
                    String[] tmpArr = serialKey.split("\\\\");
                    String serial = tmpArr[tmpArr.length - 1];

                    USBDevice currUsbDev = USBDevice.getBuilder()
                            .withSerial(serial)
                            .withVidPid(vid, pid)
                            .build();

                    //FIXME заменить методом копировать_ненулевые_свойства()
                    USBDevice updatedUSBDevice = usbDeviceMap.get(serial);
                    if (updatedUSBDevice == null) {                         //если в мапу ранее не записывали устройства с таким же серийником
                        usbDeviceMap.put(serial, currUsbDev);               //то просто заносим новое устройство,
                    } else {                                                //иначе
                        updatedUSBDevice.copyNonNullProperties(currUsbDev); //копируем новые свойства в свойства существующего устройства
                        usbDeviceMap.put(serial, updatedUSBDevice);         //и записываем, т.е., по сути, обновляем.
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return usbDeviceMap;
    }

    /**
     * <p>Читает пути к профилям пользователей из HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows NT\CurrentVersion\ProfileList.
     * По указанному пути будут соответствующие записи только если пользователь хотя бы раз логинился. Этого вполне достаточно,
     * т.к. если пользователь ниразу не логинился в системе, то он не мог использовать USB устройства.</p>
     *
     * <p>В качестве альтернативного решения задачи определения имени пользователя по SID может служить команда
     * <code>wmic useraccount where sid="" get name</code>. Подробности можно найти, например,
     * по <a href="https://www.lifewire.com/how-to-find-a-users-security-identifier-sid-in-windows-2625149">ссылке</a></p>
     *
     * @return список созданных профилей пользователей
     */
    public List<UserProfile> getUserProfileList() {
        List<UserProfile> userProfileList = new ArrayList<>();
        try {
            List<String> profileRegKeys = WinRegReader.getSubkeys(REG_PROFILE_LIST);
            userProfileList = profileRegKeys.stream()
                    .map(profile -> {
                        String profileImagePath = WinRegReader.getValue(profile, "ProfileImagePath").orElseThrow();
                        String username = profileImagePath.substring(profileImagePath.lastIndexOf("\\") + 1);
                        String sid = profile.substring(profile.lastIndexOf("\\") + 1);

                        UserProfile.Builder builder = UserProfile.getBuilder();
                        return builder
                                .withProfileImagePath(Path.of(profileImagePath))
                                .withSecurityId(sid)
                                .withUsername(username)
                                .build();
                    })
                    .collect(Collectors.toList());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return userProfileList;
    }

    /**
     * @param pidvid строка, содержащая в себе подстроку вида {@code pid_VVVV&PID_PPPP}
     * @return значение PID (ProductID)
     */
    private Optional<String> parsePid(String pidvid) {
        Matcher matcher = Pattern.compile("pid_(.{4})").matcher(pidvid);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    /**
     * @param pidvid строка, содержащая в себе подстроку вида {@code vid_VVVV&PID_PPPP}
     * @return значение VID (VendorID)
     */
    private Optional<String> parseVid(String pidvid) {
        Matcher matcher = Pattern.compile("vid_(.{4})").matcher(pidvid);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
}
