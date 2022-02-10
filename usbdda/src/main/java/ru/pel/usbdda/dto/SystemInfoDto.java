package ru.pel.usbdda.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SystemInfoDto {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("uuid")
    private String uuid;

    @JsonProperty("osInfo")
    private OSInfoDto osInfo;

    @JsonProperty("usbDeviceList")
    private List<USBDeviceDto> usbDeviceList;
}
