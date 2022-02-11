package ru.pel.usbddc.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserProfileTest {

    @Test
    @DisplayName("Создание объекта, передавая null-значения")
    void creatingUserProfileWithNullProperties(){
        assertDoesNotThrow(()-> UserProfile.getBuilder()
                .withProfileImagePath(null)
                .withUsername(null)
                .withSecurityId(null)
                .build());
    }
}