package ru.pel.usbdda.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;
import java.util.List;

public class OSInfo {
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
