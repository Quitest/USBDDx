package ru.pel.usbdda.controller;

import org.springframework.web.bind.annotation.GetMapping;
import ru.pel.usbdda.dto.USBDeviceDto;

import javax.websocket.server.PathParam;

public class UsbDeviceController {
    @GetMapping
    public USBDeviceDto getUsbDeviceBySerial(@PathParam("serial") String serial) {

    }
}
