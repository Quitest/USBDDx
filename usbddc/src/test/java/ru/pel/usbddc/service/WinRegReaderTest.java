package ru.pel.usbddc.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WinRegReaderTest {

    @Test
    void loadHive() {
        WinRegReader.loadHive("HKLM\\tempHive", "C:\\Users\\adm\\ntuser.dat");
    }

    @Test
    void unloadHive() {
    }
}