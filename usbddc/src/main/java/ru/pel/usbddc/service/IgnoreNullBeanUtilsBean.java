package ru.pel.usbddc.service;

import org.apache.commons.beanutils.BeanUtilsBean;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

public class IgnoreNullBeanUtilsBean extends BeanUtilsBean {
    @Override
    public void copyProperty(Object dest, String name, Object value)
            throws IllegalAccessException, InvocationTargetException {
        if (value == null || value.equals("")) return;
        if (value instanceof LocalDateTime){
            LocalDateTime dateTime = (LocalDateTime) value;
            if (dateTime.isEqual(LocalDateTime.MIN)){
                return;
            }
        }
        super.copyProperty(dest, name, value);
    }
}
