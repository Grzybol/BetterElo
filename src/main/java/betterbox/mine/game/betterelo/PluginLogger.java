package betterbox.mine.game.betterelo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PluginLogger {

    private final File logFile;

    // Enumeracja dla poziomów logowania
    public enum LogLevel {
        INFO, WARNING, ERROR, DEBUG
    }

    public PluginLogger(String folderPath) {
        // Tworzenie folderu dla logów, jeśli nie istnieje
        File logFolder = new File(folderPath);
        if (!logFolder.exists()) {
            logFolder.mkdirs();
        }

        // Utworzenie obiektu File dla pliku logów
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date date = new Date();
        String fileName = formatter.format(date) + ".log";
        logFile = new File(logFolder, fileName);

        try {
            // Jeśli plik nie istnieje, to go utworzymy
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace(); // Tu można użyć loggera serwera do zalogowania błędu
        }
    }

    public void log(String message) {
        log(LogLevel.INFO, message);
    }

    // Przeciążona metoda logowania, która obsługuje poziomy logowania
    public void log(LogLevel level, String message) {
        // Dodanie timestampu i poziomu logowania do wiadomości
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logMessage = timestamp + " [" + level.toString() + "] - " + message;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(logMessage);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace(); // Tu można użyć loggera serwera do zalogowania błędu
        }
    }
}
