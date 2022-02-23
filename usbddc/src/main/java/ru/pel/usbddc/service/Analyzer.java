package ru.pel.usbddc.service;

import ru.pel.usbddc.entity.USBDevice;

import java.io.IOException;
import java.util.Map;

public interface Analyzer {
    Map<String, USBDevice> getAnalysis() throws IOException;
    Analyzer setDoNewAnalysis(boolean doNewAnalysis);
}
