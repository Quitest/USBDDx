package ru.pel.usbddc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Класс для чтения данных из реестра. Состоит исключительно из static методов чтения данных из реестра.
 * <p>
 * Задумка взята с <a href=https://stackoverflow.com/questions/62289/read-write-to-windows-registry-using-java>https://stackoverflow.com...</a>
 * </p>
 */
public class WinRegReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(WinRegReader.class);

    private WinRegReader() {
        //все методы статические - нет необходимости создавать объект.
    }

    /**
     * Возвращает список подразделов (подключей) указанного раздела (ключа) реестра
     *
     * @param key раздел (ключ) реестра, подразделы (подключи) которого необходимо получить
     * @return список подразделов (подключей). Если подразделов нет, то возращается список с размером 0.
     */
    public static List<String> getSubkeys(String key) throws IOException, InterruptedException {
        //Вывод имеет следующий формат
        //<key>\subkey1 - первая строка, в моем случае вседга пустая.
        //<key>\subkey2
        //<key>\subkeyN
        String output = WinComExecutor.exec("reg query \"" + key + "\"").getResult();
//            String output = execCommand("cmd /c start /wait /I reg.lnk query \"" + key + "\"").getResult();

        return Arrays.stream(output.split(System.lineSeparator()))
                .filter(s -> !s.isEmpty() &&            //отбрасываем пустые строки
                        s.matches("HKEY.+") &&    //строки с параметрами
                        !s.matches(Pattern.quote(key)))  //отбстроку с именем раздела, в котором ищем подразделы
                .collect(Collectors.toList());
    }

    /**
     * Служит для чтения всех параметров из указанного ключа реестра.
     *
     * @param key ключ (подраздел) реестра параметры которого необходимо получить
     * @return Возвращает Optional пару параметр=значение. Если параметров нет или что-то пошло не так, то возыращается
     * Optional.empty()
     */
    public static Optional<Map<String, String>> getAllValuesInKey(String key) {
        try {
            String output = WinComExecutor.exec("reg query \"" + key + "\"").getResult();

//            String cyr = new String(output.getBytes("cp866"), "windows-1251");

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
            LOGGER.error("ОШИБКА. Не удалось получить список параметров реестра в разделе {}. Причина: {}", key, e.getLocalizedMessage());
            LOGGER.debug("{}", e.toString());
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
            String output = WinComExecutor.exec("reg query " + '"' + key + "\" /v \"" + value + "\"").getResult();

            // Вывод имеет следующий формат:
            // \n<Version information>\n\n<value>\t<registry type>\t<value>
            //не пойму его назначения оригинального условия if(!output.contains("\t")){
            //  В моем случае всегда было true, т.к. output всегда не содержал \t, вместо табуляции в строке было четыре
            //  символа пробела.
            if (output.matches("\\s+")) {
                return Optional.empty();
            }

            String[] parsed = output.split("\\s{4}"); //в оригинале регулярка была "\t", что давало неверный результат,
            // т.к. в output деление идет четырьмя символами пробела
            return Optional.of(parsed[parsed.length - 1]);
        } catch (IOException | InterruptedException e) {
            LOGGER.error("ОШИБКА. Не удалось получить значение параметра {} в разделе {}. Причина: {}", value, key, e.getLocalizedMessage());
            LOGGER.debug("{}", e.toString());
            return Optional.empty();
        }

    }

    /**
     * <p>Загружает в куст реестра для дальнейшей работы с ним.</p>
     * <u>Внимание!</u> Требует наличие прав администратора!
     *
     * @param nodeName Имя подраздела реестра, в который загружается файл куста. Создание нового раздела.
     * @param hive     Имя файла куста, подлежащего загрузке.
     * @return {@code WinRegReader.ExecResult}, в котором первое значение код выхода (0 - успешно, 1 - провал), второе - пустая строка.
     */
    public static WinComExecutor.Result<Integer, String> loadHive(String nodeName, String hive) throws IOException, InterruptedException {
        return WinComExecutor.exec("reg load " + nodeName + " \"" + hive + "\"");
    }

    /**
     * <p>Выгружает ранее загруженный куст реестра.</p>
     * <u>Внимание!</u> Требует наличие прав администратора!
     *
     * @param nodeName выгружаемый куст реестра
     * @return {@code WinRegReader.ExecResult}, в котором первое значение код выхода (0 - успешно, 1 - провал), второе - пустая строка.
     */
    public static WinComExecutor.Result<Integer, String> unloadHive(String nodeName) throws IOException, InterruptedException {
        //TODO делать выгрузку после проверки существования раздела - нужен отдельный метод проверки.
        return WinComExecutor.exec("reg unload " + nodeName);
    }

    /**
     * Определяет существование раздела реестра путем получения значения по умолчанию. Если значение имеется, то считается, что
     * раздел существует.
     *
     * @param key раздел, существование которого необходимо проверить.
     * @return true - раздел реестра существует, false - указанного раздела нет.
     * @throws IOException
     * @throws InterruptedException
     */
    public static boolean isKeyExists(String key) throws IOException, InterruptedException {
        WinComExecutor.Result<Integer, String> result = WinComExecutor.exec("reg query \"" + key + "\" /ve ");
        boolean b = result.getExitCode() == 0;
        boolean s = !result.getResult().isEmpty();
        return b && s;
    }
    /*
     *//**
     * Выполняет указанную команду в отдельном процессе, ждет окончания ее работы и возвращает результат.
     *
     * @param command команда для выполнения
     * @return кортеж (пару значений): первое - код, с которым завершилась команда; второе - сам результат выполнения в
     * виде строки.
     * @throws IOException
     * @throws InterruptedException
     *//*
    private static ExecResult<Integer, String> execCommand(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);

        StreamReader reader = new StreamReader(process.getInputStream());
        reader.start();
        int exitCode = process.waitFor();
        reader.join();
        String result = reader.getResult();
        return new ExecResult<>(exitCode, result);
    }

    private static class StreamReader extends Thread {
        private static Logger logger = LoggerFactory.getLogger(StreamReader.class);
        private final StringWriter sw = new StringWriter();
        private InputStreamReader isr;

        public StreamReader(InputStream is) {
            try {
                this.isr = new InputStreamReader(is, "866");
            } catch (UnsupportedEncodingException e) {
                logger.error("{}", e.getLocalizedMessage());
                logger.debug("{}", e.toString());
            }
        }

        public String getResult() {
            return sw.toString();
        }

        @Override
        public void run() {
            try {
                int c;
                while ((c = isr.read()) != -1) {
                    sw.write(c);
                }
            } catch (IOException e) {
                logger.error("{}", e.getLocalizedMessage());
                logger.debug("{}", e.toString());
            }

        }
    }

    @Getter
    @Setter
    public static class ExecResult<C, R> {
        private C exitCode;
        private R result;

        public ExecResult(C exitCode, R result) {
            this.exitCode = exitCode;
            this.result = result;
        }

        public ExecResult() {
        }
    }*/
}