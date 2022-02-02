package ru.pel.usbdda.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import ru.pel.usbdda.entity.SystemInfo;

import java.nio.file.Path;
import java.util.List;

@Getter
@Setter
public class OSInfoDto {
    @JsonProperty("osId")
    private String osId;

    @JsonProperty("osName")
    private String osName;

    @JsonProperty("osVersion")
    private double osVersion;

    @JsonProperty("osArch")
    private String osArch;

    @JsonProperty("tmpdir")
    private String tmpdir;

    @JsonProperty("username")
    private String username;

    @JsonProperty("homeDir")
    private String homeDir;

    @JsonProperty("currentDir")
    private String currentDir;

    @JsonProperty("systemRoot")
    private String systemRoot;

    @JsonProperty("computerName")
    private String computerName;

    @JsonProperty("networkInterfaceList")
    private List<NetworkInterfaceDto> networkInterfaceList;

//    @JsonProperty("systemInfo")
//    private SystemInfo systemInfo;
}
