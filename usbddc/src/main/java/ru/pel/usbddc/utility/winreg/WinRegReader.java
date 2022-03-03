package ru.pel.usbddc.utility.winreg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pel.usbddc.utility.WinComExecutor;
import ru.pel.usbddc.utility.winreg.exception.RegistryAccessDeniedException;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Класс для чтения данных из реестра.
 * <p>
 * Задумка взята с <a href=https://stackoverflow.com/questions/62289/read-write-to-windows-registry-using-java>https://stackoverflow.com...</a>
 * </p>
 */
public class WinRegReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(WinRegReader.class);

    private final WinComExecutor winComExecutor = new WinComExecutor();

    public WinRegReader() {
        //Что бы случайно не потерять конструктор по умолчанию.
    }

    /**
     * Служит для чтения всех параметров из указанного ключа реестра.
     *
     * @param key ключ (подраздел) реестра параметры которого необходимо получить
     * @return Возвращает Optional пару параметр=значение. Если параметров нет или что-то пошло не так, то возыращается
     * Optional.empty()
     */
    public Optional<Map<String, String>> getAllValuesInKey(String key) {
        Optional<Map<String, String>> valuesOptional = Optional.empty();
        try {
            String command = String.format("reg query \"%s\"", key);
            String output = winComExecutor.exec(command).getBody();

            if (output.matches("\\s+")) {
                return Optional.empty();
            }

            String[] outputLines = output.split(System.lineSeparator());
            Map<String, String> values = Arrays.stream(outputLines)
                    .filter(l -> !l.isEmpty() && l.matches("\\s{4}.+")) //отбрасываем пустые строки и строки с именами ключей
                    .map(l -> l.split("\\s{4}")) //оставшиеся строки делим на элементы по признаку "4 пробела"
//                    .collect(Collectors.toMap(l->l[1],l->l[l.length-1])); //и собираем в мапу, где l[1] - имя параметра, l[l.length-1] - значение
                    .collect(Collectors.toMap(l -> l[1],
                            l -> l.length >= 3 ? l[l.length - 1] : ""));
            valuesOptional = Optional.of(values);
        } catch (IOException | InterruptedException e) {
            LOGGER.error("ОШИБКА. Не удалось получить список параметров реестра в разделе {}. Причина: {}", key, e.getLocalizedMessage());
            LOGGER.debug("Stack trace: ", e);
            Thread.currentThread().interrupt();
        }
        return valuesOptional;
    }

    /**
     * Возвращает список подразделов (подключей) указанного раздела (ключа) реестра
     *
     * @param key раздел (ключ) реестра, подразделы (подключи) которого необходимо получить
     * @return список подразделов (подключей). Если подразделов нет, то возращается список с размером 0.
     * @throws RegistryAccessDeniedException если не найден ключ реестра
     */
    public List<String> getSubkeys(String key) throws IOException, InterruptedException {
        //Вывод имеет следующий формат
        //<key>\subkey1 - первая строка, в моем случае вседга пустая.
        //<key>\subkey2
        //<key>\subkeyN
        WinComExecutor.Result<Integer, String> result = winComExecutor.exec("reg query \"" + key + "\"");
        if (result.getExitCode() != 0){
            String msg = String.format("%s Код: %d\n" +
                    "\tРаздел реестра: %s", result.getBody(),result.getExitCode(), key);
            throw new RegistryAccessDeniedException(msg);
        }
        String output = result.getBody();
        return Arrays.stream(output.split(System.lineSeparator()))
                .filter(s -> !s.isEmpty() &&            //отбрасываем пустые строки
                        s.matches("HKEY.+") &&    //строки с параметрами
                        !s.matches(Pattern.quote(key)))  //отбстроку с именем раздела, в котором ищем подразделы
                .toList();
    }

    /**
     * Получает значение указанного параметра реестра.
     *
     * @param key   путь к ключу в реестре
     * @param value параметр реестра
     * @return {@code Optional<String>}, содержащий значение параметра
     */
    public Optional<String> getValue(String key, String value) {
        Optional<String> valueOptional = Optional.empty();
        try {
            WinComExecutor.Result<Integer, String> result = winComExecutor.exec("reg query " + '"' + key + "\" /v \"" + value + "\"");
            String output = result.getBody();
            int exitCode = result.getExitCode();
            if (exitCode == 1) {
                String msg = String.format("""
                        %s
                        \tРаздел: %s
                        \tПараметр: %s""", output.trim(), key, value);
                throw new NoSuchElementException(msg);
            }
            // Вывод имеет следующий формат:
            // \n<Version information>\n\n<value>\t<registry type>\t<value>
            if (output.matches("\\s+")) {
                return Optional.empty();
            }

            String[] parsed = output.split("\\s{4}"); //в оригинале регулярка была "\t", что давало неверный результат,
            // т.к. в output деление идет четырьмя символами пробела
            valueOptional = Optional.of(parsed[parsed.length - 1]);
        } catch (IOException | InterruptedException e) {
            LOGGER.error("ОШИБКА. Не удалось получить значение параметра {} в разделе {}. Причина: {}", value, key, e.getLocalizedMessage());
            LOGGER.debug("Stack trace: ", e);
            Thread.currentThread().interrupt();
        }
        return valueOptional;
    }

    /**
     * Определяет существование раздела реестра путем получения значения по умолчанию. Если значение имеется, то считается, что
     * раздел существует.
     *
     * @param key раздел, существование которого необходимо проверить.
     * @return true - раздел реестра существует, false - указанного раздела нет.
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException if the current thread is interrupted by another thread while it is waiting, then the wait is ended and an InterruptedException is thrown.
     */
    public boolean isKeyExists(String key) throws IOException, InterruptedException {
        WinComExecutor.Result<Integer, String> result = winComExecutor.exec("reg query \"" + key + "\" /ve ");
        boolean b = result.getExitCode() == 0;
        boolean s = !result.getBody().isEmpty();
        return b && s;
    }

    /**
     * <p>Загружает в куст реестра для дальнейшей работы с ним.</p>
     * <u>Внимание!</u> Требует наличие прав администратора!
     *
     * @param nodeName Имя подраздела реестра, в который загружается файл куста. Создание нового раздела.
     * @param hive     Имя файла куста, подлежащего загрузке.
     * @return {@code WinRegReader.ExecResult}, в котором первое значение код выхода (0 - успешно, 1 - провал), второе - пустая строка.
     * @throws RegistryAccessDeniedException при загрузке куста реестра в отсутствии повышенных привелегий пользователя.
     */
    public WinComExecutor.Result<Integer, String> loadHive(String nodeName, String hive) throws IOException, InterruptedException {
        WinComExecutor.Result<Integer, String> result = winComExecutor.exec("reg load " + nodeName + " \"" + hive + "\"");
        if (result.getExitCode() != 0) {
            String msg = String.format("%s Код: %d%n" +
                    "\tФайл: %s", result.getBody(), result.getExitCode(), hive);
            throw new RegistryAccessDeniedException(msg);
        }
        return result;
    }

    /**
     * <p>Выгружает ранее загруженный куст реестра.</p>
     * <u>Внимание!</u> Требует наличие прав администратора!
     *
     * @param nodeName выгружаемый куст реестра
     * @return {@code WinRegReader.ExecResult}, в котором первое значение код выхода (0 - успешно, 1 - провал), второе - пустая строка.
     * @throws RegistryAccessDeniedException при выгрузке куста реестра в отсутствии повышенных привелегий пользователя или попытке выгрузить
     * несуществующую ветку.
     */
    public WinComExecutor.Result<Integer, String> unloadHive(String nodeName) throws IOException, InterruptedException {
        //TODO делать выгрузку после проверки существования раздела - нужен отдельный метод проверки.

        WinComExecutor.Result<Integer, String> result = winComExecutor.exec("reg unload " + nodeName);
        if (result.getExitCode() != 0){
            String msg = String.format("%s Код: %d", result.getBody(), result.getExitCode());
            throw new RegistryAccessDeniedException(msg);
        }
        return result;
    }
}