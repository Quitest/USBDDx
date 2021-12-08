package ru.pel.usbddc.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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
@EqualsAndHashCode
public class USBDevice /*extends Device*/ {
    private static final Logger logger = LoggerFactory.getLogger(USBDevice.class.getName());
    @Getter
    @Setter
    private static String usbIds;
    //    private String NOT_DEFINE = "not defined";
    private String friendlyName;
    private String pid;
    private String productName;
    private String serial;
    private String vendorName;
    private String vid;
    private String volumeName;
    private String revision;
    private boolean isSerialOSGenerated;
//    private final String service; //TODO узнать назначение одноименного параметра в реестре винды

//    private USBDevice(Builder builder) {
//        super();
//        this.friendlyName = builder.friendlyName;
//        this.vid = builder.vid;
//        this.pid = builder.pid;
//        this.vendorName = builder.vendorName;
//        this.productName = builder.productName;
//        this.serial = builder.serial;
//        this.isSerialOSGenerated = builder.isSerialOSGenerated;
//    }

    private USBDevice() {
    }

    public static Builder getBuilder() {
        return new Builder();
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
            Field[] fieldsSuper = Device.class.getDeclaredFields();
            List<Field> fields = new ArrayList<>();
            fields.addAll(Arrays.asList(fieldsThis));
            fields.addAll(Arrays.asList(fieldsSuper));
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
        //        private String compatibleIDs;
//        private String deviceDesc;
//        private String hardwareID;
//        private String vendorName;
//        private String productName;
//        private String friendlyName;
//        private String vid;
//        private String pid;
//        private String serial;
//        private boolean isSerialOSGenerated;
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

        public Builder withSerial(String serial) {
            newUsbDevice.serial = serial;
            newUsbDevice.isSerialOSGenerated = serial.charAt(1) == '&';
            return this;
        }

        //TODO Скорее всего логику по определению poductName и vendorName разумно вынести во вне, что бы за одно чтение
        // файла можно было получить все необходимые PID/VID. Неплохое место, на первый взгляд - серверная часть.
        public Builder withVidPid(String vid, String pid) {
            newUsbDevice.vid = vid;
            newUsbDevice.pid = pid;
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
                    if (vendorFound && currStr.matches("^\\w{4}.+?")) { //начался блок следующего вендора?
                        newUsbDevice.productName = "";
                        break;
                    }
                    currStr = usbIdsReader.readLine();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return this;
        }

        public Builder withVolumeName(String volumeName) {
            newUsbDevice.volumeName = volumeName;
            return this;
        }
    }
}
