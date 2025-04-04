package swu.cp112.silkblade.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GameLogger {
    private static final String LOG_FILE = "game_log.txt";
    private static FileHandle logFile;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        // Initialize the log file in the local directory
        logFile = Gdx.files.local(LOG_FILE);
    }

    public static void logError(String message, Throwable error) {
        try {
            // Convert stack trace to string
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            error.printStackTrace(pw);
            String stackTrace = sw.toString();

            // Format the log entry
            String timestamp = dateFormat.format(new Date());
            String logEntry = String.format(
                "=== Error Log Entry ===\n" +
                "Time: %s\n" +
                "Message: %s\n" +
                "Stack Trace:\n%s\n" +
                "==================\n\n",
                timestamp, message, stackTrace
            );

            // Append to log file
            logFile.writeString(logEntry, true);

            // Also print to console
            System.err.println(logEntry);
        } catch (Exception e) {
            // If logging fails, at least print to console
            System.err.println("Failed to log error: " + e.getMessage());
            error.printStackTrace();
        }
    }

    public static void logInfo(String message) {
        try {
            String timestamp = dateFormat.format(new Date());
            String logEntry = String.format(
                "[%s] INFO: %s\n",
                timestamp, message
            );

            logFile.writeString(logEntry, true);
            System.out.println(logEntry);
        } catch (Exception e) {
            System.err.println("Failed to log info: " + e.getMessage());
        }
    }

    public static void clearLog() {
        try {
            logFile.writeString("", false);
        } catch (Exception e) {
            System.err.println("Failed to clear log: " + e.getMessage());
        }
    }

    public static String getLogContents() {
        try {
            return logFile.readString();
        } catch (Exception e) {
            return "Failed to read log: " + e.getMessage();
        }
    }
}
