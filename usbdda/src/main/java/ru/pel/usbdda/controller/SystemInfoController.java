package ru.pel.usbdda.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.pel.usbdda.dto.SystemInfoDto;
import ru.pel.usbdda.entity.SystemInfo;
import ru.pel.usbdda.service.SystemInfoService;

import java.util.List;

@Controller
@RequestMapping("/systeminfo")
public class SystemInfoController {
    SystemInfo systemInfo;

    @Autowired
    SystemInfoService service;

    @GetMapping
    public ResponseEntity<SystemInfo> getSystemInfo() {
        return systemInfo == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(systemInfo);
    }

    @PostMapping
    public ResponseEntity<List<SystemInfo>> systemInfo(@RequestBody SystemInfo systemInfo) {
//        this.systemInfo = systemInfo;
        service.save(systemInfo);

        List<SystemInfo> all = service.getAll();

        return ResponseEntity.ok(all);
    }
}