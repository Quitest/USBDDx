package ru.pel.usbddc.service;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pel.usbddc.entity.OSInfo;

import java.io.IOException;
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
    private static final Logger logger = LoggerFactory.getLogger(OSInfoCollector.class);
    private final OSInfo osInfo;

    public OSInfoCollector() {
        osInfo = new OSInfo();
    }

    public OSInfo collectInfo() {
        try {
            osInfo.setTmpdir(getTmpDir());
            osInfo.setOsName(getOsName());
            osInfo.setOsArch(getOsArch());
            osInfo.setOsVersion(getOsVersion());
            osInfo.setUsername(getUsername());
            osInfo.setHomeDir(getHomeDir());
            osInfo.setCurrentDir(getCurrentDir());

            osInfo.setSystemRoot(getSystemRoot());
            osInfo.setComputerName(getComputerName());

            osInfo.setNetworkInterfaceList(getNetworkInterfaceList());
        } catch (SocketException e) {
//            System.err.println("Не удалось собрать информацию о сетевых интерфейсах");
//            e.printStackTrace();
            logger.error("Не удалось собрать информацию о сетевых интерфейсах. {}", e.getLocalizedMessage());
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

    /**
     * Собирает минимальный объем информации о сетевых интерфейсах: имена интерфейсов, сетевые адреса и соответствующие сетевые имена
     *
     * @return самописный более примитивный аналог java.net.NetworkInterface, содержащий только интересующую информацию.
     * @throws SocketException if an I/O error occurs, or if the platform does not have at least one configured network interface
     */
    public List<ru.pel.usbddc.entity.NetworkInterface> getNetworkInterfaceList() throws SocketException {
        List<ru.pel.usbddc.entity.NetworkInterface> interfaces = new ArrayList<>();
        List<NetworkInterface> networkInterfaceList = NetworkInterface.networkInterfaces().collect(Collectors.toList());
        for (NetworkInterface networkInterface : networkInterfaceList) {
            ru.pel.usbddc.entity.NetworkInterface eth = new ru.pel.usbddc.entity.NetworkInterface();
            //для каждого сетевого интерфейса определяем имена...
            eth.setDisplayName(networkInterface.getDisplayName());
            eth.setName(networkInterface.getName());
            //... и выбираем из общей кучи информации только IP адреса и соответствующие сетевые имена.
            long startTime = System.currentTimeMillis();
            List<ru.pel.usbddc.entity.NetworkInterface.InetAddress> inetAddressList = networkInterface.inetAddresses().parallel()
                    .map(inetAddress -> {
                        ru.pel.usbddc.entity.NetworkInterface.InetAddress addr =
                                new ru.pel.usbddc.entity.NetworkInterface.InetAddress();
                        addr.setHostAddress(inetAddress.getHostAddress());
                        addr.setHostName(inetAddress.getHostName());
                        long start = System.currentTimeMillis();
                        addr.setCanonicalName(inetAddress.getCanonicalHostName());
                        logger.trace("\tMapping inetAddress {} is {}ms", inetAddress.getHostAddress(), System.currentTimeMillis()-start);
                        return addr;
                    }).collect(Collectors.toList());
            logger.trace("If name: {} - {}ms", networkInterface.getDisplayName(), System.currentTimeMillis()-startTime);
            eth.setInetAddressList(inetAddressList);

            interfaces.add(eth);
        }
        return interfaces;
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
        /*
        +----------------------------+------------------+
        | Operating system	         | Version number   |
        +----------------------------+------------------+
        | Windows 11                 | 10.0*            |
        | Windows 10                 | 10.0*            |
        | Windows Server 2022        | 10.0*            |
        | Windows Server 2019        | 10.0*            |
        | Windows Server 2016        | 10.0*            |
        | Windows 8.1                | 6.3*             |
        | Windows Server 2012 R2     | 6.3*             |
        | Windows 8                  | 6.2              |
        | Windows Server 2012        | 6.2              |
        | Windows 7	                 | 6.1              |
        | Windows Server 2008 R2     | 6.1              |
        | Windows Server 2008	     | 6.0              |
        | Windows Vista	             | 6.0              |
        | Windows Server 2003 R2 	 | 5.2              |
        | Windows Server 2003	     | 5.2              |
        | Windows XP 64-Bit Edition  | 5.2              |
        | Windows XP	             | 5.1              |
        | Windows 2000	             | 5.0              |
        +----------------------------+------------------+
        * */
        return getOsVersion() >= 6.0 ? Path.of(logPath, "\\inf") : Path.of(logPath);
//        if (getOsVersion() >= 6.0) {
//            logPath = getSystemRoot() + "\\inf";
//        }
//        return Paths.get(logPath);
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
