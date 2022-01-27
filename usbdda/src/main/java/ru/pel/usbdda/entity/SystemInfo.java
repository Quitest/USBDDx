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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "os_info_id")
    private OsInfo osInfo;

    //    @OneToMany(mappedBy = "systemInfo", cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
//    private Map<String, USBDevice> usbDeviceMap;
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(name = "systeminfo_usbDevice",
            joinColumns = @JoinColumn(name = "systeminfo_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "usb_device_id", referencedColumnName = "id"))
    private List<USBDevice> usbDeviceList;
}
