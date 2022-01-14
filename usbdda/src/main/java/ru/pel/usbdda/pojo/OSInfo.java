package ru.pel.usbdda.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.List;

@Getter
@Setter
public class OSInfo {
    @JsonProperty("osId")
    private String osId;

    @JsonProperty("osName")
    private String osName;

    @JsonProperty("osVersion")
    private double osVersion;

    @JsonProperty("osArch")
    private String osArch;

    @JsonProperty("tmpdir")
    private Path tmpdir;

    @JsonProperty("username")
    private String username;

    @JsonProperty("homeDir")
    private Path homeDir;

    @JsonProperty("currentDir")
    private Path currentDir;

    @JsonProperty("systemRoot")
    private Path systemRoot;

    @JsonProperty("computerName")
    private String computerName;

    @JsonProperty("networkInterfaceList")
    private List<NetworkInterface> networkInterfaceList;
}
