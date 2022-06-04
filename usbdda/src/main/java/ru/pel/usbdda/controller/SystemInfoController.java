package ru.pel.usbdda.controller;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.pel.usbdda.dto.SystemInfoDto;
import ru.pel.usbdda.dto.USBDeviceDto;
import ru.pel.usbdda.entity.SystemInfo;
import ru.pel.usbdda.entity.USBDevice;
import ru.pel.usbdda.service.impl.SystemInfoServiceImpl;

import javax.websocket.server.PathParam;
import java.util.List;

@Controller
@RequestMapping("/systeminfo")
public class SystemInfoController {

    @Autowired
    SystemInfoServiceImpl service;

    @GetMapping("/page")
    @ResponseBody
    public List<SystemInfoDto> getAllSystemInfo(@PathParam("page") int page, @PathParam("size") int size,
                                                @PathParam("sortDir") String sortDir, @PathParam("sortBy") String sortBy) {
        List<SystemInfo> list = service.getSystemInfoList(page, size, sortDir, sortBy);
        return list.stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("{id}")
    public ResponseEntity<SystemInfoDto> getSystemInfo(@PathVariable long id) {
        SystemInfo sysInfo = service.getByKey(id);
        SystemInfoDto dto = toDto(sysInfo);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @ResponseBody
    public SystemInfoDto postSystemInfo(@RequestBody SystemInfoDto systemInfoDto) {
        SystemInfo sysInfo = toEntity(systemInfoDto);
        service.save(sysInfo);
        return toDto(sysInfo);
    }

    /** Преобразование в DTO и "выкусывание" информацию о вложенных SystemInfo, что бы не было зацикливания. */
    private SystemInfoDto toDto(SystemInfo entity){
        ModelMapper modelMapper = new ModelMapper();
        TypeMap<USBDevice, USBDeviceDto> typeMap = modelMapper.createTypeMap(USBDevice.class, USBDeviceDto.class);
        typeMap.addMappings(m->m.skip(USBDeviceDto::setSystemInfoList));
        return modelMapper.map(entity, SystemInfoDto.class);
    }

    private SystemInfo toEntity(SystemInfoDto dto) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper.map(dto, SystemInfo.class);
    }
}
