package ru.pel.usbddc.entity;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pel.usbddc.config.UsbddcConfig;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Класс предназначен для описания USB устройства.
 * Хранит в себе сведения об устройстве, полученные из:
 * <ul>
 *     <li>реестра</li>
 *     <li>setupapi.dev.log</li>
 * </ul>
 */

public class USBDevice {
    private static final Logger LOGGER = LoggerFactory.getLogger(USBDevice.class.getName());
    @Getter
    @Setter
    private static String usbIds = UsbddcConfig.getInstance().getUsbIdsPath();
    private String friendlyName = "";
    private String guid = "";
    private String pid = "";
    private String productName = "";
    private String productNameByRegistry = "";
    private String serial = "";
    private String vendorName = "";
    private String vendorNameByRegistry = "";
    private String vid = "";
    private String revision = "";
    /**
     * Источник данных: {@code HKLM\SYSTEM\CurrentControlSet\Enum\USBSTOR\<XXX>\<SERIAL>\Device Parameters\Partmgr} параметр {@code DiskId}
     */
    private String diskId = "";
    private LocalDateTime dateTimeFirstInstall = LocalDateTime.MIN;
    private boolean isSerialOSGenerated = true;
    /**
     * Список серийных номеров томов, с которыми устройство когда-либо подключалось к системе.
     * При форматировании, как правило, меняется. Источник: последняя (после символа "_") цифра каждого подраздела ветки
     * {@code HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows NT\CurrentVersion\EMDMgmt}
     */
    private List<Long> volumeIdList = new ArrayList<>();
    private List<UserProfile> userAccountsList = new ArrayList<>();
    private Set<String> volumeLabelList = new HashSet<>();

    public USBDevice() {
    }

//    public static Builder getBuilder() {
//        return new Builder();
//    }

    public USBDevice addUserProfile(UserProfile userProfile) {
        userAccountsList.add(userProfile);
        return this;
    }

    public USBDevice addVolumeId(long volumeId) {
        volumeIdList.add(volumeId);
        return this;
    }

    public USBDevice addVolumeLabel(String volumeLabel) {
        volumeLabelList.add(volumeLabel);
        return this;
    }

