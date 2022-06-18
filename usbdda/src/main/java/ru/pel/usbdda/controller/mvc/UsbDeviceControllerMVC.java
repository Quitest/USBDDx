package ru.pel.usbdda.controller.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.pel.usbdda.entity.USBDevice;
import ru.pel.usbdda.service.UsbDeviceService;

import java.util.List;

@Controller
@RequestMapping("/usbdevices")
public class UsbDeviceControllerMVC {
    @Autowired
    private UsbDeviceService usbDeviceService;

    @GetMapping("/{serial}")
    public String getDeviceInfoBySerial(Model model, @PathVariable("serial") String serial){
        USBDevice device = usbDeviceService.getBySerial(serial);
        model.addAttribute("device", device);
        return "devices/device-by-serial";
    }

    @GetMapping("/all")
    public String getAllDevices(Model model){
        List<USBDevice> deviceList = usbDeviceService.getAllDevices();
        model.addAttribute("deviceList", deviceList);
        return "devices/all-devices";
    }
}
