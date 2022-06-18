package ru.pel.usbdda.exception;

import java.util.NoSuchElementException;

public class NoSuchDevice extends NoSuchElementException {
    public NoSuchDevice(String msg) {
        super(msg);
    }
}
