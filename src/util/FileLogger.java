package util;

import java.io.*;
import java.time.LocalDateTime;

public class FileLogger {
    private static final String LOG_FILE = "hospital_log.txt";

    public static void log(String username, String action) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            pw.println("[" + LocalDateTime.now() + "] " + username + " -> " + action);
        } catch (IOException e) {
            System.out.println("Logger error: " + e.getMessage());
        }
    }
}