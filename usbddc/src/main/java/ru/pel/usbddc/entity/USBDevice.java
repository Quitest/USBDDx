package ru.pel.usbddc.entity;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pel.usbddc.config.UsbddcConfig;
import ru.pel.usbddc.service.IgnoreNullBeanUtilsBean;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Класс предназначен для описания USB устройства.
 * Хранит в себе сведения об устройстве, полученные из:
 * <ul>
 *     <li>реестра</li>
 *     <li>setupapi.dev.log (прим.: в текущей версии сведения не берутся из него)</li>
 * </ul>
 */

@Getter
@Setter
public class USBDevice {
    private static final Logger LOGGER = LoggerFactory.getLogger(USBDevice.class.getName());
    @Getter
    @Setter
    private static String usbIds = UsbddcConfig.getInstance().getUsbIdsPath();
    private String friendlyName = "";
    private String guid = "";
    private String pid = "";
    private String productName = "";
    private String serial = "";
    private String vendorName = "";
    private String vid = "";
    private List<String> volumeLabelList = new ArrayList<>();
    private String revision = "";
    /**
     * Источник данных: {@code HKLM\SYSTEM\CurrentControlSet\Enum\USBSTOR\<XXX>\<SERIAL>\Device Parameters\Partmgr} параметр {@code DiskId}
     */
    private String diskId = "";
    /**
     * Список серийных номеров томов, с которыми устройство когда-либо подключалось к системе.
     * При форматировании, как правило, меняется. Источник: последняя (после символа "_") цифра каждого подраздела ветки
     * {@code HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows NT\CurrentVersion\EMDMgmt}
     */
    private List<Integer> volumeIdList = new ArrayList<>();
    private LocalDateTime dateTimeFirstInstall = LocalDateTime.MIN;
    private boolean isSerialOSGenerated = true;
    private List<UserProfile> userAccountsList = new ArrayList<>();

