package betterbox.mine.game.betterelo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

public class PluginLogger {

    private final File logFile;
    private Set<LogLevel> enabledLogLevels; // Zbiór aktywnych poziomów logowania

    // Enumeracja dla poziomów logowania
    public enum LogLevel {
        INFO, WARNING, ERROR, DEBUG
    }

    public PluginLogger(String folderPath, Set<LogLevel> enabledLogLevels) {
        this.enabledLogLevels = enabledLogLevels;

        // Tworzenie folderu dla logów, jeśli nie istnieje
        File logFolder = new File(folderPath,"logs");
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

    // Metoda do logowania z domyślnym poziomem INFO
    public void log(String message) {
        log(LogLevel.INFO, message);
    }

    // Metoda do logowania z określonym poziomem
    public void log(LogLevel level, String message) {
        if (enabledLogLevels.contains(level)) {
            // Dodanie timestampu i poziomu logowania do wiadomości
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String logMessage = timestamp + " [" + level + "] - " + message;

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                writer.write(logMessage);
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace(); // Tu można użyć loggera serwera do zalogowania błędu
            }
        }
    }

    // Metoda do ustawiania aktywnych poziomów logowania
    public void setEnabledLogLevels(Set<LogLevel> enabledLogLevels) {
        this.enabledLogLevels = enabledLogLevels;
    }


}
