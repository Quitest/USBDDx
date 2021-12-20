package ru.pel.usbddc.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WinRegReaderTest {
    private final String NODE_NAME = "HKEY_LOCAL_MACHINE\\tempHive";
    private final String HIVE = "C:\\Users\\Default\\ntuser.dat";

    @Test
    @DisplayName("Комплексный тест загрузки и выгрузки куста реестра")
    void complexLoadUnloadHiveTest() {
        assertAll(
                () -> WinRegReader.loadHive(NODE_NAME, HIVE),
                () -> assertTrue(WinRegReader.getSubkeys("HKLM").contains(NODE_NAME)),
                () -> WinRegReader.unloadHive(NODE_NAME),
                () -> assertFalse(WinRegReader.getSubkeys("HKLM").contains(NODE_NAME))
        );
    }
}