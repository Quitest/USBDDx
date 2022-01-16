package ru.pel.usbdda.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;

@Getter
@Setter
@Entity
public class OSInfo {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

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
    private List<NetworkInterface> networkInterfaceList;
}
