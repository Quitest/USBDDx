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
    private OSInfo osInfo;

    @OneToMany(mappedBy = "systemInfo")
//    private Map<String, USBDevice> usbDeviceMap;
    private List<USBDevice> usbDeviceList;
}
