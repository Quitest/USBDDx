package ru.pel.usbddc.service;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

@Deprecated
public class IgnoreNullBeanUtilsBean extends BeanUtilsBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(IgnoreNullBeanUtilsBean.class);

    /**
     * @deprecated Метод не работает, если используются цепные сеттеры (chain setters).
     * @param dest
     * @param name
     * @param value
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    @Override
    @Deprecated
    public void copyProperty(Object dest, String name, Object value)
            throws IllegalAccessException, InvocationTargetException {
        LOGGER.debug("Началась проверка поля {} со значением {}",name,value);
        if (value == null || value.equals("")) return;
        if (value instanceof LocalDateTime){
            LocalDateTime dateTime = (LocalDateTime) value;
            if (dateTime.isEqual(LocalDateTime.MIN)){
                return;
            }
        }
        super.copyProperty(dest, name, value);
        LOGGER.debug("Скопировано поле {} со значением {}", name,value);
    }
}
