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
    private final USBDevice control = USBDevice.getBuilder()
            .withVolumeName("oldVolume")
            .withSerial("1234Serial")
            .withRevision("PSV")
            .withDateTimeFirstInstall(dateTime)
            .withVidPid("1234", "abcd")
            .withGuid("12345678-1234-1234-1234-1234567890ab")
            .withFriendlyName(null)
            .withUserProfileList(userList)
            .build();
    private final USBDevice src = USBDevice.getBuilder()
            .withVolumeName("")
            .withSerial("1234Serial")
            .withRevision(null)
            .withDateTimeFirstInstall(LocalDateTime.MIN)
            .withVidPid("", "")
            .withGuid(null)
            .withFriendlyName(null)
            .withUserProfileList(userList)
            .build();

    @Test
    @DisplayName("Проверка корректности копирования ненулевых, непустых свойств объекта")
    void copyProperty() throws InvocationTargetException, IllegalAccessException {
        USBDevice dst = USBDevice.getBuilder()
                .withVolumeName("oldVolume")
                .withSerial(null)
                .withRevision("PSV")
                .withDateTimeFirstInstall(dateTime)
                .withVidPid("1234", "abcd")
                .withGuid("12345678-1234-1234-1234-1234567890ab")
                .withFriendlyName(null)
                .withUserProfileList(null)
                .build();


        new IgnoreNullBeanUtilsBean().copyProperties(dst, src);

        assertThat(dst, equalTo(control));
    }
}