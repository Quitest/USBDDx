package ru.pel.usbdda.service;

import org.springframework.beans.factory.annotation.Autowired;
import ru.pel.usbdda.entity.SystemInfo;
import ru.pel.usbdda.repository.SystemInfoRepository;

import java.util.List;

public class SystemInfoService {
    @Autowired
    SystemInfoRepository repository;

    public void save(SystemInfo systemInfo) {
        SystemInfo entity = new SystemInfo();
        repository.save(systemInfo);
    }

    public List<SystemInfo> getAll(){
        return repository.findAll();
    }
}
