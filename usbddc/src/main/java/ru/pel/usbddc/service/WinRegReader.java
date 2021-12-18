package ru.pel.usbddc.service;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Класс для чтения данных из реестра. Состоит исключительно из static методов чтения данных из реестра.
 * <p>
 * Задумка взята с <a href=https://stackoverflow.com/questions/62289/read-write-to-windows-registry-using-java>https://stackoverflow.com...</a>
 * </p>
 */
public class WinRegReader {
    /**
     * Возвращает список подразделов (подключей) указанного раздела (ключа) реестра
     *
     * @param key раздел (ключ) реестра, подразделы (подключи) которого необходимо получить
     * @return список подразделов (подключей). Если подразделов нет, то возращается список с размером 0.
     */
    public static List<String> getSubkeys(String key) {
        List<String> result = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("reg query \"" + key + "\"");
            StreamReader reader = new StreamReader(process.getInputStream());
            reader.start();
            process.waitFor();
            reader.join();

            //Вывод имеет следующий формат
            //<key>\subkey1 - первая строка, в моем случае вседга пустая.
            //<key>\subkey2
            //<key>\subkeyN
            String output = reader.getResult();

            result = Arrays.stream(output.split(System.lineSeparator()))
                    .filter(s -> !s.isEmpty() &&            //отбрасываем пустые строки
                            s.matches("HKEY.+") &&    //строки с параметрами
                            !s.matches(Pattern.quote(key)))  //отбстроку с именем раздела, в котором ищем подразделы
                    .collect(Collectors.toList());
            return result;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return result;
        }
    }

    /**
     * Служит для чтения всех параметров из указанного ключа реестра.
     *
     * @param key ключ (подраздел) реестра параметры которого необходимо получить
     * @return возвращает Optional пару параметр=значение. Если параметров нет или что-то пошло не так, то возыращается
     * Optional.empty()
     */
    public static Optional<Map<String, String>> getAllValuesInKey(String key) {
        try {
            Process process = Runtime.getRuntime().exec("reg query \"" + key + "\"");
            StreamReader reader = new StreamReader(process.getInputStream());

            reader.start();
            process.waitFor();
            reader.join();
            String output = reader.getResult();
            String cyr = new String(output.getBytes("cp866"), "windows-1251");

//            String cp = new String("фбвс".getBytes(), "866");
//            String utf = new String(cp.getBytes("866"), StandardCharsets.UTF_8);

            if (output.matches("\\s+")) {
                return Optional.empty();
            }

            String[] outputLines = output.split(System.lineSeparator());
            //FIXME в мапу падают абракадабры вместо нормальных русских слов, если они есть.
            Map<String, String> values = Arrays.stream(outputLines)
                    .filter(l -> !l.isEmpty() && l.matches("\\s{4}.+")) //отбрасываем пустые строки и строки с именами ключей
                    .map(l -> l.split("\\s{4}")) //оставшиеся строки делим на элементы по признаку "4 пробела"
//                    .collect(Collectors.toMap(l->l[1],l->l[l.length-1])); //и собираем в мапу, где l[1] - имя параметра, l[l.length-1] - значение
                    .collect(Collectors.toMap(l -> l[1],
                            l -> l.length >= 3 ? l[l.length - 1] : ""));
            return Optional.of(values);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return Optional.empty();
        }

    }

    /**
     * Получает значение указанного параметра реестра.
     *
     * @param key   путь к ключу в реестре
     * @param value параметр реестра
     * @return {@code Optional<String>}, содержащий значение параметра
     */
    public static Optional<String> getValue(String key, String value) {
        try {
            Process process = Runtime.getRuntime().exec("reg query " + '"' + key + "\" /v \"" + value + "\"");

            StreamReader reader = new StreamReader(process.getInputStream());
            reader.start();
            process.waitFor();
            reader.join();
            String output = reader.getResult();

            // Вывод имеет следующий формат:
            // \n<Version information>\n\n<value>\t<registry type>\t<value>
            //if( ! output.contains("\t")){ - это оригинальное условие, не пойму его назначения.
            //  В моем случае всегда было true, т.к. output всегда не содержал \t, вместо табуляции в строке было четыре
            //  символа пробела.
            if (output.matches("\\s+")) {
                return Optional.empty();
            }

            String[] parsed = output.split("\\s{4}"); //в оригинале регулярка была "\t", что давало неверный результат,
            // т.к. в output деление идет четырьмя символами пробела
            return Optional.of(parsed[parsed.length - 1]);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }

    }

    /**
     * Метод-заглушка-напоминание, возможно, в будущем он будет реализован, если потребуется.
     *
     * @param key   путь по которому расположен параметр.
     * @param value имя параметра, тип которого необходимо получить.
     * @return тип запрашиваемого параметра.
     */
    public static String getValueType(String key, String value) {
        return "";
    }

    //TODO не пойму зачем вынесено в отдельный класс. Может избавиться от него?
    private static class StreamReader extends Thread {
        //        ORIGINAL CODE
//        private InputStream is;
        private InputStreamReader isr;
        private StringWriter sw = new StringWriter();

        public StreamReader(InputStream is) {
//            ORIGINAL CODE
//            this.is = is;
            try {
                this.isr = new InputStreamReader(is, "866");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        public String getResult() {
            return sw.toString();
        }

        @Override
        public void run() {
//            ORIGINAL CODE
//            try {
//                int c;
//                while ((c = is.read()) != -1){
//                    sw.write(c);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            try {
                int c;
                while ((c = isr.read()) != -1) {
                    sw.write(c);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}