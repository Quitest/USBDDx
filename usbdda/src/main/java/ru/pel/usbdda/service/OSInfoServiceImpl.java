package ru.pel.usbdda.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pel.usbdda.entity.OsInfo;
import ru.pel.usbdda.repository.OsInfoRepository;

@Service
public class OSInfoServiceImpl implements OsInfoService {
    @Autowired
    OsInfoRepository osInfoRepository;

    @Transactional
    public long save(OsInfo osInfo) {
        osInfo.getNetworkInterfaceList().forEach(i -> i.setOsInfo(osInfo));
        osInfoRepository.save(osInfo);
        return osInfo.getId();
    }
}
