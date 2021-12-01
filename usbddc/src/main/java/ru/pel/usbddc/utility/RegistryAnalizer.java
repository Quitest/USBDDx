package ru.pel.usbddc.utility;

import ru.pel.usbddc.entity.USBDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Предназначен для сбора информации о USB устройствах из рестра ОС Windows.
 */
public class RegistryAnalizer {
    public static List<USBDevice> getUSBDevices() {
        String regKeyUSB = "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Enum\\USB";
        List<USBDevice> usbDevices = new ArrayList<>();
        USBDevice.setUsbIds("usb.ids");
        List<String> pidVidList = WinRegReader.getSubkeys(regKeyUSB);
        for (String pidvid : pidVidList) {
            List<String> listSerialKeys = WinRegReader.getSubkeys(pidvid);
            for (String serialKey : listSerialKeys) {
                /*if (serialKey.contains("5&7d0600&0&7"))*/ {
                    String containerID = WinRegReader.getValue(serialKey, "ContainerID").orElse("");
                    String friendlyName = WinRegReader.getValue(serialKey, "FriendlyName").orElse("<N/A>");
                    String hardwareID = WinRegReader.getValue(serialKey, "HardwareID").orElse("");
                    String pid = parsePid(pidvid.toLowerCase()).orElse("<N/A>");
                    String[] tmpArr = serialKey.split("\\\\");
                    String serial = tmpArr[tmpArr.length - 1];
                    String service = WinRegReader.getValue(serialKey, "Service").orElse("");
                    String vid = parseVid(pidvid.toLowerCase()).orElse("<N/A>");

                    USBDevice currUsbDev = new USBDevice(); //TODO спользовтаь паттерн билдер

                    Map<String,String> currValues = WinRegReader.getAllValuesInKey(serialKey).get();
                    for (Map.Entry<String, String> entry : currValues.entrySet()){
                        currUsbDev.setField(entry.getKey(),entry.getValue());
                    }
//                    currUsbDev.setContainerID(containerID);
//                    currUsbDev.setFriendlyName(friendlyName);
//                    currUsbDev.setHardwareID(hardwareID);
//                    currUsbDev.setService(service);


                    currUsbDev.setSerial(serial);
                    currUsbDev.setVidPid(vid,pid);
                    currUsbDev.determineVendorName();
                    currUsbDev.determineProductName();



                    usbDevices.add(currUsbDev);
                }
            }
        }

        return usbDevices;
    }

    public static List<USBDevice> getUSBDevicesWithAutoFilling(){
        String regKeyUSB = "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Enum\\USB";
        List<String> subkeys = WinRegReader.getSubkeys(regKeyUSB);
        USBDevice.setUsbIds("usb.ids");
        List<USBDevice> usbDevices = new ArrayList<>();
        for (String pidvid : subkeys){
            List<String> serials = WinRegReader.getSubkeys(pidvid);
            for (String serial : serials){
                Map<String,String> valueList = WinRegReader.getAllValuesInKey(serial).get();
                USBDevice currDevice=new USBDevice();
                valueList.forEach(currDevice::setField);
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
