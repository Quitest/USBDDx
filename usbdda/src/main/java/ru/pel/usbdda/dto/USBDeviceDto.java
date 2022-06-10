package ru.pel.usbdda.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class USBDeviceDto {

    @JsonProperty("vid")
    private String vid;

    @JsonProperty("serial")
    private String serial;

    @JsonProperty("dateTimeFirstInstall")
    private LocalDateTime dateTimeFirstInstall;

    @JsonProperty("volumeLabelList")
    private Set<String> volumeLabelList;

    @JsonProperty("volumeIdList")
    private List<Long> volumeIdList;

    @JsonProperty("serialOSGenerated")
    private boolean serialOSGenerated;

    @JsonProperty("guid")
    private String guid;

    @JsonProperty("pid")
    private String pid;

    @JsonProperty("userAccountsList")
    private List<UserProfileDto> userProfileList;

    @JsonProperty("vendorName")
    private String vendorName;

    @JsonProperty("vendorNameByRegistry")
    private String vendorNameByRegistry;

    @JsonProperty("friendlyName")
    private String friendlyName;

    @JsonProperty("productName")
    private String productName;

    @JsonProperty("productNameByRegistry")
    private String productNameByRegistry;

    @JsonProperty("diskId")
    private String diskId;

    @JsonProperty("revision")
    private String revision;

    @JsonProperty("systemInfoList")
    private List<SystemInfoDto> systemInfoList;
}