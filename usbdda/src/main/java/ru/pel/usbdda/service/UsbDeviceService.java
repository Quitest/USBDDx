package ru.pel.usbdda.service;

import ru.pel.usbdda.entity.USBDevice;

import java.util.List;

public interface UsbDeviceService {
    USBDevice getBySerial(String serial);

    List<USBDevice> getAllDevices();

    long save(USBDevice entity);
}
