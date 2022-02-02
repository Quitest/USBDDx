package ru.pel.usbdda.service;

import ru.pel.usbdda.entity.USBDevice;

public interface UsbDeviceService {
    USBDevice getBySerial(String serial);
    long save(USBDevice entity);
}
