import burp.api.montoya.MontoyaApi;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class DebugLog {
    private static MontoyaApi api;
    private static FileWriter logWriter;
    private static Path logFilePath;

    // Guards logWriter. Burp calls the HTTP handler on many threads, so writes could interleave.
    private static final Object WRITE_LOCK = new Object();

    // System property that overrides the log directory. Used by the test harnesses.
    public static final String LOG_DIR_PROPERTY = "ape.log.dir";

    public static void init(MontoyaApi montoyaApi) {
        api = montoyaApi;

        // Lines always go to Burp's extension output. Only debug mode writes a file as well.
        if (BuildConfig.DEBUG) {
            try {
                String override = System.getProperty(LOG_DIR_PROPERTY);
                Path logDir = override != null
                        ? Paths.get(override)
                        : Paths.get(System.getProperty("user.home"), ".burp-extension-logs");

                logDir.toFile().mkdirs();

                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
                String identifier = generateRandomId();

                String filename = String.format("APE-log_%s_%s.txt", timestamp, identifier);
                logFilePath = logDir.resolve(filename);

                logWriter = new FileWriter(logFilePath.toFile(), false);
                montoyaApi.logging().logToOutput("Log saved to: " + logFilePath);
                log("=== Logging session started ===");
            }
            catch (Exception e) {
                montoyaApi.logging().logToError("Failed to create log file: " + e.getMessage());
            }
        }

        montoyaApi.extension().registerUnloadingHandler(() -> {
            log("Extension is being unloaded or burp closed");
            close();
        });
    }

    public static void log(String message) {
        log(message, null);
    }

    // An [Error] line with the exception's stack trace, for errors Burp wouldn't otherwise report.
    public static void error(String message, Throwable cause) {
        log("[Error] " + message + " " + cause, cause);
    }

    private static void log(String message, Throwable cause) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        String logMessage = "[" + timestamp + "] " + message;
        boolean isError = message.startsWith("[Error]");

        // [Error] lines go to Burp's Errors tab, everything else to Output. No Montoya API outside of
        // Burp (the test harnesses), so fall back to stdout / stderr.
        if (api != null) {
            if (!isError) {
                api.logging().logToOutput(logMessage);
            }
            else if (cause != null) {
                api.logging().logToError(logMessage, cause);
            }
            else {
                api.logging().logToError(logMessage);
            }
        }
        else {
            (isError ? System.err : System.out).println(logMessage);
            if (cause != null) {
                cause.printStackTrace();
            }
        }

        // Flushed every line so the last lines before a crash aren't lost.
        synchronized (WRITE_LOCK) {
            if (logWriter == null) {
                return;
            }

            try {
                logWriter.write(logMessage + System.lineSeparator());
                logWriter.flush();
            }
            catch (IOException e) {
                if (api != null) {
                    api.logging().logToError("Log write failed: " + e.getMessage());
                }
                else {
                    System.err.println("Log write failed: " + e.getMessage());
                }
            }
        }
    }

    private static String generateRandomId() {
        Random random = new Random();
        return String.format("%06x", random.nextInt(0xFFFFFF));
    }

    public static void close() {
        log("=== Logging session ended ===");

        synchronized (WRITE_LOCK) {
            if (logWriter == null) {
                return;
            }

            try {
                logWriter.close();
            }
            catch (IOException e) {
                // Ignore
            }
            finally {
                // Burp can still call log() briefly while unloading.
                logWriter = null;
            }
        }
    }

    // The file being written, or null when no log file was opened. Used by the test harnesses.
    static Path logFilePath() {
        return logFilePath;
    }
}
