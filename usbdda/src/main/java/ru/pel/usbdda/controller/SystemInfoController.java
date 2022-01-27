package ru.pel.usbdda.controller;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.MatchingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.pel.usbdda.dto.SystemInfoDto;
import ru.pel.usbdda.entity.SystemInfo;
import ru.pel.usbdda.service.SystemInfoService;

import javax.websocket.server.PathParam;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/systeminfo")
public class SystemInfoController {
    @Autowired
    ModelMapper modelMapper;

    @Autowired
    SystemInfoService service;

    @GetMapping("/page")
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
    public SystemInfo systemInfo(@RequestBody SystemInfoDto systemInfoDto) {
        SystemInfo sysInfo = toEntity(systemInfoDto);
        service.save(sysInfo);
        return sysInfo;
    }

    private SystemInfoDto toDto(SystemInfo entity) {
        return modelMapper.map(entity, SystemInfoDto.class);
    }

    private SystemInfo toEntity(SystemInfoDto dto) {
//        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper.map(dto, SystemInfo.class);
    }
}
