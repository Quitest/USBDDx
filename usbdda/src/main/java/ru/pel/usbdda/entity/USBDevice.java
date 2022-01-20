package ru.pel.usbdda.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
public class USBDevice {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    private String vid;
    private String serial;
    private LocalDateTime dateTimeFirstInstall;
    private String volumeName;
    private boolean serialOSGenerated;
    private String guid;
    private String pid;

    @ManyToMany(mappedBy = "usbDeviceList")
    private List<UserProfile> userAccountsList;
    private String vendorName;
    private String friendlyName;
    private String productName;
    private String revision;

    @ManyToOne
    @JoinColumn(name = "system_info_id")
    private SystemInfo systemInfo;
}