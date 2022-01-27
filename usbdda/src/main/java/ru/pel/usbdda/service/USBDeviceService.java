package ru.pel.usbdda.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pel.usbdda.entity.USBDevice;

import javax.persistence.EntityManager;

@Service
public class USBDeviceService {
    private EntityManager entityManager;

    public USBDeviceService(EntityManager entityManager){
        this.entityManager = entityManager;
    }

    @Transactional
    public long save(USBDevice usbDevice){
        entityManager.persist(usbDevice);
        return usbDevice.getId();
    }
}
