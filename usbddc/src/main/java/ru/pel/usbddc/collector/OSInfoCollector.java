package ru.pel.usbddc.collector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pel.usbddc.config.UsbddcConfig;
import ru.pel.usbddc.entity.OSInfo;
import ru.pel.usbddc.service.WinRegReader;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * Класс предназначен для получения сведений об операционной системе, необходимых для сбора исходных данных
 * для анализатора.
 */
public class OSInfoCollector {
    private static final Logger LOGGER = LoggerFactory.getLogger(OSInfoCollector.class);
    private static final int THREAD_POOL_SIZE;

    static {
        THREAD_POOL_SIZE = UsbddcConfig.getInstance().getThreadPoolSize();
        LOGGER.debug("Размер пула потоков = {}", THREAD_POOL_SIZE);
    }

    private final OSInfo osInfo;

    public OSInfoCollector() {
        osInfo = new OSInfo();
    }

    /**
     * Выполняет сбор всей доступной информации о системе.
     *
     * @return объект типа OSInfo с заполненными свойствами.
     */
    public OSInfo collectInfo() {
        osInfo.setTmpdir(getTmpDir());
        osInfo.setOsName(getOsName());
        osInfo.setOsArch(getOsArch());
        osInfo.setOsVersion(getOsVersion());
        osInfo.setUsername(getUsername());
        osInfo.setHomeDir(getHomeDir());
        osInfo.setCurrentDir(getCurrentDir());

        osInfo.setSystemRoot(getSystemRoot());
        osInfo.setComputerName(getComputerName());
        osInfo.setOsId(getOsId());
        try {
            osInfo.setNetworkInterfaceList(getNetworkInterfaceList());
        } catch (SocketException | InterruptedException e) {
            LOGGER.error("Не удалось собрать информацию о сетевых интерфейсах. {}", e.getLocalizedMessage());
            LOGGER.debug("{}", e.toString());
            Thread.currentThread().interrupt();
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
     * @throws SocketException      if an I/O error occurs, or if the platform does not have at least one configured network interface.
     * @throws InterruptedException if interrupted while waiting, in which case unfinished tasks are cancelled
     */
    public List<ru.pel.usbddc.entity.NetworkInterface> getNetworkInterfaceList() throws SocketException, InterruptedException {
        long startTime = System.currentTimeMillis();
        List<NetworkInterface> networkInterfaceList = NetworkInterface.networkInterfaces().toList();
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<Callable<ru.pel.usbddc.entity.NetworkInterface>> taskList = new ArrayList<>();

        for (NetworkInterface networkInterface : networkInterfaceList) {
            Callable<ru.pel.usbddc.entity.NetworkInterface> networkInterfaceCallable = () -> mapNetworkInterface(networkInterface);
            taskList.add(networkInterfaceCallable);
        }

        List<ru.pel.usbddc.entity.NetworkInterface> interfaces;
        interfaces = executorService.invokeAll(taskList).stream()
                .map(networkInterfaceFuture -> {
                    ru.pel.usbddc.entity.NetworkInterface iface;
                    try {
                        iface = networkInterfaceFuture.get();
                    } catch (InterruptedException | ExecutionException e) {
                        LOGGER.error("{}", e.getLocalizedMessage());
                        LOGGER.debug("{}", e.toString());
                        iface = new ru.pel.usbddc.entity.NetworkInterface();
                        Thread.currentThread().interrupt();
                    }
                    return iface;
                }).toList();
        executorService.shutdown();
        LOGGER.trace("Время сбора инф об ОС: {}", System.currentTimeMillis() - startTime);
        return interfaces;
    }

    public String getOsArch() {
        return System.getProperty("os.arch");
    }

    public String getOsId() {
        return new WinRegReader().getValue("HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Cryptography", "MachineGuid").orElseThrow();
    }

    /**
     * Получить сведения о системе. Не выполняет какого-либо сбора, анализа информации.
     *
     * @return объект OSInfo, содержащий результаты предыдущего вызова {@link #collectInfo()}
     */
    public OSInfo getOsInfo() {
        return osInfo;
    }

    public String getOsName() {
        return System.getProperty("os.name");
    }

    /**
     * Возвращает версию ОС. Для семейства MS Windows соответствие версии и названия приведены в таблице:
     * <pre>{@code
     * +---------------------------+---------------+
     * | Operating system          | Version number|
     * +---------------------------+---------------+
     * | Windows 11                | 10.0*         |
     * | Windows 10                | 10.0*         |
     * | Windows Server 2022       | 10.0*         |
     * | Windows Server 2019       | 10.0*         |
     * | Windows Server 2016       | 10.0*         |
     * | Windows 8.1               | 6.3*          |
     * | Windows Server 2012 R2    | 6.3*          |
     * | Windows 8                 | 6.2           |
     * | Windows Server 2012       | 6.2           |
     * | Windows 7                 | 6.1           |
     * | Windows Server 2008 R2    | 6.1           |
     * | Windows Server 2008       | 6.0           |
     * | Windows Vista             | 6.0           |
     * | Windows Server 2003 R2    | 5.2           |
     * | Windows Server 2003       | 5.2           |
     * | Windows XP 64-Bit Edition | 5.2           |
     * | Windows XP                | 5.1           |
     * | Windows 2000              | 5.0           |
     * +---------------------------+---------------+
     * }</pre>
     *
     * @return версия операционной системы.
     */
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
        //Подозреваю, что в абсолютном большинстве случаев блок try будет сыпаться с исключением - редко меняется стандартное расположение
        // лог файлов - инициализируем переменную сразу.
        Path logPath = getOsVersion() >= 6.0 ? Path.of(getSystemRoot().toString(), "\\inf") : getSystemRoot();
        try {
            logPath = Path.of(new WinRegReader()
                    .getValue("HKEY_LOCAL_MACHINE\\Software\\Microsoft\\Windows\\CurrentVersion\\Setup", "LogPath")
                    .orElseThrow());
        } catch (NoSuchElementException e) {
            LOGGER.info("""
                    Произошла ошибка в результате поиска нестандартного пути к setupapi.dev.log в реестре :
                    \t\t{}
                    \tИспользуется путь по умолчанию для {}: {}
                    """, e.getLocalizedMessage(), getOsName(), logPath);
        }
        return logPath;
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
            listLogs = pathStream.toList();
        } catch (IOException e) {
            LOGGER.error("{}", e.getLocalizedMessage());
            LOGGER.debug("Не удалось получить список setupapi.dev.log: \n {}", e.toString());
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

    private ru.pel.usbddc.entity.NetworkInterface.InetAddress mapInetAddress(InetAddress src) {
        ru.pel.usbddc.entity.NetworkInterface.InetAddress dst = new ru.pel.usbddc.entity.NetworkInterface.InetAddress();
        dst.setHostAddress(src.getHostAddress());
        dst.setHostName(src.getHostName());
        dst.setCanonicalName(src.getCanonicalHostName());
        return dst;
    }

    private ru.pel.usbddc.entity.NetworkInterface mapNetworkInterface(NetworkInterface networkInterface) {
        long mappingInterfaceStartTime = System.currentTimeMillis();
        ru.pel.usbddc.entity.NetworkInterface eth = new ru.pel.usbddc.entity.NetworkInterface();
        //для каждого сетевого интерфейса определяем имена...
        eth.setDisplayName(networkInterface.getDisplayName());
        eth.setName(networkInterface.getName());
        //... и выбираем из общей кучи информации только IP адреса и соответствующие сетевые имена.
        List<ru.pel.usbddc.entity.NetworkInterface.InetAddress> inetAddressList = networkInterface.inetAddresses()
                .map(this::mapInetAddress).toList();
        eth.setInetAddressList(inetAddressList);
        LOGGER.trace("Время маппинга интерфейса {} : {} ms", eth.getDisplayName(), System.currentTimeMillis() - mappingInterfaceStartTime);
        return eth;
    }
}
