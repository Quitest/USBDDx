package ru.pel.usbdda.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.pel.usbdda.entity.SystemInfo;
import ru.pel.usbdda.repository.SystemInfoRepository;

import java.util.List;

@Service
public class SystemInfoService {
    @Autowired
    SystemInfoRepository repository;

    public void save(SystemInfo systemInfo) {
        repository.save(systemInfo);
    }

    public List<SystemInfo> getAll(){
        return repository.findAll();
    }
}
