package ru.pel.usbddc.utility.winreg.exception;

import java.io.IOException;

/**
 * Проверяемое исключение, возникающее при отказе в доступе к операциям с реестром Windows.
 */
public class RegistryAccessDeniedException extends IOException {
    public RegistryAccessDeniedException(String message) {
        super(message);
    }

    public RegistryAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RegistryAccessDeniedException(Throwable cause) {
        super(cause);
    }
}
