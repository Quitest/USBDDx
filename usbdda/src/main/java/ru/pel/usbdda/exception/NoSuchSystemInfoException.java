package ru.pel.usbdda.exception;

import java.util.NoSuchElementException;

public class NoSuchSystemInfoException extends NoSuchElementException {
    public NoSuchSystemInfoException(String msg){
        super(msg);
    }
}
