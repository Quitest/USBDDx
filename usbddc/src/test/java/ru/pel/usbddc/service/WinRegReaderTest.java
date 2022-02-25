package ru.pel.usbddc.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class WinRegReaderTest {
    private static final String NODE_NAME = "HKEY_LOCAL_MACHINE\\tempHive";
    private static final String HIVE = "C:\\Users\\Default\\ntuser.dat";
    private final  WinRegReader winRegReader = new WinRegReader();

    @Test
    @DisplayName("Проверка НЕ существующей ветки реестра. Ожидается FALSE")
    void isKeyExistsFalse() throws IOException, InterruptedException {
        assertFalse(winRegReader.isKeyExists("HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\EnumWhichNotExists"));
    }

    @Test
    @DisplayName("Проверка существующей ветки реестра. Ожидается TRUE")
    void isKeyExistsTrue() throws IOException, InterruptedException {
        winRegReader.loadHive(NODE_NAME, HIVE);
        assertTrue(winRegReader.isKeyExists("HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Enum"));
        winRegReader.unloadHive(NODE_NAME);
    }

    @Test
    @DisplayName("Комплексный тест загрузки и выгрузки куста реестра")
    void loadAndUnloadHiveTest() throws IOException, InterruptedException {
        WinComExecutor.Result<Integer, String> loadResult = winRegReader.loadHive(NODE_NAME, HIVE);
        WinComExecutor.Result<Integer, String> unloadResult = winRegReader.unloadHive(NODE_NAME);
        assertAll(
                () -> assertEquals(0, loadResult.getExitCode(), "Вероятно, отсутствуют права админа"),
                () -> assertEquals(0, unloadResult.getExitCode(), "Вероятно, отсутствуют права админа")
        );
    }

    @Test
    @DisplayName("Получение подразделов реестра")
    void getSubkeys() throws IOException, InterruptedException {
        final String REG_KEY = "HKEY_LOCAL_MACHINE\\SYSTEM";
        List<String> subkeys = winRegReader.getSubkeys(REG_KEY);
        assertAll(
                ()-> assertTrue(subkeys.size()>0),
                ()->assertTrue(subkeys.contains("HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet"))
        );
    }

    @Test
    @DisplayName("Чтение всех параметров раздела, в том числе кирилицы")
    void whenGetAllCyrillicValuesInKey_thenReadable(){
        Optional<Map<String, String>> allValuesInKey = winRegReader.getAllValuesInKey("HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\WinSAT");
        assertThat(allValuesInKey.get(), hasEntry("Новый параметр #1", "значение нового параметра"));
    }
}