package ru.pel.usbdda.service;

import ru.pel.usbdda.entity.SystemInfo;

import java.util.Optional;

public interface SystemInfoService {
    Optional<SystemInfo> getByKey(long key);
    SystemInfo save(SystemInfo entity);
}
