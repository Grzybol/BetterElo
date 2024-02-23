package betterbox.mine.game.betterelo;

import org.bukkit.configuration.ConfigurationSection;
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

    public Double blockBase = 0.1;

    public ExtendedConfigManager(JavaPlugin plugin, PluginLogger pluginLogger) {
        this.plugin = plugin;
        this.pluginLogger = pluginLogger;
        configureLogger();
    }
    private Map<String, Integer> blockRewards = new HashMap<>();

    public Map<String, Integer> getBlockRewards() {
        return blockRewards;
    }

    private void configureLogger() {
        // Odczytanie ustawień log_level z pliku konfiguracyjnego
        List<String> logLevels = plugin.getConfig().getStringList("log_level");
        Set<PluginLogger.LogLevel> enabledLogLevels;

        if (logLevels == null || logLevels.isEmpty()) {
            // Jeśli konfiguracja nie określa poziomów logowania, użyj domyślnych ustawień
            enabledLogLevels = EnumSet.of(PluginLogger.LogLevel.INFO, PluginLogger.LogLevel.WARNING, PluginLogger.LogLevel.ERROR);
            updateConfig("\nlog_level:\n  - INFO\n  - WARNING\n  - ERROR\nblocks_rewards:\n   DIAMOND_ORE: 4\n   DIAMOND_BLOCK: 4\n   EMERALD_ORE: 3\n   EMERALD_BLOCK: 3\n   GOLD_ORE: 2\n   GOLD_BLOCK: 2\n   IRON_ORE: 1\n   IRON_BLOCK: 1\nblock_base: 0.1");
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


        ConfigurationSection blocksRewardsSection = plugin.getConfig().getConfigurationSection("blocks_rewards");
        if (blocksRewardsSection != null) {
            // Jeśli sekcja istnieje, odczytaj jej zawartość i zapisz w pamięci
            for (String blockType : blocksRewardsSection.getKeys(false)) {
                int reward = plugin.getConfig().getInt("blocks_rewards." + blockType);
                blockRewards.put(blockType, reward);
            }
        } else {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "ConfigManager: ReloadConfig: blocks_rewards section not found in config! Creating new section..");
            blocksRewardsSection = plugin.getConfig().createSection("blocks_rewards");
            blocksRewardsSection.addDefault("DIAMOND_ORE", 4);
            blocksRewardsSection.addDefault("DIAMOND_BLOCK", 4);

        }
        blockBase = plugin.getConfig().getDouble("block_base");
        if (plugin.getConfig().contains("block_base")){
            if (plugin.getConfig().isDouble("block_base")){
                blockBase = plugin.getConfig().getDouble("block_base");
            }
            else {
                pluginLogger.log(PluginLogger.LogLevel.WARNING, "ConfigManager: ReloadConfig: block_base incorrect! Restoring default");
                plugin.getConfig().set("block_base", 0.1);
            }
        }
        else{
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "ConfigManager: ReloadConfig: block_base section not found in config! Creating new section..");
            plugin.getConfig().addDefault("block_base", 0.1);
        }
        plugin.saveConfig();

    }

    public void updateConfig(String configuration) {
        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "Config file does not exist, creating new one.");
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                pluginLogger.log(PluginLogger.LogLevel.ERROR, "Error while creating config file: " + e.getMessage());
            }
        }

        try {
            // Usunięcie istniejących linii z log_level
            List<String> lines = Files.readAllLines(Paths.get(configFile.toURI()));
            lines.removeIf(line -> line.trim().startsWith("log_level:"));

            // Dodanie nowych danych konfiguracyjnych
            lines.add("###################################");
            lines.add(configuration);

            Files.write(Paths.get(configFile.toURI()), lines);
            pluginLogger.log(PluginLogger.LogLevel.INFO, "Config file updated successfully.");
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Error while updating config file: " + e.getMessage());
        }
    }


}
