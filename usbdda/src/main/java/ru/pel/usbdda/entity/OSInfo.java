package ru.pel.usbdda.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Getter
@Setter
@Entity
public class OSInfo {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    private String osId;
    private String osName;
    private double osVersion;
    private String osArch;
    private String tmpdir;
    private String username;
    private String homeDir;
    private String currentDir;
    private String systemRoot;
    private String computerName;

    @OneToMany(mappedBy = "osInfo")
    private List<NetworkInterface> networkInterfaceList;
}
