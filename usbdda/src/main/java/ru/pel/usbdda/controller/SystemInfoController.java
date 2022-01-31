package ru.pel.usbdda.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.pel.usbdda.dto.SystemInfoDto;
import ru.pel.usbdda.entity.SystemInfo;
import ru.pel.usbdda.service.SystemInfoServiceImpl;

import javax.websocket.server.PathParam;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/systeminfo")
public class SystemInfoController {
    @Autowired
    ModelMapper modelMapper;

    @Autowired
    SystemInfoServiceImpl service;

    @GetMapping("/page")
    @ResponseBody
    public List<SystemInfoDto> getAllSystemInfo(@PathParam("page") int page, @PathParam("size") int size,
                                                @PathParam("sortDir") String sortDir, @PathParam("sortBy") String sortBy) {
        List<SystemInfo> list = service.getSystemInfoList(page, size, sortDir, sortBy);
        return list.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("{id}")
    public ResponseEntity<SystemInfoDto> getSystemInfo(@PathVariable long id) {
        SystemInfo sysInfo = service.getByKey(id);
        SystemInfoDto dto = modelMapper.map(sysInfo, SystemInfoDto.class);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @ResponseBody
    public SystemInfoDto systemInfo(@RequestBody SystemInfoDto systemInfoDto) {
        SystemInfo sysInfo = toEntity(systemInfoDto);
        service.save(sysInfo);
        return toDto(sysInfo);
    }

    private SystemInfoDto toDto(SystemInfo entity) {
        return modelMapper.map(entity, SystemInfoDto.class);
    }

    private SystemInfo toEntity(SystemInfoDto dto) {
//        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper.map(dto, SystemInfo.class);
    }
}
