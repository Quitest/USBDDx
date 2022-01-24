package ru.pel.usbdda.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pel.usbdda.entity.SystemInfo;
import ru.pel.usbdda.repository.SystemInfoRepository;

import javax.persistence.EntityManager;
import java.util.List;

@Service
public class SystemInfoService {
    //https://www.baeldung.com/jpa-get-auto-generated-id
// пыатемся работать по статье
    EntityManager entityManager;

    @Autowired
    SystemInfoRepository repository;

    public SystemInfoService(EntityManager entityManager){
        this.entityManager = entityManager;
    }

    public List<SystemInfo> getAll() {
        return repository.findAll();
    }

    @Transactional
    public long save(SystemInfo systemInfo) {
//        repository.save(systemInfo);
        entityManager.persist(systemInfo);
        return systemInfo.getId();
    }
}
