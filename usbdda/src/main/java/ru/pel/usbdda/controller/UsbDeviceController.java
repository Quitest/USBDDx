package ru.pel.usbdda.controller;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.pel.usbdda.dto.SystemInfoDto;
import ru.pel.usbdda.dto.USBDeviceDto;
import ru.pel.usbdda.entity.SystemInfo;
import ru.pel.usbdda.entity.USBDevice;
import ru.pel.usbdda.service.UsbDeviceService;

import javax.websocket.server.PathParam;

@Controller
@RequestMapping("api/usbdevices")
public class UsbDeviceController {
    @Autowired
    private UsbDeviceService usbDeviceService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping
    @ResponseBody
    public USBDeviceDto getUsbDeviceBySerial(@PathParam("serial") String serial) {
        USBDevice device = usbDeviceService.getBySerial(serial);
        return toDto(device);
    }

    private USBDeviceDto toDto(USBDevice entity) {
        ModelMapper mapper = new ModelMapper();
        //избавляемся от замкнутого цикла по выборке сведений usbdevice->systeminfo->usbdevice->...
        TypeMap<SystemInfo, SystemInfoDto> typeMap = mapper.createTypeMap(SystemInfo.class, SystemInfoDto.class);
        typeMap.addMappings(m -> m.skip(SystemInfoDto::setUsbDeviceList));

        return mapper.map(entity, USBDeviceDto.class);
    }

    private USBDevice toEntity(USBDeviceDto dto) {
        return modelMapper.map(dto, USBDevice.class);
    }
}
