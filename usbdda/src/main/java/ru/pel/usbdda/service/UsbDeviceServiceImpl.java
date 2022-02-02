package ru.pel.usbdda.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pel.usbdda.entity.USBDevice;
import ru.pel.usbdda.repository.UsbDeviceRepository;

import javax.persistence.EntityManager;

@Service
public class UsbDeviceServiceImpl implements UsbDeviceService {
    @Autowired
    UsbDeviceRepository repository;
    private EntityManager entityManager;

    public UsbDeviceServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public USBDevice getBySerial(String serial) {
        return repository.findBySerial(serial);
    }

    @Transactional
    public long save(USBDevice usbDevice) {
        entityManager.persist(usbDevice);
        return usbDevice.getId();
    }
}
