package ru.pel.usbdda.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pel.usbdda.entity.SystemInfo;
import ru.pel.usbdda.repository.SystemInfoRepository;

import javax.persistence.EntityManager;
import java.util.List;

@Service
public class SystemInfoServiceImpl implements SystemInfoService {
    //https://www.baeldung.com/jpa-get-auto-generated-id
// пыатемся работать по статье
    EntityManager entityManager;

    @Autowired
    SystemInfoRepository systemInfoRepository;

    public SystemInfoServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public SystemInfo getByKey(long key) {
        return entityManager.find(SystemInfo.class, key);
    }

    public List<SystemInfo> getSystemInfoList(int page, int size, String sortDir, String sortBy) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.fromString(sortDir), sortBy);
        Page<SystemInfo> systemInfoPage = systemInfoRepository.findAll(pageRequest);
        return systemInfoPage.getContent();
    }

    @Transactional
    public long save(SystemInfo systemInfo) {
        entityManager.persist(systemInfo);
        return systemInfo.getId();
    }
}
