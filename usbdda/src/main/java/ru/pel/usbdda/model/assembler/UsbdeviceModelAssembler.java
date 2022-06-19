package ru.pel.usbdda.model.assembler;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import ru.pel.usbdda.controller.api.UsbDeviceController;
import ru.pel.usbdda.dto.USBDeviceDto;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UsbdeviceModelAssembler implements RepresentationModelAssembler<USBDeviceDto, EntityModel<USBDeviceDto>> {
    @Override
    public EntityModel<USBDeviceDto> toModel(USBDeviceDto usbDevice) {
        return EntityModel.of(usbDevice,
                linkTo(methodOn(UsbDeviceController.class).getUsbDeviceBySerial(usbDevice.getSerial())).withSelfRel(),
                linkTo(methodOn(UsbDeviceController.class).getAllDevices()).withRel("usbDevices"));
    }
}
