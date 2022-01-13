package ru.pel.usbdda.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import ru.pel.usbdda.service.SystemInfoDeserializer;

import java.util.Map;

@Getter
@Setter
@JsonDeserialize(using = SystemInfoDeserializer.class)
public class SystemInfo {
    @JsonProperty("osInfo")
    private OSInfo osInfo;

    @JsonProperty("usbDeviceMap")
    private Map<String, USBDevice> usbDeviceMap;
}
