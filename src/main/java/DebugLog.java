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

    public static void init (MontoyaApi montoyaApi){
        api = montoyaApi;


        if (BuildConfig.DEBUG) {
            try {
                String userHome = System.getProperty("user.home");
                Path logDir = Paths.get(userHome, ".burp-extension-logs");

                logDir.toFile().mkdirs();

                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
                String identifier = generateRandomId();

                String filename = String.format("APE-log_%s_%s.txt", timestamp, identifier);
                logFilePath = logDir.resolve(filename);

                logWriter = new FileWriter(logFilePath.toFile(), false);
            }
            catch (Exception e) {
                api.logging().logToError("Failed to create log file: " + e.getMessage());
            }
        }
    }

    public static void log (String message) {
        if (BuildConfig.DEBUG) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
            String logMessage = "[" + timestamp + "]" + message;

            api.logging().logToOutput(logMessage);

            if (logWriter != null) {
                try {
                    logWriter.write(logMessage + System.lineSeparator());
                }
                catch (IOException e) {
                    api.logging().logToError("Log write failed: " + e.getMessage());
                }
            }
        }
    }

    private static String generateRandomId() {
        Random random = new Random();
        return String.format("%06x", random.nextInt(0xFFFFFF));
    }

    public static void close() {
        if (logWriter != null) {
            try {
                log("=== Logging session ended ===");
                logWriter.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
}
