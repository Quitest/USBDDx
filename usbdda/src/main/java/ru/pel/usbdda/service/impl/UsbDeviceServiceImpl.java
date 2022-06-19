package ru.pel.usbdda.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pel.usbdda.entity.USBDevice;
import ru.pel.usbdda.repository.UsbDeviceRepository;
import ru.pel.usbdda.service.UsbDeviceService;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Service
public class UsbDeviceServiceImpl implements UsbDeviceService {
    @Autowired
    private UsbDeviceRepository repository;
    private EntityManager entityManager;

    public UsbDeviceServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<USBDevice> getAllDevices() {
        return repository.findAll();
    }

    @Override
    public Optional<USBDevice> getBySerial(String serial) {
        return repository.findBySerial(serial);
    }

    @Transactional
    public long save(USBDevice usbDevice) {
        entityManager.persist(usbDevice);
        return usbDevice.getId();
    }
}
