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
import java.nio.file.Paths;
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
//TODO общие с USBSTOR, USBPRINT поля вынести в ru.pel.usbddc.entity.Device
//@Setter
@Getter
@EqualsAndHashCode
public class USBDevice extends Device {
    private static Logger logger = LoggerFactory.getLogger(USBDevice.class.getName());
    @Getter
    @Setter
    private static String usbIds;
    private final String NOT_DEFINE = "not defined";
    private final String friendlyName;
    private final String vid;
    private final String pid;
    private final String parentIdPrefix;
    private final String address;
    private final String locationInformation;
    private final String lowerFilters;
//    private final String service; //TODO узнать назначение одноименного параметра в реестре винды

    private USBDevice(Builder builder) {
        this.friendlyName = builder.friendlyName;
        this.vid = builder.vid;
        this.pid = builder.pid;
        this.parentIdPrefix = builder.parentIdPrefix;
        this.address = builder.address;
        this.locationInformation = builder.locationInformation;
        this.lowerFilters = builder.lowerFilters;
        this.vendorName = builder.vendorName;
        this.productName = builder.productName;
    }

    /**
     * Метод определяет имя устройства (продукта) по его PID. Данные берутся из файла
     * <a href = http://www.linux-usb.org/usb.ids>usb.ids</a>.
     *
     * @return true - если имя устройства (продукта) определено и установлено. false - в иных случаях.
     */
    //FIXME очень долго отрабатывает
//    public boolean determineProductName() {
//        if (vid.equals(NOT_DEFINE)) {
//            return false;
//        }
//        //парсинг файла usb.ids на наличие человеческого имени устройства и PID
//        //код лажа, но работает. Желательно переделать его на более читаемый вариант.
//        boolean nameFound = false;
//        try (Scanner scanner = new Scanner(Paths.get(usbIds))) {
//            boolean vendorFound = false;
//            //в цикле читаются строки из файла usb.ids для поиска Product name. Очевидно. :)
//            while (scanner.hasNextLine()) {
//                String currStr = scanner.nextLine();
//                if (currStr.matches("^" + vid + ".+")) { //текущая строка содержит VendorID? Т.о. отслеживаем начало блока вендора
//                    vendorFound = true;
//                    continue;
//                }
//                if (vendorFound && currStr.matches("\\t" + pid + ".+")) {//блок вендора начат и строка содержит ProductID?
//                    super.productName = currStr.split(" {2}")[1];
//                    nameFound = true;
//                    break;
//                }
//                if (vendorFound && currStr.matches("^\\w{4}.+?")) { //начался блок следующего вендора?
//                    super.productName = NOT_DEFINE;
//                    break;
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return nameFound;
//    }

    /**
     * Метод определяет имя производителя по его VID. Данные берутся из файла
     * <a href = http://www.linux-usb.org/usb.ids>usb.ids</a>. Файл должен находиться рядом с jar
     *
     * @return true - если имя производителя определено и установлено. false - в иных случаях.
     */
    //FIXME очень долго отрабатывает
//    public boolean determineVendorName() {
//        boolean found = false;
//        super.vendorName = "";
//        try (BufferedReader usbIdsReader = new BufferedReader(new FileReader(usbIds))) {
//            super.vendorName = usbIdsReader.lines()
//                    .filter(l -> l.matches(vid + ".+"))//фильтруем строки, начинающиеся с VendorID
//                    .map(s -> s.split(" {2}")[1])   // делитель - два пробела, т.о.:
//                    // [0] - vid
//                    // [1] - vendor name (имя производителя)
//                    .findFirst().orElse(NOT_DEFINE);
//
//            found = super.vendorName.trim()
//                    .matches("\\S+"); // если в имени есть хотя бы один любой символ кроме [ \t\n\x0B\f\r],
//            // то поиск считается успешным.
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return found;
//    }

    //TODO в случа удачной реализации перенести в ru.pel.usbddc.entity.Device class
//    public void setField(String fieldName, Object value) {
//        //Инфа: https://javarush.ru/groups/posts/513-reflection-api-refleksija-temnaja-storona-java
//        //try-catch навалены дург на друга, надо передалать в более простую логику.
//        char[] chars = fieldName.toCharArray();
//        chars[0] = Character.toLowerCase(chars[0]);
//        fieldName = new String(chars);
//
//        Field field = null;
//        try {
////            field = this.getClass().getDeclaredField(fieldName);
//            field = USBDevice.class.getDeclaredField(fieldName);
//        } catch (NoSuchFieldException e) {
//            try {
////                field = super.getClass().getDeclaredField(fieldName);
//                field = Device.class.getDeclaredField(fieldName);
//            } catch (NoSuchFieldException noSuchFieldException) {
//                //noSuchFieldException.printStackTrace();
////                System.err.println("Параметр " + fieldName + " не представлен в USBDevice.class и Device.class");
//                logger.warn("WARN: ", noSuchFieldException);
//            }
//        }
//        if (field != null) {
//            field.setAccessible(true);
//            try {
//                field.set(this, value);
//            } catch (IllegalAccessException illegalAccessException) {
//                illegalAccessException.printStackTrace();
//            }
//        }
//    }


//    public void setVidPid(String vid, String pid) {
//        this.vid = vid;
//        this.pid = pid;
//    }


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
        public String vendorName;
        public String productName;
        //        private static String usbIds = USBDevice.usbIds;
        private String friendlyName;
        private String vid;
        private String pid;
        private String parentIdPrefix;
        private String address;
        private String locationInformation;
        private String lowerFilters;
        private String serial;
        //    private  String service; //TODO узнать назначение одноименного параметра в реестре винды

