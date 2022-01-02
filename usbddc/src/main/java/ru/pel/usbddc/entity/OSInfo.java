package ru.pel.usbddc.entity;

import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.List;

@Getter
@Setter
public class OSInfo {
    private String osName;
    private double osVersion;
    private String osArch;
    private Path tmpdir;
    private String username;
    private Path homeDir;
    private Path currentDir;
    private Path systemRoot;
    private String computerName;
    private List<NetworkInterface> networkInterfaceList;
}
