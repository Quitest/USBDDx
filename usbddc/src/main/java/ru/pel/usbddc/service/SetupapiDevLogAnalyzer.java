package ru.pel.usbddc.service;

import lombok.Getter;
import lombok.Setter;
import ru.pel.usbddc.entity.USBDevice;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
@Setter
public class SetupapiDevLogAnalyzer {
    @Setter
    private static Path pathToLog;
    private static final String NOT_PARSED = "<SERIAL IS NOT PARSED IN LOG>";
    private List<USBDevice> usbDeviceList;
    private Map<String, USBDevice> usbDeviceMap;

    public SetupapiDevLogAnalyzer(Map<String, USBDevice> usbDeviceMap) {
        usbDeviceList = new ArrayList<>();
        this.usbDeviceMap = usbDeviceMap;
    }

    /**
     * Метод поиска даты и времени первой установки USB устройства.
     *
     * @param serial сигнатура USB устройства, по которой необходимо искать.
     * @return дата и время первого подключения устройства к системе.
     */
    public Optional<LocalDateTime> getInstallDateTime(String serial) {
        Optional<LocalDateTime> timeStamp = Optional.empty();
        try (BufferedReader logReader = new BufferedReader(new FileReader(pathToLog.toString()))) {
            String currStr = logReader.readLine();
            String timeStr = "";
            while (currStr != null) {
                if (currStr.matches(">>>.+" + serial + ".+")) {
                    timeStr = logReader.readLine();
                    break;
                }
                currStr = logReader.readLine();
            }

            Matcher matcher = Pattern.compile("\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}").matcher(timeStr);
            if (matcher.find()) {
                timeStr = matcher.group();
                timeStamp = Optional.of(LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return timeStamp;
    }

    /**
     * Парсит все доступные setupapi.dev.log'и на наличие устройств и дат их установки. Даты установки и серийники записывает
     * в usbDeviceMap, переданную конструктору. Если мапа не пустая, то выполняет слияние данных, опираясь на серийники.
     * @throws IOException
     */
    public void parse() throws IOException {
        String systemroot = new OSInfoCollector().getSystemroot().toString();
        Path logPath = Paths.get(systemroot, "\\inf");
        List<Path> devLogList = Files.find(logPath, 1,
                        (path, fileAttributes) -> path.getFileName().toString().matches("setupapi\\.dev[.\\d_]*\\.log"))
                .collect(Collectors.toList());
        for (Path devLog : devLogList) {
            try (BufferedReader reader = new BufferedReader(new FileReader(devLog.toString()))) {
                String currStr = reader.readLine();
                while (currStr != null) {
                    if (currStr.matches(">>>\\s+\\[Device Install \\(Hardware initiated\\).+]")) {
                        String serial = parseSerial(currStr);
                        if (NOT_PARSED.equals(serial)) {
                            currStr = reader.readLine();
                            continue;
                        }
                        currStr = reader.readLine();
                        Matcher matcher = Pattern.compile("\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}").matcher(currStr);
                        LocalDateTime timeStamp = LocalDateTime.MIN;
                        if (matcher.find()) {
                            currStr = matcher.group();
                            timeStamp = LocalDateTime.parse(currStr, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
                        }

                        USBDevice tmp = USBDevice.getBuilder()
                                .withSerial(serial)
                                .withDateTimeFirstInstall(timeStamp).build();

                        usbDeviceMap.merge(serial, tmp, (dst, src) -> {
                            if (dst.getDateTimeFirstInstall().equals(LocalDateTime.MIN)) {
                                dst.setDateTimeFirstInstall(src.getDateTimeFirstInstall());
                            }
                            return dst;
                        });
                    }
                    currStr = reader.readLine();
                }
            }
        }
    }

    /**
     * Парсит строку на предмет серийника
     *
     * @param str
     * @return серийный номер или {@code <SERIAL IS NOT PARSED IN LOG>}, если подходящего серийника нет.
     */
    private String parseSerial(String str) {

        String serial = Arrays.stream(str.split("#"))
                .filter(w -> w.charAt(1) == '&' || w.charAt(w.length() - 2) == '&')
                .map(s -> s.charAt(s.length() - 2) == '&' ?       //Имеется ли суффикс по типу "&0"?
                        s.substring(0, s.length() - 2) : s)
                .findFirst().orElse(NOT_PARSED);

        if (NOT_PARSED.equals(serial)) {
            serial = Arrays.stream(str.split("\\\\"))
                    .reduce((prev, next) -> next).orElse(NOT_PARSED);
            if (!NOT_PARSED.equals(serial)) {
                serial = serial.substring(0, serial.length() - 1);
            }
        }
        return serial;
    }
}