        public static Builder builder() {
            return new Builder();
        }

        public Builder withFriendlyName(String friendlyName) {
            this.friendlyName = friendlyName;
            return this;
        }

        //FIXME избавиться от двойного прохода файла
        public Builder withVidPid(String vid, String pid) {
            this.vid = vid;
            this.pid = pid;
            try (BufferedReader usbIdsReader = new BufferedReader(new FileReader(USBDevice.usbIds))) {
                vendorName = usbIdsReader.lines()
                        .filter(l -> l.matches(vid + ".+"))//фильтруем строки, начинающиеся с VendorID
                        .map(s -> s.split(" {2}")[1])   // делитель - два пробела, т.о.:
                        // [0] - vid
                        // [1] - vendor name (имя производителя)
                        .findFirst().orElse("");

//                found = super.vendorName.trim()
//                        .matches("\\S+"); // если в имени есть хотя бы один любой символ кроме [ \t\n\x0B\f\r],
                // то поиск считается успешным.
            } catch (IOException e) {
                e.printStackTrace();
            }


            if (this.vid.isEmpty()) {
                productName="";
                return this;
            }
            //парсинг файла usb.ids на наличие человеческого имени устройства и PID
            //код лажа, но работает. Желательно переделать его на более читаемый вариант.
//            boolean nameFound = false;
            try (Scanner scanner = new Scanner(Paths.get(USBDevice.usbIds))) {
                boolean vendorFound = false;
                //в цикле читаются строки из файла usb.ids для поиска Product name. Очевидно. :)
                while (scanner.hasNextLine()) {
                    String currStr = scanner.nextLine();
                    if (currStr.matches("^" + vid + ".+")) { //текущая строка содержит VendorID? Т.о. отслеживаем начало блока вендора
                        vendorFound = true;
                        continue;
                    }
                    if (vendorFound && currStr.matches("\\t" + pid + ".+")) {//блок вендора начат и строка содержит ProductID?
                        productName = currStr.split(" {2}")[1];
//                        nameFound = true;
                        break;
                    }
                    if (vendorFound && currStr.matches("^\\w{4}.+?")) { //начался блок следующего вендора?
                        productName = "";
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return this;
        }

//        public Builder withPid(String pid) {
//            this.pid = pid;
//            if (vid.isEmpty()) {
//                productName="";
//                return this;
//            }
//            //парсинг файла usb.ids на наличие человеческого имени устройства и PID
//            //код лажа, но работает. Желательно переделать его на более читаемый вариант.
////            boolean nameFound = false;
//            try (Scanner scanner = new Scanner(Paths.get(USBDevice.usbIds))) {
//                boolean vendorFound = false;
//                //в цикле читаются строки из файла usb.ids для поиска Product name. Очевидно. :)
//                while (scanner.hasNextLine()) {
//                    String currStr = scanner.nextLine();
//                    if (currStr.matches("^" + vid + ".+")) { //текущая строка содержит VendorID? Т.о. отслеживаем начало блока вендора
//                        vendorFound = true;
//                        continue;
//                    }
//                    if (vendorFound && currStr.matches("\\t" + pid + ".+")) {//блок вендора начат и строка содержит ProductID?
//                        productName = currStr.split(" {2}")[1];
////                        nameFound = true;
//                        break;
//                    }
//                    if (vendorFound && currStr.matches("^\\w{4}.+?")) { //начался блок следующего вендора?
//                        productName = "";
//                        break;
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return this;
//        }

        public Builder withParentIdPrefix(String parentIdPrefix) {
            this.parentIdPrefix = parentIdPrefix;
            return this;
        }

        public Builder withAddress(String address) {
            this.address = address;
            return this;
        }

        public Builder withLocationInformation(String locationInformation) {
            this.locationInformation = locationInformation;
            return this;
        }

        public Builder withLowerFilters(String lowerFilters) {
            this.lowerFilters = lowerFilters;
            return this;
        }

        public Builder withSerial(String serial){
            this.serial = serial;
            return this;
        }

        public USBDevice build() {
            return new USBDevice(this);
        }

        public void setField(String fieldName, Object value) {
            //Инфа: https://javarush.ru/groups/posts/513-reflection-api-refleksija-temnaja-storona-java
            //try-catch навалены дург на друга, надо передалать в более простую логику.
            char[] chars = fieldName.toCharArray();
            chars[0] = Character.toLowerCase(chars[0]);
            fieldName = new String(chars);

            Field field = null;
            try {
//            field = this.getClass().getDeclaredField(fieldName);
                field = USBDevice.Builder.class.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
//                try {
////                field = super.getClass().getDeclaredField(fieldName);
//                    field = Device.class.getDeclaredField(fieldName);
//                } catch (NoSuchFieldException noSuchFieldException) {
//                    //noSuchFieldException.printStackTrace();
////                System.err.println("Параметр " + fieldName + " не представлен в USBDevice.class и Device.class");
//
//                }
                logger.warn("WARN: ", e);
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
    }
}
