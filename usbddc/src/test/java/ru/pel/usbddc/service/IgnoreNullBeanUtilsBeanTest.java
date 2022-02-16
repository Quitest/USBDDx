package ru.pel.usbddc.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.pel.usbddc.entity.USBDevice;
import ru.pel.usbddc.entity.UserProfile;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class IgnoreNullBeanUtilsBeanTest {
    private final ArrayList<UserProfile> userList = new ArrayList<>();
    private final LocalDateTime dateTime = LocalDateTime.now();
    private final USBDevice control = new USBDevice()
            .addVolumeLabel("oldVolume")
            .setSerial("1234Serial")
            .setRevision("PSV")
            .setDateTimeFirstInstall(dateTime)
            .setVidPid("1234", "abcd")
            .setGuid("12345678-1234-1234-1234-1234567890ab")
            .setFriendlyName(null)
            .setUserAccountsList(userList);
    private final USBDevice src = new USBDevice()
            .addVolumeLabel("")
            .setSerial("1234Serial")
            .setRevision(null)
            .setDateTimeFirstInstall(LocalDateTime.MIN)
            .setVidPid("", "")
            .setGuid(null)
            .setFriendlyName(null)
            .setUserAccountsList(userList);

    @Test
    @DisplayName("Проверка корректности копирования ненулевых, непустых свойств объекта")
    void copyProperty() throws InvocationTargetException, IllegalAccessException {
        USBDevice dst = new USBDevice()
                .addVolumeLabel("oldVolume")
                .setSerial(null)
                .setRevision("PSV")
                .setDateTimeFirstInstall(dateTime)
                .setVidPid("1234", "abcd")
                .setGuid("12345678-1234-1234-1234-1234567890ab")
                .setFriendlyName(null)
                .setUserAccountsList(null);


        new IgnoreNullBeanUtilsBean().copyProperties(dst, src);

        assertThat(dst, equalTo(control));
    }
}