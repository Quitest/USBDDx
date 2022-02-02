package ru.pel.usbdda.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InetAddressDto {
    @JsonProperty("hostAddress")
    private String hostAddress;

    @JsonProperty("hostName")
    private String hostName;

    @JsonProperty("canonicalName")
    private String canonicalName;
}
