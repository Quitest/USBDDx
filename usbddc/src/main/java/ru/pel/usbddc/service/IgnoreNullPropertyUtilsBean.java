package ru.pel.usbddc.service;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

public class IgnoreNullPropertyUtilsBean extends PropertyUtilsBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(IgnoreNullPropertyUtilsBean.class);

    @Override
    public void copyProperties(Object dest, Object orig) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        LOGGER.debug("Начали копировать свойства");
        super.copyProperties(dest, orig);
        LOGGER.debug("Закончили копировать свойства");
    }

    @Override
    public void setSimpleProperty(Object bean, String name, Object value) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        LOGGER.debug("Началась проверка поля {} со значением {}",name,value);
        if (value == null || value.equals("")) return;
        if (value instanceof LocalDateTime dateTime){
            if (dateTime.isEqual(LocalDateTime.MIN)){
                return;
            }
        }
        super.setSimpleProperty(bean, name, value);
        LOGGER.debug("Скопировано поле {} со значением {}", name,value);
    }
}
