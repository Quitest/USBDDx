package ru.pel.usbddc.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.*;

//TODO реализовать сбор информации из setupapi.dev.log

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
public class USBDevice extends Device {
    @Getter
    @Setter
    private static String usbIds; //TODO Организовать хранение пути к файлу в конфиг файле, например conf.xml
    //    @Getter
//    @Setter
//    private String containerID;
    @Getter
    @Setter
    private String friendlyName;
    //    @Getter
//    @Setter
//    private String hardwareID;
//    @Getter
//    @Setter
//    private String serial;
    @Getter
    private String vid;
    //    @Getter
//    private String vendorName;
    @Getter
    private String pid;
    @Setter
    @Getter
    private String parentIdPrefix;
    @Setter
    @Getter
    private String address;
    @Setter
    @Getter
    private String locationInformation;
    @Setter
    @Getter
    private String lowerFilters;

    //TODO выполнить классификацию USB устройства. См. подробнее на http://www.linux-usb.org/usb-ids.html Все данные
    //  также есть и в usb.ids. Думаю, это поле стоит реализовать как тип USBDeviceClasses
    //TODO запрос данных о классе, попробовать реализовать через интернет, прямо с сайта.

    //TODO сделать поле хранения ДатыВремени первого и последнего использования USB устройства. Поле должно хранить
    //  ДатуВремя из разных источников для возможности поиска попыток чистки системы от следов.
    //    @Getter
//    private String productName;
    @Getter
    @Setter
    private String service; //TODO узнать назначение одноименного параметра в реестре винды

    // TODO запрос данных о классе, попробовать реализовать через интернет, прямо с сайта.
    // TODO запрос данных о классе устройства, попробовать реализовать через интернет, прямо с сайта.

    /**
     * Метод определяет имя устройства (продукта) по его PID. Данные берутся из файла
     * <a href = http://www.linux-usb.org/usb.ids>usb.ids</a>.
     *
     * @return true - если имя устройства (продукта) определено и установлено. false - в иных случаях.
     */
    //FIXME очень долго отрабатывает
    public boolean determineProductName() {
        if (vid.equals("<not defined>")) {
            return false;
        }
        //парсинг файла usb.ids на наличие человеческого имени устройства и PID
        //код лажа, но работает. Желательно переделать его на более читаемый вариант.
        boolean nameFound = false;
        try (Scanner scanner = new Scanner(Paths.get(usbIds))) {
            boolean vendorFound = false;
            //в цикле читаются строки из файла usb.ids для поиска Product name. Очевидно. :)
            while (scanner.hasNextLine()) {
                String currStr = scanner.nextLine();
                if (currStr.matches("^" + vid + ".+")) { //текущая строка содержит VendorID? Т.о. отслеживаем начало блока вендора
                    vendorFound = true;
                    continue;
                }
                if (vendorFound && currStr.matches("\\t" + pid + ".+")) {//блок вендора начат и строка содержит ProductID?
                    super.productName = currStr.split(" {2}")[1];
                    nameFound = true;
                    break;
                }
                if (vendorFound && currStr.matches("^\\w{4}.+?")) { //начался блок следующего вендора?
                    super.productName = "<not defined>";
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return nameFound;
    }

        /**
     * Метод определяет имя производителя по его VID. Данные берутся из файла
     * <a href = http://www.linux-usb.org/usb.ids>usb.ids</a>. Файл должен находиться рядом с jar
     *
     * @return true - если имя производителя определено и установлено. false - в иных случаях.
     */
        //FIXME очень долго отрабатывает
    public boolean determineVendorName() {
        boolean found = false;
        super.vendorName = "";
        try (BufferedReader usbIdsReader = new BufferedReader(new FileReader(usbIds))) {
            super.vendorName = usbIdsReader.lines()
                    .filter(l -> l.matches(vid + ".+"))//фильтруем строки, начинающиеся с VendorID
                    .map(s -> s.split(" {2}")[1])   // делитель - два пробела, т.о.:
                    // [0] - vid
                    // [1] - vendor name (имя производителя)
                    .findFirst().orElse("<not defined>");

            found = super.vendorName.trim()
                    .matches("\\S+"); // если в имени есть хотябы один любой символ кроме [ \t\n\x0B\f\r],
            // то поиск считается усешным.
        } catch (IOException e) {
            e.printStackTrace();
        }
        return found;
    }

//TODO в случа удачной реализации перенести в ru.pel.usbddc.entity.Device class
    public void setField(String fieldName, Object value) {
        //Инфа: https://javarush.ru/groups/posts/513-reflection-api-refleksija-temnaja-storona-java
        //try-catch навалены дург на друга, надо передалать в более простую логику.
        char[] chars = fieldName.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        fieldName = new String(chars);

        Field field = null;
        try {
//            field = this.getClass().getDeclaredField(fieldName);
            field = USBDevice.class.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            try {
//                field = super.getClass().getDeclaredField(fieldName);
                field = Device.class.getDeclaredField(fieldName);
            } catch (NoSuchFieldException noSuchFieldException) {
                //noSuchFieldException.printStackTrace();
            }
        }
        if (field != null){
            field.setAccessible(true);
            try {
                field.set(this, value);
            } catch (IllegalAccessException illegalAccessException) {
                illegalAccessException.printStackTrace();
            }
        }
    }

    /**
     * Метод единовременно устанавливает значения Vendor ID и Product ID. Для  корректного определения Product ID
     * необходимо наличие Vendor ID, т.к. у разных Vendor ID могут быть одинаковые Product ID.
     *
     * @param vid Vendor ID
     * @param pid Product ID
     */
    public void setVidPid(String vid, String pid) {
        this.vid = vid;
        this.pid = pid;
    }



    @Override
    public String toString() {
        //получение всех полей экземпляра и вывод их реализован при помощи методов reflection.
        //Почему? Да просто захотелось попробовать эту рефлексию. Плюс количество полей класса может меняться, и что бы
        //не лазить в метод лишний раз, решено автоматизировать немного.
        //WTF Как делать лучше или допустимо: один раз получить символ новой строки, сохранить его в String и потом
        // использовать в цикле или же в цикле напрямую использовать System.lineSeparator() ?
        final String NEW_LINE = System.lineSeparator();
        StringBuilder sb = new StringBuilder("");
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
}
