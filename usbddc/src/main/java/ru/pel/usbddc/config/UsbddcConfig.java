package ru.pel.usbddc.config;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

/**
 * Конфигурационный класс.
 * <p>
 * Данные берет из файла, указанного при инициализации или файла по умолчанию.
 * Если файл недоступен или по каким-либо причинам не удалось прочитать данные, то используется конфигурация по умолчанию.
 */
@Getter
@Setter
public class UsbddcConfig {
    private static final Logger logger = LoggerFactory.getLogger(UsbddcConfig.class);
    //TODO а нужен ли тут вообще синглтон, может все поля перегнать в статик и класс сделать utility?
    private static volatile UsbddcConfig instance;
    private int threadPoolSize;
    private String usbIdsPath;

    {
        threadPoolSize = 8;
        usbIdsPath = "usb.ids";
    }

    private UsbddcConfig(String pathToConfig) {

        try {
            Properties config = new Properties();
            FileInputStream file = new FileInputStream(pathToConfig);
            config.loadFromXML(file);
            threadPoolSize = Integer.parseInt(config.getProperty("threadPoolSize"));
            usbIdsPath = config.getProperty("usbIdsPath");

        } catch (IOException e) {
            logger.warn("Используется конфигурация по умолчанию, т.к. не удалось загрузить конфигурацию из файла {}. Причина: {}",
                    pathToConfig, e.getLocalizedMessage());
        }
    }

    /**
     * Получить конфигурацию из указанного файла.
     *
     * @param pathToConfig XML-файл конфигурации.
     * @return инстанс конфига
     */
    public static UsbddcConfig getInstance(String pathToConfig) {
        UsbddcConfig result = instance;
        if (result != null) {
            return result;
        }

        synchronized (UsbddcConfig.class) {
            if (instance == null) {
                instance = new UsbddcConfig(pathToConfig);
            }

            return instance;
        }
    }

    /**
     * Получить конфигурацию из файла по умолчанию - config.xml
     *
     * @return инстанс конфига
     */
    public static UsbddcConfig getInstance() {
        String rootPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(""))
                .getPath();
        return getInstance(rootPath + "config.xml");
    }
}
