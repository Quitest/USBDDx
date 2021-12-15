package ru.pel.usbddc.utility;

import ru.pel.usbddc.entity.USBDevice;
import ru.pel.usbddc.entity.UserProfile;

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
     * Получить точки монтирования ТЕКУЩЕГО пользователя
     *
     * @return список всех когда-либо существовавших точек монтирования
     */
    public List<String> getMountPoints2OfCurrentUser() {
        String mountPoints2 = "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\MountPoints2";

        return WinRegReader.getSubkeys(mountPoints2).stream()
                .filter(e -> e.matches(".+\\{[a-fA-F0-9-]+}"))
                .map(e -> e.substring(e.lastIndexOf("{")))
                .collect(Collectors.toList());
    }

    /**
     * Метод собирает сведения о смонтированных устройствах.
     * Информация берется из HKEY_LOCAL_MACHINE\SYSTEM\MountedDevices
     *
     * @return мапу смонтированных устройств
     */
    public Map<String, String> getMountedDevices() {
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

            USBDevice mountedUSBDevice = USBDevice.getBuilder()
                    .withSerial(serial)
                    .withGuid(deviceGuid)
                    .build();

            usbDeviceMap.merge(serial,
                    mountedUSBDevice,
                    (oldDevice, newDevice) -> {
                        oldDevice.setGuid(deviceGuid);
                        return oldDevice;
                    });
        }
        return mountedDevices;
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
        List<String> subkeys = WinRegReader.getSubkeys(REG_KEY_USB);
        USBDevice.setUsbIds("usb.ids");
        List<USBDevice> usbDevices = new ArrayList<>();
        for (String pidvid : subkeys) {
            List<String> serials = WinRegReader.getSubkeys(pidvid);
            for (String serial : serials) {
                Map<String, String> valueList = WinRegReader.getAllValuesInKey(serial).orElseThrow();
                USBDevice.Builder currDevice = USBDevice.getBuilder();
                valueList.forEach(currDevice::setField);
                usbDevices.add(currDevice.build());
            }
        }
        return usbDevices;
    }

    public Map<String, USBDevice> getUsbDeviceMap() {
        getUsbDevices();
        getMountedDevices();
        return usbDeviceMap;
    }

    /**
     * Получить список USB устройств когда-либо подключенных к АРМ.
     * Информация берется из HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Enum\USB
     *
     * @return список USB устройств, когда-либо подключенных и зарегистрированных в ОС.
     */
    public Map<String, USBDevice> getUsbDevices() {
        USBDevice.setUsbIds("usb.ids");
        List<String> pidVidList = WinRegReader.getSubkeys(REG_KEY_USB);
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

                //FIXME сделать слияние, а не замену.
                usbDeviceMap.put(serial, currUsbDev);

//                usbDeviceMap.merge(serial,
//                        currUsbDev,
//                        (oldDevice, newDevice) -> {
//                            oldDevice.;
//                            return oldDevice;
//                        });
            }
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
        List<String> profileRegKeys = WinRegReader.getSubkeys(REG_PROFILE_LIST);
        return profileRegKeys.stream()
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
