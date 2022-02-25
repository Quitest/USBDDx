package ru.pel.usbddc.utility;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class WinComExecutor {
    private static Runtime runtime;
    public WinComExecutor() {
        this(Runtime.getRuntime());
    }

    public WinComExecutor(Runtime runtime){
        WinComExecutor.runtime = runtime;
    }

    /**
     * Выполняет указанную команду в отдельном процессе, ждет окончания ее работы и возвращает результат.
     *
     * @param command команда для выполнения
     * @return кортеж (пару значений): первое - код, с которым завершилась команда; второе - сам результат выполнения в
     * виде строки.
     * @throws IOException          If an I/O error occurs
     * @throws InterruptedException if the current thread is interrupted by another thread while it is waiting, then the wait is ended and an InterruptedException is thrown.
     */
    public Result<Integer, String> exec(String command) throws IOException, InterruptedException {
//        Process process = Runtime.getRuntime().exec(command);
        Process process = runtime.exec(command);
//FIXME инфа по Error Stream https://stackoverflow.com/questions/5711084/java-runtime-getruntime-getting-output-from-executing-a-command-line-program
        StreamReader reader = new StreamReader(process.getInputStream(), process.getErrorStream());
        reader.start();
        int exitCode = process.waitFor();
        reader.join();
        String result = reader.getResult();
        return new Result<>(exitCode, result);
    }

    private static class StreamReader extends Thread {
        private static final Logger LOGGER = LoggerFactory.getLogger(WinComExecutor.StreamReader.class);
        private final StringWriter sw = new StringWriter();
        private InputStreamReader inputStreamReader;
        private InputStreamReader errorStreamReader;

        public StreamReader(InputStream is, InputStream err) {
            try {
                this.inputStreamReader = new InputStreamReader(is, "866");
                this.errorStreamReader = new InputStreamReader(err, "866");
            } catch (UnsupportedEncodingException e) {
                LOGGER.error("{}", e.getLocalizedMessage());
                LOGGER.debug("{}", e.toString());
            }
        }

        public String getResult() {
            return sw.toString();
        }

        @Override
        public void run() {
            try {
                int c;
                while ((c = inputStreamReader.read()) != -1) {
                    sw.write(c);
                }

                while ((c = errorStreamReader.read()) != -1){
                    sw.write(c);
                }
            } catch (IOException e) {
                LOGGER.error("{}", e.getLocalizedMessage());
                LOGGER.debug("{}", e.toString());
            }

        }
    }

    @Getter
    @Setter
    public static class Result<C, R> {
        private C exitCode;
        private R body;

        public Result(C exitCode, R body) {
            this.exitCode = exitCode;
            this.body = body;
        }
    }
}
