package ru.pel.usbdda.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pel.usbdda.entity.OsInfo;
import ru.pel.usbdda.entity.SystemInfo;
import ru.pel.usbdda.entity.USBDevice;
import ru.pel.usbdda.repository.SystemInfoRepository;
import ru.pel.usbdda.service.SystemInfoService;

import java.util.List;

@Service
public class SystemInfoServiceImpl implements SystemInfoService {
    //https://www.baeldung.com/jpa-get-auto-generated-id
// пыатемся работать по статье
    @Autowired
    SystemInfoRepository systemInfoRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemInfoServiceImpl.class);

    @Override
    public SystemInfo getByKey(long key) {
        return systemInfoRepository.findById(key).orElseThrow();
    }

    public List<SystemInfo> getSystemInfoList(int page, int size, String sortDir, String sortBy) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.fromString(sortDir), sortBy);
        Page<SystemInfo> systemInfoPage = systemInfoRepository.findAll(pageRequest);
        return systemInfoPage.getContent();
    }

    @Transactional
    public long save(SystemInfo systemInfo) {
        //устанавливаем обратные связи сущностей (для записи внешних ключей в БД)...
        OsInfo osInfo = systemInfo.getOsInfo();
        osInfo.getNetworkInterfaceList()
                .forEach(networkInterface -> {
                    networkInterface.getInetAddressList()
                            .forEach(inetAddress -> inetAddress.setNetworkInterface(networkInterface)); //... тут IP адреса -> сетевойИнтерфейс,
                    networkInterface.setOsInfo(osInfo); //а тут сетевойИнтерфейс -> операционнаяСистема.
                });

        List<USBDevice> usbDeviceList = systemInfo.getUsbDeviceList();
        usbDeviceList.stream()
                .forEach(usbDevice -> {
//                    usbDevice.getUserProfileList()
//                            .forEach(userProfile -> userProfile.setUsbDeviceList(usbDeviceList));
                    usbDevice.addSystemInfo(systemInfo);
                    usbDevice.getUserProfileList().forEach(userProfile -> userProfile.setUsbDeviceList(usbDeviceList));//FIXME не set, а add надо
//                    usbDevice.addUserProfileList();
//                    usbDevice.addSystemInfo(systemInfo);

                });



        systemInfoRepository.save(systemInfo);
        LOGGER.debug("""
                Сохранено в БД: 
                \tUSBDevice: {}
                \tСписок пользователей{}""",
                "149*",systemInfo.getUsbDeviceList().stream().filter(d->d.getSerial().contains("149")).findFirst().get().getUserProfileList());
        return systemInfo.getId();
    }
}
