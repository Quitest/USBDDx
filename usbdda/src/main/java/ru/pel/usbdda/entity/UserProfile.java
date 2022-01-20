package ru.pel.usbdda.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
public class UserProfile {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    private String username;
    private String profileImagePath;
    private String securityId;

    @ManyToMany
    @JoinTable(name = "userProfile_usbDevice",
            joinColumns = @JoinColumn(name = "user_profile_id",referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "usb_device_id",referencedColumnName = "id"))
    private List<USBDevice> usbDeviceList;
}
