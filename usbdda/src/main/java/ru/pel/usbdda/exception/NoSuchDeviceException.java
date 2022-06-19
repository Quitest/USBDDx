package ru.pel.usbdda.exception;

import java.util.NoSuchElementException;

public class NoSuchDeviceException extends NoSuchElementException {
    public NoSuchDeviceException(String msg) {
        super(msg);
    }
}
