package ru.pel.usbddc.analyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pel.usbddc.entity.USBDevice;
import ru.pel.usbddc.entity.UserProfile;
import ru.pel.usbddc.utility.winreg.WinRegReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * Предназначен для сбора информации о USB устройствах из реестра ОС Windows.
 */
public class RegistryAnalyzer implements Analyzer {
    private static final String REG_KEY_USB = "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Enum\\USB";
    private static final String REG_KEY_MOUNTED_DEVICES = "HKEY_LOCAL_MACHINE\\SYSTEM\\MountedDevices";
    private static final String REG_KEY_PROFILE_LIST = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\ProfileList";
    private static final String REG_KEY_USBSTOR = "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Enum\\USBSTOR";
    /**
     * Ветка службы ReadyBoost
     */
    private static final String REG_KEY_EMDMGMT = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\EMDMgmt";
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryAnalyzer.class);
    private final WinRegReader winRegReader = new WinRegReader();
    private Map<String, USBDevice> usbDeviceMap;
    private boolean doNewAnalysis;

    public RegistryAnalyzer() {
        this(true, new HashMap<>());
    }

    public RegistryAnalyzer(boolean doNewAnalysis) {
        this(doNewAnalysis, new HashMap<>());
    }

    public RegistryAnalyzer(boolean doNewAnalysis, Map<String, USBDevice> usbDeviceMap) {
        this.usbDeviceMap = usbDeviceMap;
        this.doNewAnalysis = doNewAnalysis;
    }

    /**
     * <p>Метод собирает сведения о смонтированных устройствах. Основная задача - установить соответствие между GUID и
     * серийными номерами устройств.</p>
     * <p>Если устройство с серийным номером уже имеется, то сведения о нем обновляются. Если устройства с серийным
     * номером нет, то оно запоминается.</p>
     * <p>Информация берется из HKEY_LOCAL_MACHINE\SYSTEM\MountedDevices</p>
     *
     * @return мапу смонтированных устройств. Key - серийный номер, value - устройство со всеми известными сведениями о
     * нем на текущий момент.
     */
    public Map<String, USBDevice> associateSerialToGuid() {
        Map<String, String> mountedDevices = winRegReader.getAllValuesInKey(REG_KEY_MOUNTED_DEVICES).orElseThrow();

        for (Map.Entry<String, String> entry : mountedDevices.entrySet()) {
            String encodedValue = entry.getValue();
            if (isHDDPartition(encodedValue)) {
                LOGGER.info("[i] Устройство определено как раздел несъемного HDD/SSD. \n\t\tПропущено: {} :: {}.",
                        entry.getKey(), encodedValue);
                continue;
            }
            String decodedValue = decodeHexToString(encodedValue);
            Matcher matcher = Pattern.compile(".*#(?<serial>\\w&?\\w*\\w+)(&\\d)?#").matcher(decodedValue);
            String serial = "";
            entry.setValue(decodedValue);
            if (matcher.find()) {
                serial = matcher.group("serial");
            }

            String key = entry.getKey();
            String deviceGuid = Arrays.stream(key.split(""))
                    .dropWhile(ch -> !ch.equals("{"))
                    .collect(Collectors.joining());

            //FIXME заменить код на метод копирования_ненулевых_свойств()
            USBDevice tmp = usbDeviceMap.get(serial);
            if (tmp == null) {
                usbDeviceMap.put(serial, new USBDevice().setSerial(serial).setGuid(deviceGuid));
            } else {
                tmp.setGuid(deviceGuid);
                usbDeviceMap.put(serial, tmp);
            }
        }
        return usbDeviceMap;
    }

    /**
     * Декодирование строки текста из HEX-представления в читаемый формат.
     *
     * @param hexStr декодируемая строка
     * @return строка в текстовом читаемом виде.
     */
    private String decodeHexToString(String hexStr) {
        return Arrays.stream(hexStr.split("(?<=\\G..)"))// разбиваем строку на парные числа - байты
                .filter(str -> !str.equals("00")) //отбрасываем нулевые байты, что бы в результате не было "пробельных" символов
                .map(str -> Character.toString(Integer.parseInt(str, 16))) // преобразуем HEX в строковые значения
                .collect(Collectors.joining());
    }

    /**
     * <p>Определяет под какой учетной записью осуществлялось использование устройства.</p>
     * <p>Определение происходит путем сопоставления имеющегося GUID и
     * из куста реестра пользователя \Software\Microsoft\Windows\CurrentVersion\Explorer\MountPoints2
     * </p>
     *
     * @return число устройств, пользователей которых удалось определить.
     */
    public long determineDeviceUsers() {
        List<UserProfile> userProfileList = getUserProfileList();
        String currentUserHomeDir = System.getProperty("user.home");
        long counter = 0;
        for (UserProfile userProfile : userProfileList) {
            List<String> mountedGUIDsOfUser = userProfile.getProfileImagePath().toString().equals(currentUserHomeDir) ?
                    getMountedGUIDsOfCurrentUser() : getMountedGUIDsOfUser(userProfile);
            counter += usbDeviceMap.values().stream()
                    .filter(usbDevice -> mountedGUIDsOfUser.contains(usbDevice.getGuid()))
                    .map(usbDevice -> {
                        usbDevice.addUserProfile(userProfile);
                        return usbDevice;
                    }).count();
        }
        return counter;
    }

    /**
     * Выполняет анализ реестра на наличие записей о USB устройствах и данных о них.
     *
     * @return если doNewAnalysis задан true, то возвращается результат нового анализа, если false - возвращает результат предыдущего
     * анализа.
     */
    @Override
    public Map<String, USBDevice> getAnalysis() {
        if (!doNewAnalysis) {
            return usbDeviceMap;
        } else {
            usbDeviceMap = new HashMap<>();
            getUsbDevices();
            getReadyBoostDevices();
            associateSerialToGuid();
            determineDeviceUsers();
            getFriendlyName();
            parseWindowsPortableDevice();
        }
        return usbDeviceMap;
    }

    //TODO кроме FriendlyName метод еще и revision заполняет - имя не в полной мере соответствует.
    public Map<String, USBDevice> getFriendlyName() {
        try {
            List<String> deviceGroupList = winRegReader.getSubkeys(REG_KEY_USBSTOR);
            for (String deviceGroupKey : deviceGroupList) {
                String revision = deviceGroupKey.substring(deviceGroupKey.lastIndexOf('_') + 1);
                List<String> serialList = winRegReader.getSubkeys(deviceGroupKey);
                for (String serialKey : serialList) {
                    int lastIndexOfSlash = serialKey.lastIndexOf('\\') + 1;
                    String serial = serialKey.charAt(serialKey.length() - 2) == '&' ?       //Имеется ли суффикс по типу "&0" у раздела реестра?
                            serialKey.substring(lastIndexOfSlash, serialKey.length() - 2) : //Если да - отбрасываем его и префикс в виде пути
                            serialKey.substring(lastIndexOfSlash);                          //иначе просто отбрасываем префикс в виде пути.
                    String friendlyName = winRegReader.getValue(serialKey, "FriendlyName").orElse("<не доступно>");
                    String diskId = winRegReader.getValue(serialKey + "\\Device Parameters\\Partmgr", "DiskId").orElse("<не доступно>");

                    USBDevice tmp = new USBDevice()
                            .setRevision(revision)
                            .setFriendlyName(friendlyName)
                            .setDiskId(diskId);

                    usbDeviceMap.merge(serial, tmp, (usbDevice, src) -> {
                        usbDevice.setFriendlyName(src.getFriendlyName());
                        usbDevice.setRevision(src.getRevision());
                        usbDevice.setDiskId(src.getDiskId());
                        return usbDevice;
                    });
                }
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("ОШИБКА. Не удалось получить поле Friendly name. Причина {}", e.getLocalizedMessage());
            LOGGER.debug("Не удалось обработать {}", REG_KEY_USBSTOR, e);
            Thread.currentThread().interrupt();
        }
        return usbDeviceMap;
    }

    /**
     * Получить GUID устройств, которые использовались ТЕКУЩИМ пользователем.
     *
     * @return список GUID всех когда-либо подключенных устройств.
     */
    public List<String> getMountedGUIDsOfCurrentUser() {
        String mountPoints2 = "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\MountPoints2";
        List<String> result = new ArrayList<>();
        try {
            result = winRegReader.getSubkeys(mountPoints2).stream()
                    .filter(e -> e.matches(".+\\{[a-fA-F0-9-]+}"))
                    .map(e -> e.substring(e.lastIndexOf("{")))
                    .toList();
        } catch (IOException | InterruptedException e) {
            LOGGER.error("ОШИБКА. Не удалось получить GUID'ы устройств, используемых ТЕКУЩИМ пользователем. Причина: {}",
                    e.getLocalizedMessage());
            LOGGER.debug("Не удалось обработать {}", mountPoints2, e);
            Thread.currentThread().interrupt();
        }
        return result;
    }

    /**
     * <p>Получение GUID устройств подключенных указанным пользователем. Данные берутся из загруженного соответствующего
     * куста NTUSER.DAT.</p>
     * <p>Если указан профиль текущего пользователя, то вызывается метод {@link #getMountedGUIDsOfCurrentUser() } </p>
     *
     * @param userProfile профиль пользователя, из которого необходимо получить GUID'ы.
     * @return список GUID всех когда-либо подключенных указанным пользователем устройств.
     */
    public List<String> getMountedGUIDsOfUser(UserProfile userProfile) {
        String currentUserHomedir = System.getProperty("user.home");
        String profileHomedir = userProfile.getProfileImagePath().toString();
        if (profileHomedir.equals(currentUserHomedir)) {
            return getMountedGUIDsOfCurrentUser();
        }

        List<String> guidList = new ArrayList<>();
        String username = userProfile.getUsername().replaceAll("[\\s\\.-]+", "");
        String nodeName = "HKEY_LOCAL_MACHINE\\userHive_" + username;
        String userHive = userProfile.getProfileImagePath().toString() + "\\NTUSER.DAT";
        try {
            winRegReader.loadHive(nodeName, userHive);

            guidList = winRegReader.getSubkeys(nodeName + "\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\MountPoints2").stream()
                    .filter(e -> e.matches(".+\\{[a-fA-F0-9-]+}"))
                    .map(e -> e.substring(e.lastIndexOf("{")))
                    .toList();
            winRegReader.unloadHive(nodeName);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            LOGGER.error("ОШИБКА. Не удалось получить GUID'ы устройств, используемых пользователем {}. Причина: {}",
                    username, e.getLocalizedMessage());
            LOGGER.debug("{}", e.toString());
            Thread.currentThread().interrupt();
        }
        return guidList;
    }

    /**
     * Анализ ветки {@code HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows NT\CurrentVersion\EMDMgmt}. Ветка реестра ведется службой ReadyBoost.
     * В нее записывается информация о всех подключаемых к системе устройствах, что служит неплохим источником данных для дальнейшего анализа.
     * Анализ ветки выполняется на случай, если лог файлы или другие ветки были почищены - получим хоть какую-то информацию об устройствах.
     *
     * @return мапу устройств, когда-либо подключенных к системе и опрошенных/протестированных службой ReadyBoost.
     */
    public Map<String, USBDevice> getReadyBoostDevices() {
        try {
            List<String> keyList = winRegReader.getSubkeys(REG_KEY_EMDMGMT);
//            VOL [диск:]
            for (String key : keyList) {
                parseReadyBoostKey(key);
            }

        } catch (IOException | InterruptedException e) {
            LOGGER.error("Анализ ветки {} прерван: {}", REG_KEY_EMDMGMT, e.getLocalizedMessage());
            LOGGER.debug("Анализ ветки {} прерван", REG_KEY_EMDMGMT, e);
            Thread.currentThread().interrupt();
        }
        return usbDeviceMap;
    }

    /**
     * Получить список USB устройств когда-либо подключенных к АРМ.
     * Информация берется из HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Enum\USB
     *
     * @return список USB устройств, когда-либо подключенных и зарегистрированных в ОС.
     */
    public Map<String, USBDevice> getUsbDevices() {
        try {
            List<String> pidVidList = winRegReader.getSubkeys(REG_KEY_USB);

            for (String pidvid : pidVidList) {
                List<String> listSerialKeys = winRegReader.getSubkeys(pidvid);
                for (String serialKey : listSerialKeys) {
                    String pid = parsePid(pidvid.toLowerCase()).orElse("<N/A>");
                    String vid = parseVid(pidvid.toLowerCase()).orElse("<N/A>");
                    String[] tmpArr = serialKey.split("\\\\");
                    String serial = tmpArr[tmpArr.length - 1];

                    USBDevice currUsbDev = new USBDevice()
                            .setSerial(serial)
                            .setVidPid(vid, pid);

                    usbDeviceMap.merge(serial, currUsbDev, (dst, src) -> {
                        dst.setSerial(src.getSerial());
                        dst.setPid(src.getPid());
                        dst.setProductName(src.getProductName());
                        dst.setVid(src.getVid());
                        dst.setVendorName(src.getVendorName());
                        return dst;
                    });
                }
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("ОШИБКА. Не удалось получить список устройств из HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Enum\\USB. " +
                    "Причина: {}", e.getLocalizedMessage());
            LOGGER.debug("ОШИБКА. Не удалось получить список устройств из HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Enum\\USB. ", e);
            Thread.currentThread().interrupt();
        }
        return usbDeviceMap;
    }

    /**
     * <p>Читает пути к профилям пользователей из HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows NT\CurrentVersion\ProfileList.
     * По указанному пути будут соответствующие записи только если пользователь хотя бы раз логинился. Этого вполне достаточно,
     * т.к. если пользователь ниразу не логинился в системе, то он не мог использовать USB устройства.</p>
     *
     * <p>В качестве альтернативного решения задачи определения имени пользователя по SID может служить команда
     * <code>wmic useraccount where sid="" get name</code>. Подробности можно найти, например,
     * по <a href="https://www.lifewire.com/how-to-find-a-users-security-identifier-sid-in-windows-2625149">ссылке</a></p>
     *
     * @return список созданных профилей пользователей
     */
    public List<UserProfile> getUserProfileList() {
        List<UserProfile> userProfileList = new ArrayList<>();
        try {
            List<String> profileRegKeys = winRegReader.getSubkeys(REG_KEY_PROFILE_LIST);
            userProfileList = profileRegKeys.stream()
                    .map(profile -> {
                        String profileImagePath = winRegReader.getValue(profile, "ProfileImagePath").orElseThrow();
                        String username = profileImagePath.substring(profileImagePath.lastIndexOf("\\") + 1);
                        String sid = profile.substring(profile.lastIndexOf("\\") + 1);

                        UserProfile.Builder builder = UserProfile.getBuilder();
                        return builder
                                .withProfileImagePath(Path.of(profileImagePath))
                                .withSecurityId(sid)
                                .withUsername(username)
                                .build();
                    })
                    .toList();
        } catch (IOException | InterruptedException e) {
            LOGGER.error("ОШИБКА. Не удалось получить список профилей в системе. Причина: {}", e.getLocalizedMessage());
            LOGGER.debug("ОШИБКА. Не удалось получить список профилей в системе.", e);
            Thread.currentThread().interrupt();
        }
        return userProfileList;
    }

    public boolean isDoNewAnalysis() {
        return doNewAnalysis;
    }

    public RegistryAnalyzer setDoNewAnalysis(boolean doNewAnalysis) {
        this.doNewAnalysis = doNewAnalysis;
        return this;
    }

    /**
     * Проверить относится ли запись к разделу несъемного HDD/SDD
     *
     * @param hexValue - проверяемое значение - строка, содержащая HEX-символы
     * @return true - если строка начинается с HEX-сигнатуры {@code 444D494F3A49443A}, false - во всех остальных случаях.
     */
    private boolean isHDDPartition(String hexValue) {
        final String signature = "444D494F3A49443A"; //TODO есть подозрение, что это сигнатура свойственна только GPT-дискам.
        return hexValue.startsWith(signature);
    }

    /**
     * @param pidvid строка, содержащая в себе подстроку вида {@code pid_VVVV&PID_PPPP}
     * @return значение PID (ProductID)
     */
    private Optional<String> parsePid(String pidvid) {
        Matcher matcher = Pattern.compile("(?i)pid_(.{4})").matcher(pidvid);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    /**
     * Парсинг подключа {@code HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows NT\CurrentVersion\EMDMgmt}.
     * По результату парсинга/анализа мапа устройств заполняется данными об устройстве:
     * <ul>
     *     <li>VendorNameByRegistry;</li>
     *     <li>ProductNameByRegistry;</li>
     *     <li>Revision;</li>
     *     <li>VolumeLabel (список дополняется уникальными метками тома);</li>
     *     <li>VolumeId (в список добавляются все ID тома).</li>
     * </ul>
     *
     * @param key анализируемый подраздел ветки {@code HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows NT\CurrentVersion\EMDMgmt}
     */
    private void parseReadyBoostKey(String key) {
        try {
            String pattern = ".*&Ven_(?<ven>\\p{Graph}*)&Prod_(?<prod>[\\p{Graph}&&[^&]]*)(&Rev_(?<rev>\\p{Graph}*))?#(?<serial>\\w&?\\w*)(&\\d)?.*}(?<volLabel>\\p{Print}*)_(?<volId>\\d*)";
            Matcher matcher = Pattern.compile(pattern).matcher(key);
            boolean matcherFindResult = matcher.find();
            if (matcherFindResult) {
                String ven = matcher.group("ven");
                String prod = matcher.group("prod");
                String rev = matcher.group("rev");
                String serial = matcher.group("serial");
                String volLabel = matcher.group("volLabel");
                long volumeId = Long.parseLong(matcher.group("volId"));
                USBDevice tmp = new USBDevice()
                        .setRevision(rev)
                        .setSerial(serial)
                        .setProductNameByRegistry(prod)
                        .setVendorNameByRegistry(ven)
                        .addVolumeLabel(volLabel)
                        .addVolumeId(volumeId);

                usbDeviceMap.merge(serial, tmp, (dst, src) -> {
                    dst.addVolumeLabel(volLabel);
                    dst.addVolumeId(volumeId);
                    dst.setVendorNameByRegistry(src.getVendorNameByRegistry());
                    dst.setProductNameByRegistry(src.getProductNameByRegistry());
                    dst.setRevision(src.getRevision());
                    return dst;
                });
            } else {
                LOGGER.warn("Запись пропущена - не соответствует паттерну:\n\t\tKey = {}", key);
            }
        } catch (IllegalStateException | NoSuchElementException e) {
            LOGGER.warn("Запись об устройстве не удалось распознать.\n\tЗапись: {}\n\tПричина: {}", key, e.getLocalizedMessage());
            LOGGER.debug("Запись об устройстве не удалось распознать.\n\tЗапись: {}", key, e);
        }
    }

    /**
     * @param pidvid строка, содержащая в себе подстроку вида {@code vid_VVVV&PID_PPPP}
     * @return значение VID (VendorID)
     */
    private Optional<String> parseVid(String pidvid) {
        Matcher matcher = Pattern.compile("(?i)vid_(.{4})").matcher(pidvid);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    /**
     * Парсит раздел реестра Windows {@code HKLM\SOFTWARE\Microsoft\Windows Portable Devices} на наличие меток
     * смонтированных разделов (томов) на известных устройствах.
     *
     * @return мапу, наполненную серийным номером устройства и списком наименований разделов (томов) на нем.
     */
    public Map<String, USBDevice> parseWindowsPortableDevice() {
        String wpdKey = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows Portable Devices\\Devices";
        try {
            List<String> deviceList = winRegReader.getSubkeys(wpdKey);
            for (String deviceEntry : deviceList) {
                Optional<String> serial = Optional.empty();
                try {
                    serial = usbDeviceMap.keySet().stream()
                            .filter(s -> deviceEntry.matches("(?i).*#" + s + "[#&].*"))
                            .findFirst();
                } catch (PatternSyntaxException e) {
                    //Т.к. regexp формируется за счет строковой переменной, которая может в себе содержать весь набор символов,
                    // в том числе и символы, влияющие на обработку выражения, необходимо учесть высокую вероятность
                    // построения некорректного regexp.
                    serial = usbDeviceMap.keySet().stream()
                            .filter(s -> deviceEntry.contains("#" + s))
                            .findFirst();
                    LOGGER.warn("[W] При определении метки тома {} использовался метод contains() вместо matches", serial.orElse("***"));
                    LOGGER.debug("[D] При определении метки тома {} использовался метод contains() вместо matches", serial.orElse("***"), e);
                }
                if (serial.isPresent()) {
                    String volumeName = winRegReader.getValue(deviceEntry, "FriendlyName").orElseThrow();
                    USBDevice tmp = new USBDevice()
                            .setSerial(serial.get())
                            .addVolumeLabel(volumeName);

                    usbDeviceMap.merge(serial.get(), tmp, (dst, src) -> {
                        dst.addVolumeLabel(volumeName);
                        return dst;
                    });
                } else {
                    //FIXME код говно. переписать.
                    List<String> stringList = Arrays.stream(deviceEntry.split("#")).toList();
                    String newPid = "";
                    String newVid = "";
                    try {
                        newVid = stringList.stream()
                                .filter(elem -> elem.matches("(?i).*vid_.*"))
                                .map(elem -> parseVid(elem).orElseThrow())
                                .findFirst().orElseThrow();
                        newPid = stringList.stream()
                                .filter(elem -> elem.matches("(?i).*pid_.*"))
                                .map(elem -> parsePid(elem).orElseThrow())
                                .findFirst().orElseThrow();
                    } catch (NoSuchElementException e) {
                        LOGGER.warn("Не удалось определить VID/PID в записи {}", deviceEntry);
                        LOGGER.debug("Не удалось определить VID/PID в записи {}", deviceEntry, e);
                    }
                    String newSerial = stringList.get(stringList.size() - 1);
                    String volumeName = winRegReader.getValue(deviceEntry, "FriendlyName").orElseThrow();
                    USBDevice tmp = new USBDevice()
                            .setVidPid(newVid, newPid)
                            .setSerial(newSerial)
                            .addVolumeLabel(volumeName);
                    usbDeviceMap.merge(newSerial, tmp, (dst, src) -> {
                        dst.setSerial(src.getSerial());
                        dst.setPid(src.getPid());
                        dst.setVid(src.getVid());
                        dst.setProductName(src.getProductName());
                        dst.setVendorName(src.getVendorName());
                        return dst;
                    });

                }
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("ОШИБКА. Не удалось получить метку для устройства. Причина: {}", e.getLocalizedMessage());
            LOGGER.debug("ОШИБКА. Не удалось получить метку для устройства.", e);
            Thread.currentThread().interrupt();
        }

        return usbDeviceMap;
    }
}
