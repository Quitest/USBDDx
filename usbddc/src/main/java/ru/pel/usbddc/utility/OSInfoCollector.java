package ru.pel.usbddc.utility;

import lombok.Getter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Класс предназначен для получения сведений об операционной системе, необходимых для сбора исходных данных
 * для анализатора.
 */
public class OSInfoCollector {
    @Getter
    private String osName = null;
    @Getter
    private double osVersion = 0.0;
    @Getter
    private String osArch = null;
    @Getter
    private Path tmpdir = null;
    @Getter
    private String username = null;
    @Getter
    private Path homedir = null;
    @Getter
    private Path currentdir = null;
    @Getter
    private Path systemroot = null;
    //TODO надо получить имя ПК
    //TODO надо получить сетевые настройки для идентификации ПК в сети

    public OSInfoCollector() {
        try {
            Properties props = System.getProperties();
            tmpdir = Paths.get(props.getProperty("java.io.tmpdir"));
            osName = props.getProperty("os.name");
            osArch = props.getProperty("os.arch");
            osVersion = Double.parseDouble(props.getProperty("os.version"));
            username = props.getProperty("user.name");
            homedir = Paths.get(props.getProperty("user.home"));
            currentdir = Paths.get(props.getProperty("user.dir"));
            systemroot = Paths.get(System.getenv("systemroot"));
        } catch (SecurityException e) {
            System.err.println("Возможно, Вам поможет документация на метод System.getProperties() или " +
                    "java.util.Properties.getProperties()");
            e.printStackTrace();
        }
    }

    /**
     * Выдает список всех файлов, в том числе архивных, setupapi.dev.log
     *
     * @return {@code List<Path>}, содержащий пути к каждому setupapi.dev.log
     */
    public List<Path> getListSetupapiDevLogs() {
        //поток каталогов из книги. См. заметку на странице 124
        List<Path> listLogs = new ArrayList<>();
        try (Stream<Path> pathStream = Files.find(getPathToSetupapiDevLog(),
                1,
                (p, bfa) -> p.getFileName().toString().matches("setupapi\\.dev[0-9_.]*\\.log"))) {
            listLogs = pathStream.collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Косяк при работе ru.pel.usbddc.utility.OSInfoCollector.getListSetupapiDevLogs()");
            e.printStackTrace();
        }
        return listLogs;
    }

    /**
     * Возвращает место расположения текстовых логов.
     * Первоначально идет попытка получить место расположения Setupapi.dev.log и других текстовых логов путем чтения
     * параметра LogPath в HKEY_LOCAL_MACHINE\Software\Microsoft\Windows\CurrentVersion\Setup\. Если такой параметр
     * отсутствует, то берется путь по умолчанию:
     * <ul>
     * <li>для ОС Windows 7 и более новых - %systemroot%\inf</li>
     * <li>для ОС Windows более ранних - %systemroot%</li>
     * </ul>
     *
     * @return экземпляр Path, содержащий путь месту размещения setupapi.dev.log'ов.
     */
    //TODO уточнить путь к Setupapi.dev.log в ОС версии 6.0
    //Подробности о месте расположения логов см. https://docs.microsoft.com/ru-ru/windows-hardware/drivers/install/setting-the-directory-path-of-the-text-logs
    public Path getPathToSetupapiDevLog() {
        String logPath = WinRegReader
                .getValue("HKEY_LOCAL_MACHINE\\Software\\Microsoft\\Windows\\CurrentVersion\\Setup", "LogPath")
                .orElse(systemroot.toString());

        if (osVersion >= 6.1) { // 6.1 - версия Windows 7 в линейке Windows NT
            logPath = systemroot.toString() + "\\inf";
        }
        //FIXME выкинуть имя файла из return'а
        return Paths.get(logPath + "\\setupapi.dev.log");
    }

    /**
     * Выводит значения всех полей экземпляра.
     *
     * @return строку, содержащую все поля класса и их значения в формате {@code <имяПоля> = <значение>}
     */
    @Override
    public String toString() {
        //получение всех полей экземпляра и вывод их реализован при помощи методов reflection.
        //Почему? Да просто захотелось попробовать эту рефлексию. Плюс количество полей класса может меняться, и что бы
        //не лазить в метод лишний раз, решено автоматизировать немного.
        final String NEW_LINE = System.lineSeparator();
        StringBuilder sb = new StringBuilder("");
        try {
            Field[] fields = OSInfoCollector.class.getDeclaredFields();

            for (Field field : fields) {

                sb.append(field.getName())
                        .append(" = ")
                        .append(field.get(this))
                        .append(NEW_LINE);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