    //См. https://github.com/Quitest/USBDDx/issues/47
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        USBDevice usbDevice = (USBDevice) o;
        return isSerialOSGenerated == usbDevice.isSerialOSGenerated && Objects.equals(friendlyName, usbDevice.friendlyName) && Objects.equals(guid, usbDevice.guid) && Objects.equals(pid, usbDevice.pid) && Objects.equals(productName, usbDevice.productName) && Objects.equals(serial, usbDevice.serial) && Objects.equals(vendorName, usbDevice.vendorName) && Objects.equals(vid, usbDevice.vid) && Objects.equals(volumeLabelList, usbDevice.volumeLabelList) && Objects.equals(revision, usbDevice.revision) && Objects.equals(userAccountsList, usbDevice.userAccountsList);
    }

    public LocalDateTime getDateTimeFirstInstall() {
        return dateTimeFirstInstall;
    }

    public USBDevice setDateTimeFirstInstall(LocalDateTime dateTimeFirstInstall) {
        this.dateTimeFirstInstall = dateTimeFirstInstall;
        return this;
    }

    public String getDiskId() {
        return diskId;
    }

    public USBDevice setDiskId(String diskId) {
        this.diskId = diskId;
        return this;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public USBDevice setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
        return this;
    }

    public String getGuid() {
        return guid;
    }

    public USBDevice setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    public String getPid() {
        return pid;
    }

    public USBDevice setPid(String pid) {
        this.pid = pid;
        return this;
    }

    public String getProductName() {
        return productName;
    }

    public USBDevice setProductName(String productName) {
        this.productName = productName;
        return this;
    }

    public String getProductNameByRegistry() {
        return productNameByRegistry;
    }

    public USBDevice setProductNameByRegistry(String productNameByRegistry) {
        this.productNameByRegistry = productNameByRegistry;
        return this;
    }

    public String getRevision() {
        return revision;
    }

    public USBDevice setRevision(String revision) {
        this.revision = revision;
        return this;
    }

    public String getSerial() {
        return serial;
    }

    /**
     * Устанавливает значение serial (серийный номер), а также isSerialOSGenerated в значение false, если второй
     * символ & (признак того что значение сгенерировано ОС и оно уникально только в рамках текущей ОС), true - в
     * противном случае.
     *
     * @param serial серийный номер устройства
     * @return возвращает билдер
     */
    public USBDevice setSerial(String serial) {
        this.serial = Objects.requireNonNullElse(serial, "");
        //бывает что серийника нет вообще или при чтении данных серийником выступает набор бит, что бы корректно
        // найти и сравнить символ '&' введена проверка.
        if (this.serial.isBlank()) {
            isSerialOSGenerated = false;
        } else {
            isSerialOSGenerated = this.serial.charAt(1) == '&';
        }
        return this;
    }

    public List<UserProfile> getUserAccountsList() {
        return userAccountsList;
    }

    public USBDevice setUserAccountsList(List<UserProfile> userAccountsList) {
        this.userAccountsList = userAccountsList;
        return this;
    }

    public String getVendorName() {
        return vendorName;
    }

    public USBDevice setVendorName(String vendorName) {
        this.vendorName = vendorName;
        return this;
    }

    public String getVendorNameByRegistry() {
        return vendorNameByRegistry;
    }

    public USBDevice setVendorNameByRegistry(String vendorNameByRegistry) {
        this.vendorNameByRegistry = vendorNameByRegistry;
        return this;
    }

    public String getVid() {
        return vid;
    }

    public USBDevice setVid(String vid) {
        this.vid = vid;
        return this;
    }

    public List<Long> getVolumeIdList() {
        return volumeIdList;
    }

    public USBDevice setVolumeIdList(List<Long> volumeIdList) {
        this.volumeIdList = volumeIdList;
        return this;
    }

    public Set<String> getVolumeLabelList() {
        return volumeLabelList;
    }

    public USBDevice setVolumeLabelList(Set<String> volumeLabelList) {
        this.volumeLabelList = volumeLabelList;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(friendlyName, guid, pid, productName, serial, vendorName, vid, volumeLabelList, revision, isSerialOSGenerated, userAccountsList);
    }

    private boolean isNecessaryMerge(@NotNull String src) {
        return /*src != null &&*/ !src.isBlank();
    }

    private boolean isNecessaryMerge(@NotNull LocalDateTime src) {
        return src.isBefore(dateTimeFirstInstall) &&
//                src != null &&
                dateTimeFirstInstall.isEqual(LocalDateTime.MIN) &&
                !src.isEqual(LocalDateTime.MIN);
    }

    public boolean isSerialOSGenerated() {
        return isSerialOSGenerated;
    }

    public USBDevice setSerialOSGenerated(boolean serialOSGenerated) {
        isSerialOSGenerated = serialOSGenerated;
        return this;
    }

    /**
     * <p>Выполняет копирование свойств из src в текущий объект. Свойства равные null в источнике игнорируются - в
     * текущем объекте свойство остается неизменным.</p>
     *
     * <p><strong>ВНИМАНИЕ!</strong> В случае изменения состава свойств необходимо руками исправлять копирование.
     * При использовании BeanUtilsBean и PropertyUtilsBean от org.apache.commons копирование свойств не происходит,
     * т.к. используются цепные сеттеры (chain setters). При использовании сеттеров с возвращаемым типом void они
     * работают отлично.
     * </p>
     *
     * @param src Устройство, свойства которого необходимо скопировать.
     * @return текущее устройство со свойствами, обновленными из src, если не произошло ошибок, иначе возвращает объект
     * в исходном состоянии.
     * @throws UnsupportedOperationException if the addAll operation is not supported by this list
     * @throws ClassCastException            if the class of an element of the specified collection prevents it from being added to this list
     * @throws NullPointerException          if the specified collection contains one or more null elements and this list does not permit null elements, or if the specified collection is null
     * @throws IllegalArgumentException      if some property of an element of the specified collection prevents it from being added to this list
     * @throws IllegalStateException         if not all the elements can be added at this time due to insertion restrictions
     *                                       <p><strong>Примечание:</strong> выше упомянутые исключения относятся к операциям добавления элементов в коллекции.</p>
     */
    public USBDevice mergeProperties(USBDevice src) {
        LOGGER.debug("Начата процедура слияния свойств устройств с серийными номерами [{}] <-- [{}]", getSerial(), src.getSerial());
/*        try {
            // PropertyUtilsBean() при отсутствии необходимости преобразования типов быстрее
//            new IgnoreNullBeanUtilsBean().copyProperties(this, src);
            new IgnoreNullPropertyUtilsBean().copyProperties(this, src);
            LOGGER.debug("Поля объекта {} скопированы в объект {}", src, this);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            LOGGER.error("ОШИБКА копирования свойств. Причина: {}", e.getLocalizedMessage());
            LOGGER.debug("{}", e);
        }
        */

        //TODO в следующий подход к методу или с ростом навыков эту лапшу заменить на более грамотный код.
        //Далее идет лапша из однотипных блоков кода:
        // 1. Получаем значение свойства из источника.
        // 2. Проверяем его на необходимость слияния с приемником (текущий объект).
        // 3. Выполняем слияние: для примитивов - замена значения, для коллекций - добавление всех элементов из источника в приемник.
        // 4. Логирование с уровнем TRACE
        String srcFriendlyName = src.getFriendlyName();
        if (isNecessaryMerge(srcFriendlyName)) {
            friendlyName = srcFriendlyName;
            LOGGER.trace("Добавлено в свойство {} значение {}", "friendlyName", srcFriendlyName);
        }
        String srcGuid = src.getGuid();
        if (isNecessaryMerge(srcGuid)) {
            guid = srcGuid;
            LOGGER.trace("Добавлено в свойство {} значение {}", "guid", srcGuid);
        }
        String srcPid = src.getPid();
        if (isNecessaryMerge(srcPid)) {
            this.pid = srcPid;
            LOGGER.trace("Добавлено в свойство {} значение {}", "pid", srcPid);
        }
        String srcProductName = src.getProductName();
        if (isNecessaryMerge(srcProductName)) {
            this.productName = srcProductName;
            LOGGER.trace("Добавлено в свойство {} значение {}", "productName", srcProductName);
        }
        String srcProductNameByRegistry = src.getProductNameByRegistry();
        if (isNecessaryMerge(srcProductNameByRegistry)) {
            this.productNameByRegistry = srcProductNameByRegistry;
            LOGGER.trace("Добавлено в свойство {} значение {}", "productNameByRegistry", srcProductNameByRegistry);
        }
        String srcSerial = src.getSerial();
        if (isNecessaryMerge(srcSerial)) {
            this.serial = srcSerial;
            LOGGER.trace("Добавлено в свойство {} значение {}", "serial", srcSerial);
        }
        String srcVendorName = src.getVendorName();
        if (isNecessaryMerge(srcVendorName)) {
            this.vendorName = srcVendorName;
            LOGGER.trace("Добавлено в свойство {} значение {}", "vendorName", srcVendorName);
        }
        String srcVendorNameByRegistry = src.getVendorNameByRegistry();
        if (isNecessaryMerge(srcVendorNameByRegistry)) {
            this.vendorNameByRegistry = srcVendorNameByRegistry;
            LOGGER.trace("Добавлено в свойство {} значение {}", "vendorNameByRegistry", srcVendorNameByRegistry);
        }
        String srcVid = src.getVid();
        if (isNecessaryMerge(srcVid)) {
            this.vid = srcVid;
            LOGGER.trace("Добавлено в свойство {} значение {}", "vid", srcVid);
        }
        String srcRevision = src.getRevision();
        if (isNecessaryMerge(srcRevision)) {
            this.revision = srcRevision;
            LOGGER.trace("Добавлено в свойство {} значение {}", "revision", srcRevision);
        }
        String srcDiskId = src.getDiskId();
        if (isNecessaryMerge(srcDiskId)) {
            this.diskId = srcDiskId;
            LOGGER.trace("Добавлено в свойство {} значение {}", "diskId", srcDiskId);
        }
        LocalDateTime srcDateTimeFirstInstall = src.getDateTimeFirstInstall();
        if (isNecessaryMerge(srcDateTimeFirstInstall)) {
            this.dateTimeFirstInstall = srcDateTimeFirstInstall;
            LOGGER.trace("Добавлено в свойство {} значение {}", "dateTimeFirstInstall", srcDateTimeFirstInstall);
        }

        isSerialOSGenerated = src.isSerialOSGenerated();
        LOGGER.trace("Добавлено в свойство {} значение {}", "isSerialGenerated", src.isSerialOSGenerated());
        //Выполняется прямое добавление элементов без проверки, потому что согласно документации на метод addAll(Collection c):
        // 1. NPE выбрасывается, если src == null
        // 2. NPE выбрасывается, если в src есть элементы равные null, а приемник не допускает наличие null-элементов.
        // 3. Реализация метода ArrayList.addAll предусматривает раннее прекращение работы src.size == 0.
        volumeIdList.addAll(src.getVolumeIdList());
        LOGGER.trace("Добавлено в свойство {} значение {}", "volumeIdList", src.getVolumeIdList());
        userAccountsList.addAll(src.getUserAccountsList());
        LOGGER.trace("Добавлено в свойство {} значение {}", "userAccountList", src.getUserAccountsList());
        volumeLabelList.addAll(src.getVolumeLabelList());
        LOGGER.trace("Добавлено в свойство {} значение {}", "volumeLabelList", src.getVolumeLabelList());
        LOGGER.debug("Закончена процедура слияния свойств устройств с серийными номерами [{}] <-- [{}]", getSerial(), src.getSerial());
        return this;
    }

    /**
     * Устанавливает значения VID/PID. При наличии файла usb.ids автоматически заполняет vendorName и productName
     *
     * @param vid Vendor ID
     * @param pid Product ID
     * @return возвращает билдер.
     */
    public USBDevice setVidPid(String vid, String pid) {
        //WTF почему если использовать две строки ниже вместо двух IF'ов выше, то null прорывается до vid.matches()?
        this.vid = Objects.requireNonNullElse(vid, "");
        this.pid = Objects.requireNonNullElse(pid, "");
        String regexVidPid = "[0-9a-fA-F]{4}";
        if (!this.vid.matches(regexVidPid) || !this.pid.matches(regexVidPid)) {
            return this;
        }
        this.productName = "";
        this.vendorName = "";

        try (BufferedReader usbIdsReader = new BufferedReader(new FileReader(USBDevice.usbIds))) {
            String currStr = "";
            boolean vendorFound = false;
            //TODO код лажа - переписать на нормальный.
            while (currStr != null) {
                if (currStr.matches("(?i)^" + this.vid + ".+")) { //текущая строка содержит VendorID? Т.о. отслеживаем начало блока вендора
                    this.vendorName = currStr.split(" {2}")[1];// делитель - два пробела, т.о.:
                    // [0] - vid
                    // [1] - vendor name (имя производителя)
                    vendorFound = true;
                    currStr = usbIdsReader.readLine();
                    continue;
                }
                if (vendorFound && currStr.matches("(?i)\\t" + this.pid + ".+")) {//блок вендора начат и строка содержит ProductID?
                    this.productName = currStr.split(" {2}")[1];
                    break;
                }
                if (vendorFound && currStr.matches("\\[0-9a-fA-F]{4}\\s+.+")) { //начался блок следующего вендора?
                    this.productName = "";
                    break;
                }
                currStr = usbIdsReader.readLine();
            }
        } catch (IOException e) {
            LOGGER.warn("Не удалось определить название производителя и имя продукта для {}/{} по причине: {}",
                    this.vid, this.vid, e.getLocalizedMessage());
            LOGGER.debug("{}", e.toString());
            this.productName = "undef.";
            this.vendorName = "undef.";
        }
        return this;
    }

    @Override
    public String toString() {
        //Получение всех полей экземпляра и вывод их реализован при помощи методов reflection.
        //Почему? Да просто захотелось попробовать эту рефлексию. Плюс количество полей класса может меняться, и что бы
        //не лазить в метод лишний раз, решено автоматизировать немного.
        final String LINE_SEPARATOR = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        try {
            Field[] fieldsThis = USBDevice.class.getDeclaredFields();
            List<Field> fields = new ArrayList<>(Arrays.asList(fieldsThis));
            fields.sort(Comparator.comparing(Field::getName));

            for (Field field : fields) {
                sb.append(field.getName())
                        .append(" = ")
                        .append(field.get(this))
                        .append(LINE_SEPARATOR);
            }

        } catch (IllegalAccessException e) {
            LOGGER.error("Не возможно получить доступ к свойству объекта: {}", e.getLocalizedMessage());
            LOGGER.debug("{}", e.toString());
        }
        return sb.toString();
    }

