package ru.pel.usbddc.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public class SystemInfo {
    private OSInfo osInfo = new OSInfo();
    private String uuid = "";
    private String comment = "";
    private Map<String, USBDevice> usbDeviceMap = new HashMap<>();
    /**
     * True - если запуск производится из-под учетки админа и при наличии расширенных полномочий - "Запустить от имени администратора".<br>
     * False - в остальных случаях.
     */
    private boolean isScannedWithAdminPrivileges;

    public SystemInfo() {
        //Во избежание утраты конструктора по умолчанию при создании в будущем конструктора с параметрами.
    }

    /**
     * Выполнить слияние информации о USBDevice. Свойства, не равные null и не пустые, копируются в текущий объект из
     * источника.
     *
     * @param src источник данных для слияния.
     * @return текущий объект с дополненными данными.
     */
    public SystemInfo mergeUsbDeviceInfo(Map<String, USBDevice> src) {
        usbDeviceMap = Stream.of(usbDeviceMap, src)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        USBDevice::mergeProperties
                ));
        return this;
    }
}
