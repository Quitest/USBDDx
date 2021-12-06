package ru.pel.usbddc;
/**
 * USBDDC - USB devices data collector
 */

import ru.pel.usbddc.entity.USBDevice;
import ru.pel.usbddc.utility.RegistryAnalizer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        //TODO сделать вывод сразу в файл, что бы не было проблем с кодировками в консоле.
        PrintWriter writer = new PrintWriter("result.txt", StandardCharsets.UTF_8);
//        System.out.println(new ru.pel.usbddc.utility.OSInfoCollector().toString());
        System.out.println("\nTESTING ru.pel.usbddc.utility.RegistryAnalizer.getUSBDevices()");
        writer.println("\nTESTING ru.pel.usbddc.utility.RegistryAnalizer.getUSBDevices()");
        List<USBDevice> usbDevices = RegistryAnalizer.getUSBDevices();
        usbDevices.stream()
                .forEach(System.out::println);
        usbDevices.forEach(writer::println);

        RegistryAnalizer.getMountedDevices().forEach(
                (k,v) -> System.out.println(k+" = "+v)
        );

        writer.close();
    }
}