//    /**
//     * "Строитель" объектов типа USBDevice. Особенность строителя - при передаче в качестве аргументов значений null, в
//     * поля записываются не нулевые значения (пустые строки, списки нулевой длинны, дата и время минимального значения).
//     */
//    public static class Builder {
//        private final USBDevice newUsbDevice;

//        private Builder() {
//            newUsbDevice = new USBDevice();
//        }

//        /**
//         * Добавить дополнительный ID (серийный номер) тома диска.
//         *
//         * @param volumeId ID (серийный номер) тома.
//         * @return
//         */
//        public Builder addVolumeId(long volumeId) {
//            newUsbDevice.volumeIdList.add(volumeId);
//            return this;
//        }
//
//        public Builder addVolumeLabel(String volumeName) {
//            newUsbDevice.volumeLabelList.add(Objects.requireNonNullElse(volumeName, ""));
//            return this;
//        }

//        public USBDevice build() {
//            return newUsbDevice;
//        }

//        /**
//         * @param fieldName
//         * @param value
//         * @deprecated Рефлексия в данном случае = выстрел в ногу.
//         */
//        @Deprecated(forRemoval = true)
//        public void setField(String fieldName, Object value) {
//            //Инфа: https://javarush.ru/groups/posts/513-reflection-api-refleksija-temnaja-storona-java
//            //try-catch навалены друг на друга, надо переделать в более простую логику.
//            char[] chars = fieldName.toCharArray();
//            chars[0] = Character.toLowerCase(chars[0]);
//            fieldName = new String(chars);
//
//            Field field = null;
//            try {
//                field = USBDevice.Builder.class.getDeclaredField(fieldName);
//            } catch (NoSuchFieldException e) {
//                LOGGER.warn("WARN: {} = {}", e, value);
//            }
//            if (field != null) {
//                field.setAccessible(true);
//                try {
//                    field.set(this, value);
//                } catch (IllegalAccessException illegalAccessException) {
//                    illegalAccessException.printStackTrace();
//                }
//            }
//        }

