package ru.pel.usbdda.service;

import ru.pel.usbdda.entity.SystemInfo;

public interface SystemInfoService {
    SystemInfo getByKey(long key);
    long save(SystemInfo entity);
}
