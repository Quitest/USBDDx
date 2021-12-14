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

    private final List<USBDevice> usbDeviceList;

    public RegistryAnalyzer() {
        this.usbDeviceList = new ArrayList<>();
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
            String decodedValue = Arrays.stream(encodedValue.split("(?<=\\G..)"))// разбиваем строку на парные числа - байты
                    .filter(str -> !str.equals("00")) //отбрасываем нулевые байты, что бы в результате не было "пробельных" символов
                    .map(str -> Character.toString(Integer.parseInt(str, 16))) // преобразуем HEX в строковые значения
                    .collect(Collectors.joining());
            entry.setValue(decodedValue);

            String key = entry.getKey();
            String deviceGuid = Arrays.stream(key.split(""))
                    .dropWhile(ch -> !ch.equals("{"))
                    .collect(Collectors.joining());

//            try {
//                String deviceSerialNo = decodedValue.split("#")[2];
//                deviceSerialNo = deviceSerialNo.substring(0, deviceSerialNo.lastIndexOf("&") - 1);
//
//
//                for (USBDevice usbDevice : usbDeviceList) {
//                    if (usbDevice.getSerial().equals(deviceSerialNo)) {
//                        usbDevice.setGuid(deviceGuid);
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

//            String guid;
//            int index = key.lastIndexOf("{");
//            if (index != -1) {
//                guid = key.substring(key.lastIndexOf("{"));
//            } else {
//                guid = "";
//            }
        }
        return mountedDevices;
    }

    /**
     * Получить список USB устройств когда-либо подключенных к АРМ.
     * Информация берется из HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Enum\USB
     *
     * @return список USB устройств, когда-либо подключенных и зарегистрированных в ОС.
     */
    public List<USBDevice> getUSBDevices() {
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
                usbDeviceList.add(currUsbDev);
            }
        }

        return usbDeviceList;
    }

    /**
     * Позволяет получить список USB устройств, когда-либо подключаемых к системе. Заполнение полей USBDevice происходит
     * автоматически из полей реестра имеющих такие же наименования.
     *
     * @return
     */
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

    public List<USBDevice> getUsbDeviceList() {
        getUSBDevices();
        getMountedDevices();
        return usbDeviceList;
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
