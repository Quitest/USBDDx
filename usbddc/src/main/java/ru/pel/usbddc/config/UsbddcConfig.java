package ru.pel.usbddc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class UsbddcConfig extends UsbddcDefaultConfig{
    private static final Logger logger = LoggerFactory.getLogger(UsbddcConfig.class);

    public UsbddcConfig(String pathToConfig){

        try {
            Properties config = new Properties();
            config.loadFromXML(new FileInputStream(pathToConfig));
            threadPoolSize = Integer.parseInt(config.getProperty("threadPoolSize"));
            usbIdsPath = config.getProperty("usbIdsPath");

        } catch (IOException e) {
            logger.warn("Не удалось загрузить конфигурацию из файла {}. Используется конфигурация по умолчанию. Причина: {}",
                    pathToConfig, e.getLocalizedMessage());
        }
    }
}
