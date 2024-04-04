package betterbox.mine.game.betterelo;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class BetterRanksCheaters {
    private JavaPlugin plugin;
    private final PluginLogger pluginLogger;
    private List<String> cheatersList;

    public BetterRanksCheaters(JavaPlugin plugin, PluginLogger pluginLogger) {
        this.plugin = plugin;
        this.pluginLogger = pluginLogger;
        this.cheatersList = new ArrayList<>();
    }

    public void CheckCheatersFromBetterRanks() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3, "CheckCheatersFromBetterRanks called");
        Plugin betterRanksPlugin = plugin.getServer().getPluginManager().getPlugin("BetterRanks");
        if (betterRanksPlugin == null || !betterRanksPlugin.isEnabled()) {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "BetterRanks plugin is not found or is disabled.");
            cheatersList.clear(); // Clear the list to ensure it's empty.
            return;
        }

        File dataFolder = betterRanksPlugin.getDataFolder();
        if (!dataFolder.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "BetterRanks plugin folder doesn't exist!");
            cheatersList.clear(); // Clear the list to ensure it's empty.
            return;
        }

        File configFile = new File(dataFolder, "database.yml");
        if (!configFile.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "BetterRanks database.yml doesn't exist!");
            cheatersList.clear(); // Clear the list to ensure it's empty.
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        Set<String> playerNames = config.getKeys(false);
        pluginLogger.log(PluginLogger.LogLevel.CHEATERS, "BetterRanksCheaters: CheckCheatersFromBetterRanks checking..");
        cheatersList.clear();
        for (String playerName : playerNames) {
            String rank = config.getString(playerName + ".rank");
            if (rank != null && rank.equalsIgnoreCase("CHEATER")) {
                cheatersList.add(playerName);
                pluginLogger.log(PluginLogger.LogLevel.CHEATERS, "BetterRanksCheaters: CheckCheatersFromBetterRanks: adding cheater " + playerName);
            }
        }
        pluginLogger.log(PluginLogger.LogLevel.CHEATERS, "BetterRanksCheaters: CheckCheatersFromBetterRanks: Cheaters found: " + cheatersList);
    }

    public List<String> getCheatersList() {
        pluginLogger.log(PluginLogger.LogLevel.CHEATERS, "BetterRanksCheaters: getCheatersList called");
        return cheatersList;
    }
}
