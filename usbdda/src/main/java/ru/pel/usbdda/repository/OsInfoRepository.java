package ru.pel.usbdda.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.pel.usbdda.entity.OsInfo;

@Repository
public interface OsInfoRepository extends JpaRepository<OsInfo, Long> {
}
