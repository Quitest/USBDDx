package ru.pel.usbddc.legacy;
/**
 * USBDDC - USB devices data collector
 */

import ru.pel.usbddc.entity.USBDevice;
import ru.pel.usbddc.utility.RegistryAnalizer;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        //TODO сделать вывод сразу в файл, что бы не было проблем с кодировками в консоле.
        PrintWriter writer = new PrintWriter("result.txt", "UTF-8");
//        System.out.println(new ru.pel.usbddc.utility.OSInfoCollector().toString());
        System.out.println("\nTESTING ru.pel.usbddc.utility.RegistryAnalizer.getUSBDevices()");
        writer.println("\nTESTING ru.pel.usbddc.utility.RegistryAnalizer.getUSBDevices()");
        List<USBDevice> usbDevices = RegistryAnalizer.getUSBDevices();
        usbDevices.stream()
//                .filter(d->d.getVid()!=null)
                .forEach(System.out::println);
        usbDevices.forEach(writer::println);

//        System.out.println("\nTESTING getInstallDateTime()");
//        ru.pel.usbddc.utility.SetupapiDevLogAnalizer.setPathToLog(new ru.pel.usbddc.utility.OSInfoCollector().getPathToSetupapiDevLog());
//        for (ru.pel.usbddc.entity.USBDevice d : usbDevices){
//            String serial = d.getSerial();
//            LocalDateTime td = new ru.pel.usbddc.utility.SetupapiDevLogAnalizer().getInstallDateTime(serial).orElse(LocalDateTime.MIN);
//            System.out.printf("USB VID/PID: %5s/%-5S | Install date: %s %n",d.getVid(), d.getPid(),
//                    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").format(td));
//        }

//        System.out.println("\nTESTING getAllValuesInKey()");
//        Map<String, String> values = ru.pel.usbddc.utility.WinRegReader
//                .getAllValuesInKey("HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Enum\\USB\\" +
//                        "VID_125F&PID_312B\\1492710242260098").get();
//        values.forEach((k, v) -> System.out.println(k + " = " + v));

//        List<String> l =ru.pel.usbddc.utility.WinRegReader.getSubkeys("HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Enum\\USB\\VID_125F&PID_312B");
//                l.forEach(System.out::println);
        writer.close();
    }
}
