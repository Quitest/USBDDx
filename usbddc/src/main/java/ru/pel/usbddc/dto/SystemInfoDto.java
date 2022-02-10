package ru.pel.usbddc.dto;

import lombok.Getter;
import lombok.Setter;
import ru.pel.usbddc.entity.OSInfo;
import ru.pel.usbddc.entity.SystemInfo;
import ru.pel.usbddc.entity.USBDevice;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class SystemInfoDto {
    private OSInfo osInfo;
    private String uuid;
    private List<USBDevice> usbDeviceList;

    public SystemInfoDto(final SystemInfo systemInfo){
        this.osInfo = systemInfo.getOsInfo();
        this.uuid = systemInfo.getUuid();
        this.usbDeviceList = systemInfo.getUsbDeviceMap().values().parallelStream().collect(Collectors.toList());
    }

    public SystemInfoDto(){}
}
