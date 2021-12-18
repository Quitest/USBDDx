package ru.pel.usbddc.service;

import lombok.Setter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SetupapiDevLogAnalizer {
    @Setter
    private static Path pathToLog;

    /**
     * Метод поиска даты и времени первой установки USB устройства.
     *
     * @param serial сигнатура USB устройства, по которой необходимо искать.
     * @return дату и время первого подключения устройства к системе.
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

    //TODO реализовать парсинг списка файлов переменной длины
    public void parse() throws FileNotFoundException, UnsupportedEncodingException {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(pathToLog.toString()), StandardCharsets.UTF_8));

        br.lines()
                .filter(l -> l.matches(">>>\\s+\\[Device Install.+USB\\\\.+"))
                .forEach(System.out::println);
    }

}
