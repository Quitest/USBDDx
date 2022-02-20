package ru.pel.usbddc.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * Содержит имена, полученные из разных источников.
 * UsbIds - файл usb.ids;
 * reg - реестр Windows
 */
@Getter
@Setter
public abstract class AbstractName {
    /**
     * Хранит имя полученное из файла usb.ids
     * Файл можно получить по <a href="http://www.linux-usb.org/usb-ids.html">адресу</a>
     */
    protected String byUsbIds;
    /**
     * Хранит имя полученное из реестра.
     */
    protected String byRegistry;
}
