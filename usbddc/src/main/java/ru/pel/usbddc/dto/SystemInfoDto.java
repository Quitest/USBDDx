package ru.pel.usbddc.dto;

import lombok.Getter;
import lombok.Setter;
import ru.pel.usbddc.entity.OSInfo;
import ru.pel.usbddc.entity.SystemInfo;
import ru.pel.usbddc.entity.USBDevice;

import java.util.List;

@Getter
@Setter
public class SystemInfoDto {
    private OSInfo osInfo;
    private String uuid;
    private String comment;
    private List<USBDevice> usbDeviceList;
    private boolean isScannedWithAdminPrivileges;

    public SystemInfoDto(final SystemInfo systemInfo) {
        this.osInfo = systemInfo.getOsInfo();
        this.uuid = systemInfo.getUuid();
        this.comment = systemInfo.getComment();
        this.isScannedWithAdminPrivileges = systemInfo.isScannedWithAdminPrivileges();
        this.usbDeviceList = systemInfo.getUsbDeviceMap().values().parallelStream().toList();
    }

    public SystemInfoDto() {
    }
}
