package ru.pel.usbdda.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.pel.usbdda.dto.SystemInfo;

@Controller
@RequestMapping("/systeminfo")
public class SystemInfoController {
    SystemInfo systemInfo;

    @GetMapping
    public ResponseEntity<SystemInfo> getSystemInfo() {
        return systemInfo == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(systemInfo);
    }

    @PostMapping
    public ResponseEntity<SystemInfo> systemInfo(@RequestBody SystemInfo systemInfo) {
        this.systemInfo = systemInfo;
        return ResponseEntity.ok(this.systemInfo);
    }
}
