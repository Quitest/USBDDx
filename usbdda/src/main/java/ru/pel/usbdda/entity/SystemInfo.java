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

    private String uuid;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private OsInfo osInfo;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(name = "systeminfo_usbDevice",
            joinColumns = @JoinColumn(name = "systeminfo_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "usb_device_id", referencedColumnName = "id"))
    private List<USBDevice> usbDeviceList;

    /**
     * True - если запуск производится из-под учетки админа и при наличии расширенных полномочий - "Запустить от имени администратора".<br>
     * False - в остальных случаях.
     */
    private boolean isScannedWithAdminPrivileges;
}
