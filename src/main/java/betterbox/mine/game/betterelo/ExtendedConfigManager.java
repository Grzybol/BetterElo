package betterbox.mine.game.betterelo;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import javax.servlet.annotation.HandlesTypes;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ExtendedConfigManager {
    private JavaPlugin plugin;
    private PluginLogger pluginLogger;
    List<String> logLevels = null;
    List<String> eventItemsPlaceholder = null;
    Set<PluginLogger.LogLevel> enabledEventItemsPlaceholder;
    private File configFile = null;
    Set<PluginLogger.LogLevel> enabledLogLevels;

    public Double blockBase = 0.1;
    public Double antywebCost = 0.1;
    public int fireworkCooldown = 1500;

    public ExtendedConfigManager(JavaPlugin plugin, PluginLogger pluginLogger) {
        this.plugin = plugin;
        this.pluginLogger = pluginLogger;
        configureLogger();
    }
    private Map<String, Double> blockRewards = new HashMap<>();

    public Map<String, Double> getBlockRewards() {
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
        ReloadConfig();
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
            plugin.saveConfig();

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
                double reward = plugin.getConfig().getDouble("blocks_rewards." + blockType);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL4, blockType+": "+reward);

                blockRewards.put(blockType, reward);
            }
        } else {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "ConfigManager: ReloadConfig: blocks_rewards section not found in config! Creating new section..");
            blocksRewardsSection = plugin.getConfig().createSection("blocks_rewards");
            blocksRewardsSection.set("DIAMOND_ORE", 4);
            blocksRewardsSection.set("DIAMOND_BLOCK", 4);
            plugin.saveConfig();

        }

        blockBase = plugin.getConfig().getDouble("block_base");
        if (plugin.getConfig().contains("block_base")){
            if (plugin.getConfig().isDouble("block_base")){
                blockBase = plugin.getConfig().getDouble("block_base");
            }
            else {
                pluginLogger.log(PluginLogger.LogLevel.WARNING, "ConfigManager: ReloadConfig: block_base incorrect! Restoring default");
                plugin.getConfig().set("block_base", 0.1);
                plugin.saveConfig();
            }
        }
        else{
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "ConfigManager: ReloadConfig: block_base section not found in config! Creating new section..");
            plugin.getConfig().createSection("block_base");
            plugin.getConfig().set("block_base", 0.1);
            //blockBaseSection.set("value", 0.1);
            plugin.saveConfig();
        }
        antywebCost = plugin.getConfig().getDouble("antyweb_elo_cost");
        if (plugin.getConfig().contains("antyweb_elo_cost")){
            if (plugin.getConfig().isDouble("antyweb_elo_cost")){
                antywebCost = plugin.getConfig().getDouble("antyweb_elo_cost");
            }
            else {
                pluginLogger.log(PluginLogger.LogLevel.WARNING, "ConfigManager: ReloadConfig: antyweb_elo_cost incorrect! Restoring default");
                plugin.getConfig().set("antyweb_elo_cost", 1.5);
                plugin.saveConfig();
            }
        }
        else{
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "ConfigManager: ReloadConfig: block_base section not found in config! Creating new section..");
            plugin.getConfig().createSection("antyweb_elo_cost");
            plugin.getConfig().set("antyweb_elo_cost", 0.1);
            //blockBaseSection.set("value", 0.1);
            plugin.saveConfig();
        }

        //Firework cost
        fireworkCooldown = plugin.getConfig().getInt("Infinite_firework_cooldown");
        if (plugin.getConfig().contains("Infinite_firework_cooldown")){
            if (plugin.getConfig().isInt("Infinite_firework_cooldown")){
                fireworkCooldown = plugin.getConfig().getInt("Infinite_firework_cooldown");
            }
            else {
                pluginLogger.log(PluginLogger.LogLevel.WARNING, "ConfigManager: ReloadConfig: Infinite_firework_cooldown incorrect! Restoring default");
                plugin.getConfig().set("Infinite_firework_cooldown", 1500);
                plugin.saveConfig();
            }
        }
        else{
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "ConfigManager: ReloadConfig: block_base section not found in config! Creating new section..");
            plugin.getConfig().createSection("Infinite_firework_cooldown");
            plugin.getConfig().set("Infinite_firework_cooldown", 1500);
            //blockBaseSection.set("value", 0.1);
            plugin.saveConfig();
        }







        //wczytywanie listy itemow do ktorych bedzie przypisywana nazwa gracza przy rozdaniu nagrod
/*
        ConfigurationSection eventItemsSection = plugin.getConfig().getConfigurationSection("event_items_with_username");
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"extendedConfigManager.ReloadConfig checking event_items_with_username section");
        if (eventItemsSection != null) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"extendedConfigManager.ReloadConfig event_items_with_username section not null");
            // Jeśli sekcja istnieje, odczytaj jej zawartość i zapisz w pamięci
            for (String eventItem : eventItemsSection.getKeys(false)) {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"extendedConfigManager.ReloadConfig event_items_with_username section checking item "+eventItem);
                String ItemID = plugin.getConfig().getString("event_items_with_username." +eventItem);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, eventItem+":"+ ItemID);
                eventItemsPlaceholder.add(ItemID);
            }
        } else {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "ConfigManager: ReloadConfig: event_items_with_username section not found in config! Creating new section..");
            eventItemsSection = plugin.getConfig().createSection("event_items_with_username");
            //eventItemsSection.setComments("event_items_with_username", Collections.singletonList("To the lore of these items will be added a name of the player that received the item"));
            plugin.saveConfig();
            }

 */
// Wczytywanie listy itemów, do których będzie przypisywana nazwa gracza przy rozdaniu nagród
        List<String> eventItemsWithUsername = plugin.getConfig().getStringList("event_items_with_username");
        if (eventItemsWithUsername == null || eventItemsWithUsername.isEmpty()) {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "ConfigManager: ReloadConfig: event_items_with_username section not found in config or is empty! Creating new section with default values.");
            // Tutaj możesz ustawić domyślne wartości, jeśli lista jest pusta lub nie istnieje
            plugin.getConfig().set("event_items_with_username", Arrays.asList("DIAMOND_PICKAXE")); // Przykład domyślnych wartości
            plugin.saveConfig();
        } else {
            // Lista istnieje i ma elementy, logowanie lub inne operacje
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "event_items_with_username loaded: " + eventItemsWithUsername.toString());
            // Możesz tutaj przypisać wczytaną listę do zmiennej klasy, jeśli potrzebujesz
            this.eventItemsPlaceholder = eventItemsWithUsername;
        }





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
