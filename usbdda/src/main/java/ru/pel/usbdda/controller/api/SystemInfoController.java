package ru.pel.usbdda.controller.api;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.pel.usbdda.dto.SystemInfoDto;
import ru.pel.usbdda.dto.USBDeviceDto;
import ru.pel.usbdda.entity.SystemInfo;
import ru.pel.usbdda.entity.USBDevice;
import ru.pel.usbdda.exception.NoSuchSystemInfoException;
import ru.pel.usbdda.model.assembler.SystemInfoModelAssembler;
import ru.pel.usbdda.service.impl.SystemInfoServiceImpl;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/systeminfo")
public class SystemInfoController {

    @Autowired
    private SystemInfoServiceImpl service;
    @Autowired
    private PagedResourcesAssembler<SystemInfo> pagedResourcesAssembler;
    @Autowired
    private SystemInfoModelAssembler assembler;

    @GetMapping("/page")
    public PagedModel<EntityModel<SystemInfo>> getAllSystemInfo(Pageable page) {
        Page<SystemInfo> systemInfoList = service.getSystemInfoList(page);
        return pagedResourcesAssembler.toModel(systemInfoList, assembler);
    }

    @GetMapping("/{id}")
    public EntityModel<SystemInfo> getSystemInfo(@PathVariable long id) {
        SystemInfo sysInfo = service.getByKey(id).orElseThrow(()->new NoSuchSystemInfoException("Не существует системы с id = " + id));
        return assembler.toModel(sysInfo);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntityModel<SystemInfo> postSystemInfo(@RequestBody SystemInfoDto systemInfoDto) {
        SystemInfo sysInfo = toEntity(systemInfoDto);
        return assembler.toModel(service.save(sysInfo));
    }

    /**
     * Преобразование в DTO и "выкусывание" информацию о вложенных SystemInfo, что бы не было зацикливания.
     */
    private SystemInfoDto toDto(SystemInfo entity) {
        ModelMapper modelMapper = new ModelMapper();
        TypeMap<USBDevice, USBDeviceDto> typeMap = modelMapper.createTypeMap(USBDevice.class, USBDeviceDto.class);
        typeMap.addMappings(m -> m.skip(USBDeviceDto::setSystemInfoList));
        return modelMapper.map(entity, SystemInfoDto.class);
    }

    private SystemInfo toEntity(SystemInfoDto dto) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper.map(dto, SystemInfo.class);
    }
}
