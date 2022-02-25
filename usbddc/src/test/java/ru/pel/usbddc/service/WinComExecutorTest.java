package ru.pel.usbddc.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith(MockitoExtension.class)
class WinComExecutorTest {

    private static final WinComExecutor WIN_COM_EXECUTOR = new WinComExecutor();

    private final String NODE_NAME = "HKEY_LOCAL_MACHINE\\tempHive";
    private final String HIVE = "C:\\Users\\Default\\ntuser.dat";
    private final String LOAD_HIVE_COMMAND = String.format("reg load %s %s", NODE_NAME, HIVE);
    private final String UNLOAD_HIVE = String.format("reg unload %s", NODE_NAME);

    @Test
    @DisplayName("Команда выполнена с ошибкой - body содержит описание ошибки")
    void whenExecCommandFailBodyContainsDescription() throws IOException, InterruptedException {
        try {
            WinComExecutor.Result<Integer, String> result = WIN_COM_EXECUTOR.exec(LOAD_HIVE_COMMAND);
            assertThat(result.getBody(), not(emptyOrNullString()));
        } finally {
            WIN_COM_EXECUTOR.exec(UNLOAD_HIVE);
        }
    }

    @Test
    @DisplayName("Команда выполнена с ошибкой - exit code НЕ равен нулю")
    void whenExecCommandFailExitCodeNotEqualToZero() throws IOException, InterruptedException {
        try {
            WinComExecutor.Result<Integer, String> result = WIN_COM_EXECUTOR.exec(LOAD_HIVE_COMMAND);
            assertThat(result.getExitCode(), not(equalTo(0)));
        } finally {
            WIN_COM_EXECUTOR.exec(UNLOAD_HIVE);
        }
    }

    @Test
    @DisplayName("Команда выполнена успешно - body содержит текстовый результат")
    void whenExecCommandSuccessfulBodyContainsStringResult() throws IOException, InterruptedException {
        try {
            WinComExecutor.Result<Integer, String> result = WIN_COM_EXECUTOR.exec(LOAD_HIVE_COMMAND);
            assertThat(result.getBody(), not(blankOrNullString()));
        } finally {
            WIN_COM_EXECUTOR.exec(UNLOAD_HIVE);
        }
    }

    @Test
    @DisplayName("Команда выполнена успешно - exit code РАВЕН 0")
    void whenExecCommandSuccessfulExitCodeEqualToZero() throws IOException, InterruptedException {
        try {
            WinComExecutor.Result<Integer, String> result = WIN_COM_EXECUTOR.exec(LOAD_HIVE_COMMAND);
            assertThat(result.getExitCode(), equalTo(0));
        } finally {
            WIN_COM_EXECUTOR.exec(UNLOAD_HIVE);
        }
    }
}