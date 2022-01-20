package ru.pel.usbdda.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class SystemInfoDto {
    @JsonProperty("osInfo")
    private OSInfo osInfo;

    @JsonProperty("usbDeviceMap")
    private Map<String, USBDevice> usbDeviceMap;
}
