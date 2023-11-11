package betterbox.mine.game.betterelo;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExtendedConfigManager {
    private JavaPlugin plugin;
    private PluginLogger pluginLogger;

    public ExtendedConfigManager(JavaPlugin plugin, PluginLogger pluginLogger) {
        this.plugin = plugin;
        this.pluginLogger = pluginLogger;
        configureLogger();
    }

    private void configureLogger() {
        // Odczytanie ustawień log_level z pliku konfiguracyjnego
        List<String> logLevels = plugin.getConfig().getStringList("log_level");
        Set<PluginLogger.LogLevel> enabledLogLevels;

        if (logLevels == null || logLevels.isEmpty()) {
            // Jeśli konfiguracja nie określa poziomów logowania, użyj domyślnych ustawień
            enabledLogLevels = EnumSet.of(PluginLogger.LogLevel.INFO, PluginLogger.LogLevel.WARNING, PluginLogger.LogLevel.ERROR);

        } else {
            enabledLogLevels = new HashSet<>();
            for (String level : logLevels) {
                try {
                    enabledLogLevels.add(PluginLogger.LogLevel.valueOf(level.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    // Jeśli podano nieprawidłowy poziom logowania, zaloguj błąd
                    plugin.getServer().getLogger().warning("Invalid log level in config: " + level);
                }
            }
        }

        // Ustawienie aktywnych poziomów logowania w loggerze
        pluginLogger.setEnabledLogLevels(enabledLogLevels);
    }

    public void updateConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "Config file does not exist, creating new one.");
            plugin.saveDefaultConfig();
        }

        try {
            List<String> lines = Files.readAllLines(Paths.get(configFile.toURI()));

            // Dodaj nowe zmienne konfiguracyjne
            lines.add("###################################");
            String defaultLogConfig = "log_level:\n  - INFO\n  - WARNING\n  - ERROR";
            lines.add(defaultLogConfig);
            // Tutaj możemy dodać nowe zmienne konfiguracyjne
            // ...

            Files.write(Paths.get(configFile.toURI()), lines);
            pluginLogger.log(PluginLogger.LogLevel.INFO, "Config file updated successfully.");
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Error while updating config file: " + e.getMessage());
        }
    }
}
