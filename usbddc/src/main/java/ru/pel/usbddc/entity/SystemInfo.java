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
    private OSInfo osInfo = new OSInfo();
    private String uuid = "";
    private Map<String, USBDevice> usbDeviceMap = new HashMap<>();

    public SystemInfo() {}

    /**
     * Выполнить слияние информации о USBDevice. Свойства, не равные null и не пустые, копируются в текущий объект из
     * источника.
     *
     * @param src источник данных для слияния.
     * @return текущий объект с дополненными данными.
     */
    public SystemInfo mergeUsbDeviceInfo(Map<String, USBDevice> src) {
        usbDeviceMap = Stream.of(usbDeviceMap, src)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        USBDevice::copyNonBlankProperties
                ));
        return this;
    }
}
