package ru.pel.usbdda.service;

import ru.pel.usbdda.entity.USBDevice;

import java.util.List;
import java.util.Optional;

public interface UsbDeviceService {
    Optional<USBDevice> getBySerial(String serial);

    List<USBDevice> getAllDevices();

    long save(USBDevice entity);
}
