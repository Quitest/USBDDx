package ru.pel.usbdda.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.pel.usbdda.entity.NetworkInterface;

@Repository
public interface NetworkInterfaceRepository extends JpaRepository<NetworkInterface, Long> {
}
