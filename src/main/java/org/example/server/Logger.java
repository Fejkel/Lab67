import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final Path LOG_FILE = Paths.get("server_logs.txt");

    // Formatowanie daty i czasu
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Metoda do logowania wiadomości
    public static void log(String message) {
        String timestamp = "[" + LocalDateTime.now().format(formatter) + "] ";
        String logEntry = timestamp + message + System.lineSeparator();

        try {
            // Tworzenie pliku, jeśli nie istnieje, i dopisanie logu
            Files.writeString(LOG_FILE, logEntry, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }
}