package ru.pel.usbdda.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
/*@Table(name = "user_profiles",
        uniqueConstraints = @UniqueConstraint(columnNames = {"securityId"}))*/
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id", nullable = false)
    private Long id;
    private String username;
    private String profileImagePath;
    private String securityId;

    @ManyToMany//(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "userProfile_usbDevice",
            joinColumns = @JoinColumn(name = "user_profile_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "usb_device_id", referencedColumnName = "id"))
    @JsonManagedReference
    private List<USBDevice> usbDeviceList;
}
