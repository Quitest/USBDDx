package ru.pel.usbdda.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class SystemInfo {
    @JsonProperty("osInfo")
    private OSInfo osInfo;

    @JsonProperty("usbDeviceMap")
    private Map<String, USBDevice> usbDeviceMap;
}
