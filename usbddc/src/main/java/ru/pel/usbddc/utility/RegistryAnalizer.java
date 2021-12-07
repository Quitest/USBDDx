package ru.pel.usbddc.utility;

import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import ru.pel.usbddc.entity.USBDevice;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Предназначен для сбора информации о USB устройствах из рестра ОС Windows.
 */
public class RegistryAnalizer {
    private final static String REG_KEY_USB = "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Enum\\USB";
    private final static String REG_KEY_MOUNTED_DEVICES = "HKEY_LOCAL_MACHINE\\SYSTEM\\MountedDevices";

    /**
     * Получить список USB устройств когда-либо подключенных к АРМ.
     * Информация берется из HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Enum\USB
     * @return список USB устройств, когда-либо подключенных и зарегистрированных в ОС.
     */
    public static List<USBDevice> getUSBDevices() {
//        String REG_KEY_USB = "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Enum\\USB";
        List<USBDevice> usbDevices = new ArrayList<>();
        USBDevice.setUsbIds("usb.ids");
        List<String> pidVidList = WinRegReader.getSubkeys(REG_KEY_USB);
        for (String pidvid : pidVidList) {
            List<String> listSerialKeys = WinRegReader.getSubkeys(pidvid);
            for (String serialKey : listSerialKeys) {
                /*if (serialKey.contains("9000938F29B1F646"))*/
                {
                    String compatibleIDs = WinRegReader.getValue(serialKey, "CompatibleIDs").orElse("");
                    String friendlyName = WinRegReader.getValue(serialKey, "FriendlyName").orElse("");
                    String hardwareID = WinRegReader.getValue(serialKey, "HardwareID").orElse("");
                    String pid = parsePid(pidvid.toLowerCase()).orElse("<N/A>");
                    String[] tmpArr = serialKey.split("\\\\");
                    String serial = tmpArr[tmpArr.length - 1];
                    String service = WinRegReader.getValue(serialKey, "Service").orElse("");
                    String vid = parseVid(pidvid.toLowerCase()).orElse("<N/A>");

                    USBDevice.Builder currUsbDev = USBDevice.Builder.builder();

                    Map<String, String> currValues = WinRegReader.getAllValuesInKey(serialKey).get();
                    for (Map.Entry<String, String> entry : currValues.entrySet()) {
                        currUsbDev.setField(entry.getKey(), entry.getValue());
                    }

                    currUsbDev
                            .withCompatibleIDs(compatibleIDs)
                            .withHardwareId(hardwareID)
                            .withFriendlyName(friendlyName)
                            .withSerial(serial)
                            .withVidPid(vid, pid);

                    usbDevices.add(currUsbDev.build());
                }
            }
        }

        return usbDevices;
    }

    /**
     * Метод собирает сведения о смонтированных устройствах.
     * Информация берется из HKEY_LOCAL_MACHINE\SYSTEM\MountedDevices
     *
     * @return мапу точек монтирования
     */
    public static Map<String, String> getMountedDevices() {
        Map<String,String> mountedDevices = WinRegReader.getAllValuesInKey(REG_KEY_MOUNTED_DEVICES).orElseThrow();
        Base64.Decoder decoder = Base64.getDecoder();
        for (Map.Entry<String,String> entry : mountedDevices.entrySet()){
            String value = entry.getValue();
            String[] val = value.split("(?<=\\G..)"); // разбиваем строку на парные числа - байты
            String collect = Arrays.stream(val)
                    .filter(str->!str.equals("00"))
                    .map(b -> Integer.parseInt(b, 16))
                    .map(Character::toString)
                    .collect(Collectors.joining());
//            System.out.println(collect);
            entry.setValue(collect);
        }
        return mountedDevices;
    }

    public static List<USBDevice> getUSBDevicesWithAutoFilling() {
//        String REG_KEY_USB = "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Enum\\USB";
        List<String> subkeys = WinRegReader.getSubkeys(REG_KEY_USB);
        USBDevice.setUsbIds("usb.ids");
        List<USBDevice> usbDevices = new ArrayList<>();
        for (String pidvid : subkeys) {
            List<String> serials = WinRegReader.getSubkeys(pidvid);
            for (String serial : serials) {
                Map<String, String> valueList = WinRegReader.getAllValuesInKey(serial).get();
                USBDevice.Builder currDevice = USBDevice.Builder.builder();
                valueList.forEach(currDevice::setField);
                usbDevices.add(currDevice.build());
            }
        }
        return usbDevices;
    }

    /**
     * @param pidvid строка, содержащая в себе подстроку вида {@code vid_VVVV&PID_PPPP}
     * @return значение VID (VendorID)
     */
    private static Optional<String> parseVid(String pidvid) {
        Matcher matcher = Pattern.compile("vid_(.{4})").matcher(pidvid);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    /**
     * @param pidvid строка, содержащая в себе подстроку вида {@code pid_VVVV&PID_PPPP}
     * @return значение PID (ProductID)
     */
    private static Optional<String> parsePid(String pidvid) {
        Matcher matcher = Pattern.compile("pid_(.{4})").matcher(pidvid);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
}
