package ru.pel.usbddc.service;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class WinComExecutor {
    private WinComExecutor() {
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
    public static Result<Integer, String> exec(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);

        StreamReader reader = new StreamReader(process.getInputStream());
        reader.start();
        int exitCode = process.waitFor();
        reader.join();
        String result = reader.getResult();
        return new Result<>(exitCode, result);
    }

    private static class StreamReader extends Thread {
        private static final Logger LOGGER = LoggerFactory.getLogger(WinComExecutor.StreamReader.class);
        private final StringWriter sw = new StringWriter();
        private InputStreamReader isr;

        public StreamReader(InputStream is) {
            try {
                this.isr = new InputStreamReader(is, "866");
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
                while ((c = isr.read()) != -1) {
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
