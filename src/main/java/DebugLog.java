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

    /*
    Guards logWriter. Burp calls the HTTP handler on many threads at once, so log() runs concurrently;
    unsynchronized writes to a shared FileWriter can interleave mid-line and produce entries that are
    two messages spliced together.
     */
    private static final Object WRITE_LOCK = new Object();

    // Where log files go. Overridable so the log can be redirected, which is also how the flush
    // behaviour below gets tested.
    public static final String LOG_DIR_PROPERTY = "ape.log.dir";

    public static void init (MontoyaApi montoyaApi){
        api = montoyaApi;

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
                // The parameter, not Controller.montoyaApi, so init() does not depend on the static
                // having been assigned first.
                montoyaApi.logging().logToOutput("Log saved to: " + logFilePath);
                DebugLog.log("=== Logging session started ===");
            }
            catch (Exception e) {
                api.logging().logToError("Failed to create log file: " + e.getMessage());
            }
        }

        montoyaApi.extension().registerUnloadingHandler(() -> {
            DebugLog.log("Extension is being unloaded or burp closed");
            DebugLog.close();
        });
    }

    public static void log (String message) {
        if (BuildConfig.DEBUG) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
            String logMessage = "[" + timestamp + "] " + message;

            // Outside of Burp (e.g. the logic harness in src/test/java) there is no Montoya API to
            // log to, so fall back to stdout.
            if (api != null) {
                api.logging().logToOutput(logMessage);
            }
            else {
                System.out.println(logMessage);
            }

            /*
            One writer shared by every Burp thread, so the write is serialised. Flushed on every line
            rather than left in FileWriter's buffer: the log exists to diagnose crashes and hangs, and
            an unflushed buffer loses exactly the last few lines before one, which are the interesting
            ones. Debug logging is not on a hot path, so the cost does not matter.
             */
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
    }

    private static String generateRandomId() {
        Random random = new Random();
        return String.format("%06x", random.nextInt(0xFFFFFF));
    }

    public static void close() {
        // Logged outside the lock, since log() takes it itself.
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
                // Cleared so a log() arriving after unload is dropped rather than writing to a closed
                // writer. Burp can keep calling into an extension briefly while it unloads.
                logWriter = null;
            }
        }
    }

    // The file being written, or null when no log file was opened. Used by the test harness.
    static Path logFilePath() {
        return logFilePath;
    }
}
