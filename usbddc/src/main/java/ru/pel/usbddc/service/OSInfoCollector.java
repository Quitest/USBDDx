package ru.pel.usbddc.service;

import lombok.Getter;
import ru.pel.usbddc.entity.OSInfo;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Класс предназначен для получения сведений об операционной системе, необходимых для сбора исходных данных
 * для анализатора.
 */
@Getter
public class OSInfoCollector {
    private OSInfo osInfo;

    public OSInfo collectInfo() {
        try {
            osInfo.setTmpdir(Paths.get(System.getProperty("java.io.tmpdir")));
            osInfo.setOsName(System.getProperty("os.name"));
            osInfo.setOsArch(System.getProperty("os.arch"));
            osInfo.setOsVersion(Double.parseDouble(System.getProperty("os.version")));
            osInfo.setUsername(System.getProperty("user.name"));
            osInfo.setHomeDir(Paths.get(System.getProperty("user.home")));
            osInfo.setCurrentDir(Paths.get(System.getProperty("user.dir")));

            osInfo.setSystemRoot(Paths.get(System.getenv("systemroot")));
            osInfo.setComputerName(System.getenv("computername"));

            osInfo.setNetworkInterfaceList(NetworkInterface.networkInterfaces().collect(Collectors.toList()));
        } catch (SecurityException e) {
            System.err.println("Возможно, Вам поможет документация на метод System.getProperties() или " +
                    "java.util.Properties.getProperties()");
            e.printStackTrace();
        } catch (SocketException e) {
            System.err.println("Не удалось собрать информацию о сетевых интерфейсах");
            e.printStackTrace();
        }

        return osInfo;
    }

    public String getComputerName() {
        return System.getenv("computername");
    }

    public Path getCurrentDir() {
        return Paths.get(System.getProperty("user.dir"));
    }

    public Path getHomeDir() {
        return Paths.get(System.getProperty("user.home"));
    }

    public List<NetworkInterface> getNetworkInterfaceList() throws SocketException {
        return NetworkInterface.networkInterfaces().collect(Collectors.toList());
    }

    public String getOsArch() {
        return System.getProperty("os.arch");
    }

    public String getOsName() {
        return System.getProperty("os.name");
    }

    public double getOsVersion() {
        return Double.parseDouble(System.getProperty("os.version"));
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
    //FIXME приблизить к типовому интерфейсу - переименовать в getPathToLogs.
    public Path getPathToSetupapiDevLog() {
        String logPath = WinRegReader
                .getValue("HKEY_LOCAL_MACHINE\\Software\\Microsoft\\Windows\\CurrentVersion\\Setup", "LogPath")
                .orElse(getSystemRoot().toString());

        if (getOsVersion() >= 6.1) { // 6.1 - версия Windows 7 в линейке Windows NT
            logPath = getSystemRoot() + "\\inf";
        }

        return Paths.get(logPath);
    }

    /**
     * Выдает список всех файлов, в том числе архивных, setupapi.dev.log
     *
     * @return {@code List<Path>}, содержащий пути к каждому setupapi.dev.log
     */
    //FIXME приблизить к типовому интерфейсу - переименовать в getLogList
    public List<Path> getSetupapiDevLogList() {
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

    public Path getSystemRoot() {
        return Paths.get(System.getenv("systemroot"));
    }

    public Path getTmpDir() {
        return Paths.get(System.getProperty("java.io.tmpdir"));
    }

    public String getUsername() {
        return System.getProperty("user.name");
    }

//    /**
//     * Выводит значения всех полей экземпляра.
//     *
//     * @return строку, содержащую все поля класса и их значения в формате {@code <имяПоля> = <значение>}
//     */
//    @Override
//    public String toString() {
//        //получение всех полей экземпляра и вывод их реализован при помощи методов reflection.
//        //Почему? Да просто захотелось попробовать эту рефлексию. Плюс количество полей класса может меняться, и что бы
//        //не лазить в метод лишний раз, решено автоматизировать немного.
//        final String NEW_LINE = System.lineSeparator();
//        StringBuilder sb = new StringBuilder();
//        try {
//            Field[] fields = OSInfoCollector.class.getDeclaredFields();
//
//            for (Field field : fields) {
//
//                sb.append(field.getName())
//                        .append(" = ")
//                        .append(field.get(this))
//                        .append(NEW_LINE);
//            }
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//        return sb.toString();
//    }
}