//        public Builder withDateTimeFirstInstall(LocalDateTime dateTime) {
//            newUsbDevice.dateTimeFirstInstall = Objects.requireNonNullElse(dateTime, LocalDateTime.MIN);
//            return this;
//        }
//
//        //TODO Скорее всего логику по определению poductName и vendorName разумно вынести во вне, что бы за одно чтение
//        // файла можно было получить все необходимые PID/VID. Неплохое место, на первый взгляд - серверная часть.
//
//        public Builder withDiskId(String diskId) {
//            newUsbDevice.diskId = Objects.requireNonNullElse(diskId, "");
//            return this;
//        }
//
//        public Builder withFriendlyName(String friendlyName) {
//            newUsbDevice.friendlyName = Objects.requireNonNullElse(friendlyName, "");
//            return this;
//        }
//
//        public Builder withGuid(String guid) {
//            newUsbDevice.guid = Objects.requireNonNullElse(guid, "");
//            return this;
//        }
//
//        public Builder withProductNameByRegistry(String productName) {
//            newUsbDevice.productNameByRegistry = productName;
//            return this;
//        }
//
//        public Builder withRevision(String rev) {
//            newUsbDevice.revision = Objects.requireNonNullElse(rev, "");
//            return this;
//        }

//        /**
//         * Устанавливает значение serial (серийный номер), а также isSerialOSGenerated в значение false, если второй
//         * символ & (признак того что значение сгенерировано ОС и оно уникально только в рамках текущей ОС), true - в
//         * противном случае.
//         *
//         * @param serial серийный номер устройства
//         * @return возвращает билдер
//         */
//        public Builder withSerial(String serial) {
//            newUsbDevice.serial = Objects.requireNonNullElse(serial, "");
//            //бывает что серийника нет вообще или при чтении данных серийником выступает набор бит, что бы корректно
//            // найти и сравнить символ '&' введена проверка.
//            if (newUsbDevice.serial.isBlank()) {
//                newUsbDevice.isSerialOSGenerated = false;
//            } else {
//                newUsbDevice.isSerialOSGenerated = serial.charAt(1) == '&';
//            }
//            return this;
//        }

