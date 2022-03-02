package ru.pel.usbddc.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.pel.usbddc.utility.winreg.WinRegReader;
import ru.pel.usbddc.utility.winreg.exception.RegistryAccessDeniedException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.jupiter.api.Assertions.*;

class WinRegReaderTest {
    private static final String NODE_NAME = "HKEY_LOCAL_MACHINE\\tempHive";
    private static final String HIVE = "C:\\Users\\Default\\ntuser.dat";
    private final WinRegReader winRegReader = new WinRegReader();

    @Test
    @DisplayName("Получение подразделов реестра")
    void getSubkeys() throws IOException, InterruptedException {
        final String REG_KEY = "HKEY_LOCAL_MACHINE\\SYSTEM";
        List<String> subkeys = winRegReader.getSubkeys(REG_KEY);
        assertAll(
                () -> assertTrue(subkeys.size() > 0),
                () -> assertTrue(subkeys.contains("HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet"))
        );
    }

    @Test
    @DisplayName("Проверка НЕ существующей ветки реестра. Ожидается FALSE")
    void isKeyExistsFalse() throws IOException, InterruptedException {
        assertFalse(winRegReader.isKeyExists("HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\EnumWhichNotExists"));
    }

    @Test
    @DisplayName("Проверка существующей ветки реестра. Ожидается TRUE")
    void isKeyExistsTrue() throws IOException, InterruptedException {
        assertTrue(winRegReader.isKeyExists("HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Enum"));
    }

    @Test
    @DisplayName("Чтение всех параметров раздела, в том числе кирилицы")
    void whenGetAllCyrillicValuesInKey_thenReadable() {
        Optional<Map<String, String>> allValuesInKey = winRegReader.getAllValuesInKey("HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\WinSAT");
        assertThat(allValuesInKey.get(), hasEntry("Новый параметр #1", "значение нового параметра"));
    }

    @Test
    @DisplayName("Операция загрузки куста реестра. Не ожидается исключений.")
    void whenLoadHive_thenDoesNotThrowException() {
        try {
            assertDoesNotThrow(() -> winRegReader.loadHive(NODE_NAME, HIVE));
        } finally {
            try {
                winRegReader.unloadHive(NODE_NAME);
            } catch (RegistryAccessDeniedException ignore) {
                //Игнорируем. При успешной загрузке, выгрузка делается для удаления следов активности в реестре.
                // Вероятно, даже в этом случае не стоит игнорировать исключение, однако, идей по обработке пока нет.
                // Еще более правильным решением должно быть решение, которое не выполняет каких-либо изменений в реестре,
                // в том числе и загрузки кустов.
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    @DisplayName("Операция загрузки куста реестра при отсутствии прав доступа. Ожидается  RegistryAccessDeniedException.")
    void whenLoadWithNoPermissions_thenThrowRegistryAccessDeniedException() {
        try {
            RegistryAccessDeniedException exception =
                    assertThrows(RegistryAccessDeniedException.class, () -> winRegReader.loadHive(NODE_NAME, HIVE));
            String actual = exception.getMessage();
            String expected = "Ошибка: Клиент не обладает требуемыми правами. Код: 1";
            assertThat(actual, equalToIgnoringCase(expected));
        } finally {
            try {
                winRegReader.unloadHive(NODE_NAME);
            } catch (RegistryAccessDeniedException ignore) {
                //Игнорируем. При успешной загрузке, выгрузка делается для удаления следов активности в реестре.
                // Вероятно, даже в этом случае не стоит игнорировать исключение, однако, идей по обработке пока нет.
                // Еще более правильным решением должно быть решение, которое не выполняет каких-либо изменений в реестре,
                // в том числе и загрузки кустов.
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    @DisplayName("Операция выгрузки несуществующей ветки реестра при наличии прав. Ожидается RegistryAccessDeniedException.")
    void whenUnloadNotExistsKey_thenRegistryAccessDeniedException() {
        RegistryAccessDeniedException exception =
                assertThrows(RegistryAccessDeniedException.class, () -> winRegReader.unloadHive(NODE_NAME + "notExists"));
        String actual = exception.getLocalizedMessage();
        String expected = "Ошибка: Параметр задан неверно. Код: 1";
        assertThat(actual, equalToIgnoringCase(expected));
    }

    @Test
    @DisplayName("Операция выгрузки куста реестра при отсутствии прав доступа. Ожидается  RegistryAccessDeniedException.")
    void whenUnloadWithNoPermissions_thenThrowRegistryAccessDeniedException() {
        RegistryAccessDeniedException exception =
                assertThrows(RegistryAccessDeniedException.class, () -> winRegReader.unloadHive(NODE_NAME));
        String actual = exception.getMessage();
        String expected = "Ошибка: Клиент не обладает требуемыми правами. Код: 1";
        assertThat(actual, equalToIgnoringCase(expected));
    }
}