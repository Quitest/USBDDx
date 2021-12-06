package ru.pel.usbddc.entity;

import lombok.Getter;

import java.util.Map;

/**
 * Описывает типовое USB устройство. В реестре имеются ветки USB, USBSTOR, USBPRINT, в которых очень похожие наборы
 * параметров.
 */
@Getter
public abstract class Device {
    protected String compatibleIDs;    //REG_MULTI_SZ    CID_MS_GENERICPRINT
    /**
     * <a href=https://docs.microsoft.com/en-us/windows-hardware/drivers/install/how-usb-devices-are-assigned-container-ids>
     * О том как формируется ContainerID в ОС Windows</a>
     */
//    protected String containerID;    //REG_SZ    {eaa0e959-0e69-5e4d-bc63-327399048f59}
    protected String deviceDesc;   //REG_SZ    HP LaserJet 1020
    protected String hardwareID;    //REG_MULTI_SZ    USBPRINT\Hewlett-PackardHP_La26DD\0Hewlett-PackardHP_La26DD
    protected String serial;
    protected boolean isSerialOSGenerated;
    protected String vendorName;
    protected String productName;
    protected Map<String, String> properties;
//    protected String mfg;    //REG_SZ    @oem30.inf,%mfg%;HP


}
