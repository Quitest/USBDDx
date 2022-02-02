package ru.pel.usbdda.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.pel.usbdda.entity.SystemInfo;

@Repository
public interface SystemInfoRepository extends JpaRepository<SystemInfo, Long> {
}
