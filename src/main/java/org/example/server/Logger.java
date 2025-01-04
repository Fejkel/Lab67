package org.example.server;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final Path LOG_FILE = Paths.get("server_logs.txt");

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void log(String message) {
        String timestamp = "[" + LocalDateTime.now().format(formatter) + "] ";
        String logEntry = timestamp + message + System.lineSeparator();
        try {
            Files.writeString(LOG_FILE, logEntry, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }
}