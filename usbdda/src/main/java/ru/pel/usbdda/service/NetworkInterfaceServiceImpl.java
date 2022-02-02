package ru.pel.usbdda.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.pel.usbdda.entity.NetworkInterface;
import ru.pel.usbdda.repository.NetworkInterfaceRepository;

@Service
public class NetworkInterfaceServiceImpl implements NetworkInterfaceService {
    @Autowired
    NetworkInterfaceRepository repository;

    @Override
    public NetworkInterface save(ru.pel.usbdda.entity.NetworkInterface entity) {
        return repository.save(entity);
    }
}
