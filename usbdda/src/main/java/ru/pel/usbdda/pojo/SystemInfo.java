package ru.pel.usbdda.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class SystemInfo {
    @JsonProperty("osInfo")
    private OSInfo osInfo;

    @JsonProperty("usbDeviceMap")
    private Map<String, USBDevice> usbDeviceMap;
}
