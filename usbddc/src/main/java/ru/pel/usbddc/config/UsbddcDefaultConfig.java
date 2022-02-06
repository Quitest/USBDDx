package ru.pel.usbddc.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsbddcDefaultConfig {
    protected int threadPoolSize;
    protected String usbIdsPath;

    {
        threadPoolSize = 8;
        usbIdsPath = "";
    }

    public UsbddcDefaultConfig() {
//        throw new IllegalStateException("Utility class");
    }
}
