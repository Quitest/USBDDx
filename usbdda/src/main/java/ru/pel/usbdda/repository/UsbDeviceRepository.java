package ru.pel.usbdda.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.pel.usbdda.entity.USBDevice;

import java.util.Optional;

@Repository
public interface UsbDeviceRepository extends JpaRepository<USBDevice, Long> {
    Optional<USBDevice> findBySerial(String serial);
}
