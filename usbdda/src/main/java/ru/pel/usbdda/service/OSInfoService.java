package ru.pel.usbdda.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pel.usbdda.entity.OsInfo;

import javax.persistence.EntityManager;

@Service
public class OSInfoService {
    EntityManager entityManager;

    public OSInfoService(EntityManager entityManager){
        this.entityManager = entityManager;
    }

    @Transactional
    public long save(OsInfo osInfo){
        entityManager.persist(osInfo);
        return osInfo.getId();
    }
}
