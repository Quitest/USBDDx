package ru.pel.usbdda.controller.api;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;
import ru.pel.usbdda.dto.SystemInfoDto;
import ru.pel.usbdda.dto.USBDeviceDto;
import ru.pel.usbdda.entity.SystemInfo;
import ru.pel.usbdda.entity.USBDevice;
import ru.pel.usbdda.service.UsbDeviceService;
import ru.pel.usbdda.model.assembler.UsbdeviceModelAssembler;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("api/usbdevices")
public class UsbDeviceController {
    @Autowired
    private UsbDeviceService usbDeviceService;
    @Autowired
    private UsbdeviceModelAssembler assembler;
    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/{serial}")
    public USBDeviceDto getUsbDeviceBySerial(@PathVariable("serial") String serial) {
        USBDevice device = usbDeviceService.getBySerial(serial);
        return toDto(device);
    }

    @GetMapping("/all")
    public CollectionModel<EntityModel<USBDeviceDto>> getAllDevices(){
        List<EntityModel<USBDeviceDto>> entityModels = usbDeviceService.getAllDevices().stream()
                .map(UsbDeviceController::toDto)
                .map(assembler::toModel)
                .toList();
        return CollectionModel.of(entityModels,
                linkTo(methodOn(UsbDeviceController.class).getAllDevices()).withSelfRel());
    }

    private static USBDeviceDto toDto(USBDevice entity) {
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
