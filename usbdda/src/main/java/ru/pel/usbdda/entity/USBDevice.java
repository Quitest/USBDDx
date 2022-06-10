package ru.pel.usbdda.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class USBDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vid;
    private String serial;
    private LocalDateTime dateTimeFirstInstall;
    private String volumeName;
    private boolean serialOSGenerated;
    private String guid;
    private String pid;

    @ManyToMany(mappedBy = "usbDeviceList", cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private List<UserProfile> userProfileList;
    private String vendorName;
    private String friendlyName;
    private String productName;
    private String revision;
    private String vendorNameByRegistry;
    private String diskId;
    private String productNameByRegistry;

    //    @ManyToOne
//    @JoinColumn(name = "system_info_id")
//    private SystemInfo systemInfo;
    @ManyToMany(mappedBy = "usbDeviceList")
    private List<SystemInfo> systemInfoList;

    public void addSystemInfo(SystemInfo systemInfo) {
        if (systemInfoList == null) {
            systemInfoList = new ArrayList<>();
        }
        systemInfoList.add(systemInfo);
    }

    //    private List<OsInfo> osInfoList;
    public void addUserProfile(UserProfile userProfile) {
        if (userProfileList == null) {
            userProfileList = new ArrayList<>();
        }
        userProfileList.add(userProfile);
    }

    public void addUserProfileList(List<UserProfile> userProfileList) {
        this.userProfileList.addAll(userProfileList);
    }
}