package ru.pel.usbdda.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pel.usbdda.entity.OsInfo;
import ru.pel.usbdda.entity.SystemInfo;
import ru.pel.usbdda.repository.SystemInfoRepository;

import javax.persistence.EntityManager;
import java.util.List;

@Service
public class SystemInfoService implements ISystemInfoService{
    //https://www.baeldung.com/jpa-get-auto-generated-id
// пыатемся работать по статье
    EntityManager entityManager;

    @Autowired
    SystemInfoRepository systemInfoRepository;

    public SystemInfoService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public SystemInfo getByKey(long key) {
//        return entityManager.find(SystemInfo.class, key);
        SystemInfo systemInfo = entityManager.find(SystemInfo.class, key);
        OsInfo osInfo = systemInfo.getOsInfo();
        System.out.println(osInfo.getUsername());
        return systemInfo;
    }

    public List<SystemInfo> getSystemInfoList(int page, int size, String sortDir, String sortBy) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.fromString(sortDir), sortBy);

        Page<SystemInfo> systemInfoPage = systemInfoRepository.findAll(pageRequest);

        return  systemInfoPage.getContent();
    }

    @Transactional
    public long save(SystemInfo systemInfo) {
        entityManager.persist(systemInfo);
        return systemInfo.getId();
    }
}
