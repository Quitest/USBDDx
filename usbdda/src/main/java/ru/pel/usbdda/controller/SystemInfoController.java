package ru.pel.usbdda.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.pel.usbdda.dto.SystemInfoDto;
import ru.pel.usbdda.entity.SystemInfo;
import ru.pel.usbdda.service.SystemInfoService;

import javax.persistence.EntityManager;
import javax.websocket.server.PathParam;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/systeminfo")
public class SystemInfoController {
    SystemInfo systemInfo;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    SystemInfoService service;

    @GetMapping("{id}")
    public ResponseEntity<SystemInfoDto> getSystemInfo(@PathVariable long id) {
        SystemInfo systemInfo = service.getByKey(id);
        return ResponseEntity.ok(modelMapper.map(systemInfo,SystemInfoDto.class));
    }

    @GetMapping("/all")
    public List<SystemInfoDto> getAllSystemInfo(@PathParam("page") int page, @PathParam("size") int size,
                                                @PathParam("sortDir") String sortDir, @PathParam("sortBy") String sortBy){
        List<SystemInfo> list = service.getSystemInfoList(page, size, sortDir, sortBy);
        return list.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<SystemInfo> systemInfo(@RequestBody SystemInfo systemInfo) {
        this.systemInfo = systemInfo;
        service.save(this.systemInfo);
        return ResponseEntity.ok(this.systemInfo);
    }

    private SystemInfoDto toDto(SystemInfo entity){
        return modelMapper.map(entity, SystemInfoDto.class);
    }

    private SystemInfo toEntity(SystemInfoDto dto){
        return modelMapper.map(dto, SystemInfo.class);
    }
}