//        public Builder withUserProfileList(List<UserProfile> userProfileList) {
//            newUsbDevice.userAccountsList = Objects.requireNonNullElse(userProfileList, new ArrayList<>());
//            return this;
//        }
//
//        public Builder withVendorNameByRegistry(String vendorName) {
//            newUsbDevice.vendorNameByRegistry = vendorName;
//            return this;
//        }

//        /**
//         * Устанавливает значения VID/PID. При наличии файла usb.ids автоматически заполняет vendorName и productName
//         *
//         * @param vid Vendor ID
//         * @param pid Product ID
//         * @return возвращает билдер.
//         */
//        public Builder withVidPid(String vid, String pid) {
//            //WTF почему если использовать две строки ниже вместо двух IF'ов выше, то null прорывается до vid.matches()?
//            newUsbDevice.vid = Objects.requireNonNullElse(vid, "");
//            newUsbDevice.pid = Objects.requireNonNullElse(pid, "");
//            String regexVidPid = "[0-9a-fA-F]{4}";
//            if (!newUsbDevice.vid.matches(regexVidPid) || !newUsbDevice.pid.matches(regexVidPid)) {
//                return this;
//            }
//            newUsbDevice.productName = "";
//            newUsbDevice.vendorName = "";
//
//            try (BufferedReader usbIdsReader = new BufferedReader(new FileReader(USBDevice.usbIds))) {
//                String currStr = "";
//                boolean vendorFound = false;
//                //TODO код лажа - переписать на нормальный.
//                while (currStr != null) {
//                    if (currStr.matches("(?i)^" + newUsbDevice.vid + ".+")) { //текущая строка содержит VendorID? Т.о. отслеживаем начало блока вендора
//                        newUsbDevice.vendorName = currStr.split(" {2}")[1];// делитель - два пробела, т.о.:
//                        // [0] - vid
//                        // [1] - vendor name (имя производителя)
//                        vendorFound = true;
//                        currStr = usbIdsReader.readLine();
//                        continue;
//                    }
//                    if (vendorFound && currStr.matches("(?i)\\t" + newUsbDevice.pid + ".+")) {//блок вендора начат и строка содержит ProductID?
//                        newUsbDevice.productName = currStr.split(" {2}")[1];
//                        break;
//                    }
//                    if (vendorFound && currStr.matches("\\[0-9a-fA-F]{4}\\s+.+")) { //начался блок следующего вендора?
//                        newUsbDevice.productName = "";
//                        break;
//                    }
//                    currStr = usbIdsReader.readLine();
//                }
//            } catch (IOException e) {
//                LOGGER.warn("Не удалось определить название производителя и имя продукта для {}/{} по причине: {}",
//                        newUsbDevice.vid, newUsbDevice.vid, e.getLocalizedMessage());
//                LOGGER.debug("{}", e.toString());
//                newUsbDevice.productName = "undef.";
//                newUsbDevice.vendorName = "undef.";
//            }
//            return this;
//        }
//    }
}
