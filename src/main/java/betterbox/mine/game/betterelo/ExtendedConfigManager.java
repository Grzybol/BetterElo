package betterbox.mine.game.betterelo;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ExtendedConfigManager {
    private JavaPlugin plugin;
    private PluginLogger pluginLogger;

    public ExtendedConfigManager(JavaPlugin plugin, PluginLogger pluginLogger) {
        this.plugin = plugin;
        this.pluginLogger = pluginLogger;
    }

    public void updateConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");

        // Sprawdź, czy plik konfiguracyjny istnieje
        if (!configFile.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "Config file does not exist, creating new one.");
            plugin.saveDefaultConfig();
        }

        // Przeczytaj plik konfiguracyjny
        try {
            List<String> lines = Files.readAllLines(Paths.get(configFile.toURI()));

            // Dodaj nowe zmienne konfiguracyjne
            lines.add("###################################");
            // Tutaj możemy dodać nowe zmienne konfiguracyjne
            // Na przykład: lines.add("nowa_zmienna: wartosc");

            // Zapisz zmodyfikowany plik
            Files.write(Paths.get(configFile.toURI()), lines);
            pluginLogger.log(PluginLogger.LogLevel.INFO, "Config file updated successfully.");

        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Error while updating config file: " + e.getMessage());
        }
    }
}
