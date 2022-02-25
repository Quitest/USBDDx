package ru.pel.usbddc.analyzer;

import ru.pel.usbddc.entity.USBDevice;

import java.io.IOException;
import java.util.Map;

public interface Analyzer {
    Map<String, USBDevice> getAnalysis() throws IOException;
    Analyzer setDoNewAnalysis(boolean doNewAnalysis);
}