    private USBDevice() {
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public void addUserProfile(UserProfile userProfile) {
        userAccountsList.add(userProfile);
    }

    public void addVolumeId(int volumeId) {
        volumeIdList.add(volumeId);
    }

    public void addVolumeLabel(String volumeLabel) {
        volumeLabelList.add(volumeLabel);
    }

    /**
     * Выполняет копирование свойств из src в текущий объект. Свойства равные null в источнике игнорируются - в
     * текущем объекте свойство остается неизменным.
     *
     * @param src Устройство, свойства которого необходимо скопировать.
     * @return текущее устройство со свойствами, обновленными из src, если не произошло ошибок, иначе возвращает объект
     * в исходном состоянии.
     */
    public USBDevice copyNonBlankProperties(USBDevice src) {
        try {
            new IgnoreNullBeanUtilsBean().copyProperties(this, src);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("ОШИБКА копирования свойств. Причина: {}", e.getLocalizedMessage());
            LOGGER.debug("{}", e.toString());
        }
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

    @Override
    public int hashCode() {
        return Objects.hash(friendlyName, guid, pid, productName, serial, vendorName, vid, volumeLabelList, revision, isSerialOSGenerated, userAccountsList);
    }

    /**
     * Метод введен временно для ручной настройки выводимых полей. Позже будет удален.
     *
     * @return
     * @deprecated Вводился для наглядного вывода собранной информации.
     */
    @Deprecated(forRemoval = true)
    public String printSomeInfo() {
        return String.format("%-35s | %-30s", serial, guid);
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

    /**
     * "Строитель" объектов типа USBDevice. Особенность строителя - при передаче в качестве аргументов значений null, в
     * поля записываются не нулевые значения (пустые строки, списки нулевой длинны, дата и время минимального значения).
     */
    public static class Builder {
        private final USBDevice newUsbDevice;

        private Builder() {
            newUsbDevice = new USBDevice();
        }

        /**
         * Добавить дополнительный ID (серийный номер) тома диска.
         *
         * @param volumeId ID (серийный номер) тома.
         * @return
         */
        public Builder addVolumeId(int volumeId) {
            newUsbDevice.volumeIdList.add(volumeId);
            return this;
        }

        public Builder addVolumeLabel(String volumeName) {
            newUsbDevice.volumeLabelList.add(Objects.requireNonNullElse(volumeName, ""));
            return this;
        }

        public USBDevice build() {
            return newUsbDevice;
        }

        /**
         * @param fieldName
         * @param value
         * @deprecated Рефлексия в данном случае = выстрел в ногу.
         */
        @Deprecated(forRemoval = true)
        public void setField(String fieldName, Object value) {
            //Инфа: https://javarush.ru/groups/posts/513-reflection-api-refleksija-temnaja-storona-java
            //try-catch навалены друг на друга, надо переделать в более простую логику.
            char[] chars = fieldName.toCharArray();
            chars[0] = Character.toLowerCase(chars[0]);
            fieldName = new String(chars);

            Field field = null;
            try {
                field = USBDevice.Builder.class.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                LOGGER.warn("WARN: {} = {}", e, value);
            }
            if (field != null) {
                field.setAccessible(true);
                try {
                    field.set(this, value);
                } catch (IllegalAccessException illegalAccessException) {
                    illegalAccessException.printStackTrace();
                }
            }
        }

        public Builder withDateTimeFirstInstall(LocalDateTime dateTime) {
            newUsbDevice.dateTimeFirstInstall = Objects.requireNonNullElse(dateTime, LocalDateTime.MIN);
            return this;
        }

        //TODO Скорее всего логику по определению poductName и vendorName разумно вынести во вне, что бы за одно чтение
        // файла можно было получить все необходимые PID/VID. Неплохое место, на первый взгляд - серверная часть.

        public Builder withDiskId(String diskId) {
            newUsbDevice.diskId = Objects.requireNonNullElse(diskId, "");
            return this;
        }

        public Builder withFriendlyName(String friendlyName) {
            newUsbDevice.friendlyName = Objects.requireNonNullElse(friendlyName, "");
            return this;
        }

        public Builder withGuid(String guid) {
            newUsbDevice.guid = Objects.requireNonNullElse(guid, "");
            return this;
        }

        public Builder withRevision(String rev) {
            newUsbDevice.revision = Objects.requireNonNullElse(rev, "");
            return this;
        }

        /**
         * Устанавливает значение serial (серийный номер), а также isSerialOSGenerated в значение false, если второй
         * символ & (признак того что значение сгенерировано ОС и оно уникально только в рамках текущей ОС), true - в
         * противном случае.
         *
         * @param serial серийный номер устройства
         * @return возвращает билдер
         */
        public Builder withSerial(String serial) {
            newUsbDevice.serial = Objects.requireNonNullElse(serial, "");
            //бывает что серийника нет вообще или при чтении данных серийником выступает набор бит, что бы корректно
            // найти и сравнить символ '&' введена проверка.
            if (newUsbDevice.serial.isBlank()) {
                newUsbDevice.isSerialOSGenerated = false;
            } else {
                newUsbDevice.isSerialOSGenerated = serial.charAt(1) == '&';
            }
            return this;
        }

        public Builder withUserProfileList(List<UserProfile> userProfileList) {
            newUsbDevice.userAccountsList = Objects.requireNonNullElse(userProfileList, new ArrayList<>());
            return this;
        }

        /**
         * Устанавливает значения VID/PID. При наличии файла usb.ids автоматически заполняет vendorName и productName
         *
         * @param vid Vendor ID
         * @param pid Product ID
         * @return возвращает билдер.
         */
        public Builder withVidPid(String vid, String pid) {
            //WTF почему если использовать две строки ниже вместо двух IF'ов выше, то null прорывается до vid.matches()?
            newUsbDevice.vid = Objects.requireNonNullElse(vid, "");
            newUsbDevice.pid = Objects.requireNonNullElse(pid, "");
            String regexVidPid = "[0-9a-fA-F]{4}";
            if (!newUsbDevice.vid.matches(regexVidPid) || !newUsbDevice.pid.matches(regexVidPid)) {
                return this;
            }
            newUsbDevice.productName = "";
            newUsbDevice.vendorName = "";

            try (BufferedReader usbIdsReader = new BufferedReader(new FileReader(USBDevice.usbIds))) {
                String currStr = "";
                boolean vendorFound = false;
                //TODO код лажа - переписать на нормальный.
                while (currStr != null) {
                    if (currStr.matches("(?i)^" + newUsbDevice.vid + ".+")) { //текущая строка содержит VendorID? Т.о. отслеживаем начало блока вендора
                        newUsbDevice.vendorName = currStr.split(" {2}")[1];// делитель - два пробела, т.о.:
                        // [0] - vid
                        // [1] - vendor name (имя производителя)
                        vendorFound = true;
                        currStr = usbIdsReader.readLine();
                        continue;
                    }
                    if (vendorFound && currStr.matches("(?i)\\t" + newUsbDevice.pid + ".+")) {//блок вендора начат и строка содержит ProductID?
                        newUsbDevice.productName = currStr.split(" {2}")[1];
                        break;
                    }
                    if (vendorFound && currStr.matches("\\[0-9a-fA-F]{4}\\s+.+")) { //начался блок следующего вендора?
                        newUsbDevice.productName = "";
                        break;
                    }
                    currStr = usbIdsReader.readLine();
                }
            } catch (IOException e) {
                LOGGER.warn("Не удалось определить название производителя и имя продукта для {}/{} по причине: {}",
                        newUsbDevice.vid, newUsbDevice.vid, e.getLocalizedMessage());
                LOGGER.debug("{}", e.toString());
                newUsbDevice.productName = "undef.";
                newUsbDevice.vendorName = "undef.";
            }
            return this;
        }
    }
}
