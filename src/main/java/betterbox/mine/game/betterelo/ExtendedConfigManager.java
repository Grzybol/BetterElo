package betterbox.mine.game.betterelo;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ExtendedConfigManager {
    private JavaPlugin plugin;
    private PluginLogger pluginLogger;
    List<String> logLevels = null;
    private File configFile = null;
    Set<PluginLogger.LogLevel> enabledLogLevels;
    private List<String> excludedRegions;

    public ExtendedConfigManager(JavaPlugin plugin, PluginLogger pluginLogger) {
        this.plugin = plugin;
        this.pluginLogger = pluginLogger;
        configureLogger();
        excludedRegions = new ArrayList<>();
    }
    public List<String> getExcludedRegions() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "ExtendedConfigManager: getExcludedRegions called");
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "ExtendedConfigManager: getExcludedRegions returning "+ Arrays.toString(excludedRegions.toArray()));
        return excludedRegions;
    }


    private void configureLogger() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "ExtendedConfigManager: configureLogger called");
        // Odczytanie ustawień log_level z pliku konfiguracyjnego
        List<String> logLevels = plugin.getConfig().getStringList("log_level");
        Set<PluginLogger.LogLevel> enabledLogLevels;

        if (logLevels == null || logLevels.isEmpty()) {
            // Jeśli konfiguracja nie określa poziomów logowania, użyj domyślnych ustawień
            enabledLogLevels = EnumSet.of(PluginLogger.LogLevel.INFO, PluginLogger.LogLevel.WARNING, PluginLogger.LogLevel.ERROR);
            updateConfig("log_level:\n  - INFO\n  - WARNING\n  - ERROR");
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
        pluginLogger.setEnabledLogLevels(enabledLogLevels);
        excludedRegions = plugin.getConfig().getStringList("excluded_regions");

        if (excludedRegions == null || excludedRegions.isEmpty()) {
            // Jeśli nie ma zdefiniowanych wyłączonych regionów, dodaj domyślny "spawn"
            excludedRegions = new ArrayList<>();
            excludedRegions.add("spawn");
            // Zaktualizuj konfigurację, żeby zapisać domyślny region
            updateConfig("excluded_regions:\n  - spawn");
        }
    }
    public void ReloadConfig() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "ConfigManager: ReloadConfig called");

        // Odczytanie ustawień log_level z pliku konfiguracyjnego
        configFile = new File(plugin.getDataFolder(), "config.yml");
        plugin.reloadConfig();
        logLevels = plugin.getConfig().getStringList("log_level");
        enabledLogLevels = new HashSet<>();

        if (logLevels == null || logLevels.isEmpty()) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "ConfigManager: ReloadConfig: no config file or no configured log levels! Saving default settings.");
            // Jeśli konfiguracja nie określa poziomów logowania, użyj domyślnych ustawień
            enabledLogLevels = EnumSet.of(PluginLogger.LogLevel.INFO, PluginLogger.LogLevel.WARNING, PluginLogger.LogLevel.ERROR);
            updateConfig("log_level:\n  - INFO\n  - WARNING\n  - ERROR");
        }

        for (String level : logLevels) {
            try {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2, "ConfigManager: ReloadConfig: adding " + level.toUpperCase());
                enabledLogLevels.add(PluginLogger.LogLevel.valueOf(level.toUpperCase()));
                pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2, "ConfigManager: ReloadConfig: current log levels: " + Arrays.toString(enabledLogLevels.toArray()));
            } catch (IllegalArgumentException e) {
                // Jeśli podano nieprawidłowy poziom logowania, zaloguj błąd
                plugin.getServer().getLogger().warning("Invalid log level in config: " + level);
            }
        }

        pluginLogger.setEnabledLogLevels(enabledLogLevels);

        // Odczytanie nowych wyłączonych regionów
        List<String> newExcludedRegions = plugin.getConfig().getStringList("excluded_regions");

        if (newExcludedRegions != null) {
            excludedRegions.clear();
            excludedRegions.addAll(newExcludedRegions);
        } else {
            // Jeśli nie ma zdefiniowanych wyłączonych regionów, dodaj domyślny "spawn"
            excludedRegions = new ArrayList<>();
            excludedRegions.add("spawn");
            // Zaktualizuj konfigurację, żeby zapisać domyślny region
            updateConfig("excluded_regions:\n  - spawn");
        }
    }


    public void updateConfig(String configuration) {

        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "Config file does not exist, creating new one.");
            plugin.saveDefaultConfig();
        }

        try {
            List<String> lines = Files.readAllLines(Paths.get(configFile.toURI()));

            // Dodaj nowe zmienne konfiguracyjne
            lines.add("###################################");
            lines.add(configuration);
            // Tutaj możemy dodać nowe zmienne konfiguracyjne
            // ...

            Files.write(Paths.get(configFile.toURI()), lines);
            pluginLogger.log(PluginLogger.LogLevel.INFO, "Config file updated successfully.");
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Error while updating config file: " + e.getMessage());
        }
        try {
            List<String> lines = Files.readAllLines(Paths.get(configFile.toURI()));
            if (!excludedRegions.isEmpty()) {
                lines.add("excluded_regions:");
                for (String region : excludedRegions) {
                    lines.add("  - " + region);
                }
            }

            // ...

            Files.write(Paths.get(configFile.toURI()), lines);
            pluginLogger.log(PluginLogger.LogLevel.INFO, "Config file updated successfully.");
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Error while updating config file: " + e.getMessage());
        }
    }
}
