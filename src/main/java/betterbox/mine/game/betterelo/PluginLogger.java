package betterbox.mine.game.betterelo;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.logging.*;

public class PluginLogger {

    private final File logFile;
    private JavaPlugin plugin;
    private Set<LogLevel> enabledLogLevels; // Zbiór aktywnych poziomów logowania

    // Enumeracja dla poziomów logowania
    public enum LogLevel {
        INFO, WARNING, ERROR, DEBUG, DEBUG_LVL2, DEBUG_LVL3,DEBUG_LVL4
    }

    public PluginLogger(String folderPath, Set<LogLevel> enabledLogLevels, JavaPlugin plugin) {
        this.enabledLogLevels = enabledLogLevels;
        this.plugin = plugin;
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
            plugin.getLogger().severe("PluginLogger: Could not create log file! "+e.getMessage());
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
                plugin.getLogger().severe("PluginLogger: log: Could not write to log file!"+e.getMessage());
            }
        }
    }

    // Metoda do ustawiania aktywnych poziomów logowania
    public void setEnabledLogLevels(Set<LogLevel> configEnabledLogLevels) {
        this.enabledLogLevels = configEnabledLogLevels;
        log("Enabled Log levels "+ Arrays.toString(enabledLogLevels.toArray()));

    }


}
