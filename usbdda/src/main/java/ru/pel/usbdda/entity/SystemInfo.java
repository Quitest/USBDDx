package ru.pel.usbdda.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
public class SystemInfo {
    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private OsInfo osInfo;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(name = "systeminfo_usbDevice",
            joinColumns = @JoinColumn(name = "systeminfo_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "usb_device_id", referencedColumnName = "id"))
    private List<USBDevice> usbDeviceList;
}
