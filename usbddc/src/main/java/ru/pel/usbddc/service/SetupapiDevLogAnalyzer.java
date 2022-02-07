package ru.pel.usbddc.service;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pel.usbddc.entity.USBDevice;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public class SetupapiDevLogAnalyzer implements Analyzer{
    private static final Logger logger = LoggerFactory.getLogger(SetupapiDevLogAnalyzer.class);
    private static final String NOT_PARSED = "<SERIAL IS NOT PARSED IN LOG>";
    @Setter
    private static Path pathToLog;
    private List<Path> setupapiDevLogList;
    private Map<String, USBDevice> usbDeviceMap;

    /**
     * Позволяет существующую мапу насытить (дополнить) новыми данными, содержащими результаты анализа лог файлов.
     *
     * @param usbDeviceMap мапа, которую надо насытить (дополнить).
     */
    public SetupapiDevLogAnalyzer(Map<String, USBDevice> usbDeviceMap) {
        this(usbDeviceMap, new OSInfoCollector().getSetupapiDevLogList());
    }

    public SetupapiDevLogAnalyzer(List<Path> setupapiDevLogList) {
        this(new HashMap<>(), setupapiDevLogList);
    }

    public SetupapiDevLogAnalyzer(@NonNull Map<String, USBDevice> usbDeviceMap, List<Path> setupapiDevLogList) {
        this.usbDeviceMap = usbDeviceMap;
        this.setupapiDevLogList = setupapiDevLogList;
    }

    /**
     * Создается новая мапа, в последствии заполняется результатом анализа (парсинга) лог файлов.
     */
    public SetupapiDevLogAnalyzer() {
        usbDeviceMap = new HashMap<>();
        setupapiDevLogList = new OSInfoCollector().getSetupapiDevLogList();
    }

    /**
     * Получение результатов анализа всех файлов setupapi.dev*.log на наличие записей об установке устройств. Анализируются
     * строки, начинающиеся на "{@code >>>  [Device Install (Hardware initiated)}" и следующая за ней - содержит дату
     * и время установки.
     *
     * @param doNewAnalysis {@code true} - выполняет анализ файла заново, данные полученные предыдущим вызовом теряются,
     *                      при этом если исходная мапа непустая, то она дополняется данными; если пустая, то создается новая.
     *                      {@code false} - возвращает результаты предыдущего анализа.
     * @return мапу, содержащую результат в виде пары значений {@code серийный номер - объект типа USBDevice}.
     * @throws IOException              If an I/O error occurs.
     * @throws FileNotFoundException    - if the named file does not exist, is a directory rather than a regular file, or
     *                                  for some other reason cannot be opened for reading.
     * @throws IllegalArgumentException - if the maxDepth parameter is negative
     * @throws SecurityException        - If the security manager denies access to the starting file. In the case of the
     *                                  default provider, the checkRead method is invoked to check read access to the directory.
     */
    @Override
    public Map<String, USBDevice> getAnalysis(boolean doNewAnalysis) throws IOException {
        if (doNewAnalysis) {
            if (usbDeviceMap.isEmpty()) {
                usbDeviceMap = new HashMap<>();
            }
//            usbDeviceMap = new HashMap<>();
            parseAllSetupapiDevLogs();
        }
        return usbDeviceMap;
    }

    /**
     * Метод поиска даты и времени первой установки USB устройства с заданным серийным номером.
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
     *
     * @throws IOException              If an I/O error occurs.
     * @throws FileNotFoundException    - if the named file does not exist, is a directory rather than a regular file, or
     *                                  for some other reason cannot be opened for reading.
     * @throws IllegalArgumentException - if the maxDepth parameter is negative
     * @throws SecurityException        - If the security manager denies access to the starting file. In the case of the
     *                                  default provider, the checkRead method is invoked to check read access to the directory.
     */
    public Map<String, USBDevice> parseAllSetupapiDevLogs() throws IOException {
//        List<Path> devLogList = new OSInfoCollector().getSetupapiDevLogList();
//        for (Path devLog : devLogList) {
        for (Path devLog : setupapiDevLogList) {
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
            }catch (IOException e){
                logger.error("Ошибка парсинга лога {}", e.getLocalizedMessage());
                throw e;
            }
        }
        return usbDeviceMap;
    }

    /**
     * Парсит строку на предмет серийника
     *
     * @param str строка, в которой производится поиск серийного номера или нечто похожего на него.
     * @return серийный номер или {@code <SERIAL IS NOT PARSED IN LOG>}, если подходящего серийника нет.
     */
    private String parseSerial(String str) {

        String serial = Arrays.stream(str.split("#"))
                .filter(w -> w.charAt(1) == '&' || w.charAt(w.length() - 2) == '&')
                .map(s -> s.charAt(s.length() - 2) == '&' ?       //Имеется ли суффикс по типу "&0"?
                        s.substring(0, s.length() - 2) : s)
                .findFirst().orElse(NOT_PARSED);
// FIXME: 27.12.2021 в некоторых случаях последним элементом является нечто похожее на GUID.
//  Необходимо обработать этот момент - GUID не должен попадать в серийники.
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
