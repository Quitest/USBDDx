package ru.pel.usbddc.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * Описывает типовое USB устройство. В реестре имеются ветки USB, USBSTOR, USBPRINT, в которых очень похожие наборы
 * параметров.
 */

public abstract class Device {
    @Getter
    @Setter
    protected String capabilities;    //REG_DWORD    0xc0
    @Getter
    @Setter
    protected String classGUID;    //REG_SZ    {4d36e979-e325-11ce-bfc1-08002be10318}
    @Getter
    @Setter
    protected String compatibleIDs;    //REG_MULTI_SZ    CID_MS_GENERICPRINT
    @Getter
    @Setter
    protected String configFlags;    //REG_DWORD    0x0
    /**
     * <a href=https://docs.microsoft.com/en-us/windows-hardware/drivers/install/how-usb-devices-are-assigned-container-ids>
     *     О том как формируется ContainerID в ОС Windows</a>
     */
    @Getter
    @Setter
    protected String containerID;    //REG_SZ    {eaa0e959-0e69-5e4d-bc63-327399048f59}
    @Getter
    @Setter
    protected String deviceDesc;   //REG_SZ    HP LaserJet 1020
    @Getter
    @Setter
    protected String driver;    //REG_SZ    {4d36e979-e325-11ce-bfc1-08002be10318}\0000
    @Getter
    @Setter
    protected String hardwareID;    //REG_MULTI_SZ    USBPRINT\Hewlett-PackardHP_La26DD\0Hewlett-PackardHP_La26DD
    @Getter
    @Setter
    protected String serial;
    @Setter
    @Getter
    protected String vendorName;
    @Setter
    @Getter
    protected String productName;
    @Getter
    @Setter
    protected String mfg;    //REG_SZ    @oem30.inf,%mfg%;HP

    public abstract boolean determineVendorName();
    public abstract boolean determineProductName();


}
