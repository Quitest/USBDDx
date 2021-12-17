package ru.pel.usbddc.entity;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pel.usbddc.utility.IgnoreNullBeanUtilsBean;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Класс предназначен для описания USB устройства.
 * Хранит в себе сведения об устройстве, полученные из:
 * <ul>
 *     <li>реестра</li>
 *     <li>setupapi.dev.log (прим.: в текущей версии сведения не берутся из него)</li>
 * </ul>
 */
/*
 * Учитывая что для конструирования объекта ru.pel.usbddc.entity.USBDevice требуется много параметров, как вариант дальнейшей "пробы пера" и
 * прокачки навыков, можно попробовать реализовать создание ru.pel.usbddc.entity.USBDevice, используя паттерн Строитель/Builder.
 * Однако, плюсов применения его в данной версии ПО не вижу пока что - уменьшим количество параметров, увеличим количество
 * классов и интерфесов ради одного типа? Сомнительно... Или не понимаю еще каких-то плюсов применения паттерна.
 * */

@Getter
@Setter
//@EqualsAndHashCode
public class USBDevice {
    private static final Logger logger = LoggerFactory.getLogger(USBDevice.class.getName());
    @Getter
    @Setter
    private static String usbIds;

    static {
        usbIds = "usb.ids";
    }

    private String friendlyName;
    private String guid;
    private String pid;
    private String productName;
    private String serial;
    private String vendorName;
    private String vid;
    private String volumeName;
    private String revision;
    private boolean isSerialOSGenerated;
//    private List<String> userAccountsList;
//    private final String service; //TODO узнать назначение одноименного параметра в реестре винды
    private List<UserProfile> userAccountsList;

    private USBDevice() {
        friendlyName = "";
        pid = "";
        productName = "";
        serial = "";
        vendorName = "";
        vid = "";
        volumeName = "";
        revision = "";
        isSerialOSGenerated = true;
        userAccountsList = new ArrayList<>();
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        USBDevice usbDevice = (USBDevice) o;
        return isSerialOSGenerated == usbDevice.isSerialOSGenerated && Objects.equals(friendlyName, usbDevice.friendlyName) && Objects.equals(guid, usbDevice.guid) && Objects.equals(pid, usbDevice.pid) && Objects.equals(productName, usbDevice.productName) && Objects.equals(serial, usbDevice.serial) && Objects.equals(vendorName, usbDevice.vendorName) && Objects.equals(vid, usbDevice.vid) && Objects.equals(volumeName, usbDevice.volumeName) && Objects.equals(revision, usbDevice.revision) && Objects.equals(userAccountsList, usbDevice.userAccountsList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(friendlyName, guid, pid, productName, serial, vendorName, vid, volumeName, revision, isSerialOSGenerated, userAccountsList);
    }

    /**
     * Выполняет копирование свойств из src в текущий объект. Свойства равные null в источнике игнорируются - в
     * текущем объекте свойство остается неизменным.
     *
     * @param src Устройство, свойства которого необходимо скопировать.
     */
    public void copyNonNullProperties(USBDevice src) throws InvocationTargetException, IllegalAccessException {
        new IgnoreNullBeanUtilsBean().copyProperties(this,src);
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
        //получение всех полей экземпляра и вывод их реализован при помощи методов reflection.
        //Почему? Да просто захотелось попробовать эту рефлексию. Плюс количество полей класса может меняться, и что бы
        //не лазить в метод лишний раз, решено автоматизировать немного.
        //WTF Как делать лучше или допустимо: один раз получить символ новой строки, сохранить его в String и потом
        // использовать в цикле или же в цикле напрямую использовать System.lineSeparator() ?
        final String NEW_LINE = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        try {
            Field[] fieldsThis = USBDevice.class.getDeclaredFields();
            List<Field> fields = new ArrayList<>();
            fields.addAll(Arrays.asList(fieldsThis));
            fields.sort(Comparator.comparing(Field::getName));

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

    public static class Builder {
        private USBDevice newUsbDevice;

        private Builder() {
            newUsbDevice = new USBDevice();
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
//                logger.warn("WARN: {} = {}", e, value);
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

        public Builder withFriendlyName(String friendlyName) {
            newUsbDevice.friendlyName = friendlyName;
            return this;
        }

        public Builder withGuid(String guid) {
            newUsbDevice.guid = guid;
            return this;
        }

        //TODO Скорее всего логику по определению poductName и vendorName разумно вынести во вне, что бы за одно чтение
        // файла можно было получить все необходимые PID/VID. Неплохое место, на первый взгляд - серверная часть.

        public Builder withRevision(String rev) {
            newUsbDevice.revision = rev;
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
            newUsbDevice.serial = serial;
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
            newUsbDevice.userAccountsList = new ArrayList<>(userProfileList);
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
            newUsbDevice.vid = vid;
            newUsbDevice.pid = pid;
            newUsbDevice.productName = "";
            newUsbDevice.vendorName = "";
            String regexVidPid = "[0-9a-fA-F]{4}";
            if (!vid.matches(regexVidPid) || !pid.matches(regexVidPid)) {
                return this;
            }

            try (BufferedReader usbIdsReader = new BufferedReader(new FileReader(USBDevice.usbIds))) {
                String currStr = "";
                boolean vendorFound = false;
                //TODO код лажа - переписать на нормальный.
                while (currStr != null) {
                    if (currStr.matches("^" + vid + ".+")) { //текущая строка содержит VendorID? Т.о. отслеживаем начало блока вендора
                        newUsbDevice.vendorName = currStr.split(" {2}")[1];// делитель - два пробела, т.о.:
                        // [0] - vid
                        // [1] - vendor name (имя производителя)
                        vendorFound = true;
                        currStr = usbIdsReader.readLine();
                        continue;
                    }
                    if (vendorFound && currStr.matches("\\t" + pid + ".+")) {//блок вендора начат и строка содержит ProductID?
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
                //TODO сделать запись в лог
                e.printStackTrace();
                newUsbDevice.productName = "";
                newUsbDevice.vendorName = "";
            }

            return this;
        }

        public Builder withVolumeName(String volumeName) {
            newUsbDevice.volumeName = volumeName;
            return this;
        }
    }
}
