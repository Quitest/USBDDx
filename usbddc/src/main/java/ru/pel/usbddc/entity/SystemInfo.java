package ru.pel.usbddc.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public class SystemInfo {
    private OSInfo osInfo;
    private Map<String,USBDevice> usbDeviceMap;

    public SystemInfo() {
        osInfo = new OSInfo();
        usbDeviceMap = new HashMap<>();
    }

    public SystemInfo mergeUsbDeviceInfo(Map<String,USBDevice> src){
        usbDeviceMap = Stream.of(usbDeviceMap,src)
                .flatMap(map-> map.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        USBDevice::copyNonBlankProperties
                ));
        return this;
    }
}
