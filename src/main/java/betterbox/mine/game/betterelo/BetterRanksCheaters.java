package betterbox.mine.game.betterelo;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class BetterRanksCheaters {
    private JavaPlugin plugin;
    private final PluginLogger pluginLogger;
    public List<String> cheatersList;

    public BetterRanksCheaters(JavaPlugin plugin, PluginLogger pluginLogger) {
        this.plugin = plugin;
        this.pluginLogger = pluginLogger;
        this.cheatersList = new ArrayList<>();
    }

    public void CheckCheatersFromBetterRanks() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3,"CheckCheatersFromBetterRanks called");
        File dataFolder = plugin.getServer().getPluginManager().getPlugin("BetterRanks").getDataFolder();
        if (!dataFolder.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "BetterRanks plugin folder doesn't exist!");
            return; // Jeśli folder danych nie istnieje, to nie ma co sprawdzać.
        }

        File configFile = new File(dataFolder, "database.yml");
        if (!configFile.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "BetterRanks database.yml doesn't exist!");
            return; // Jeśli plik database.yml nie istnieje, to nie ma co sprawdzać.
        }


        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        Set<String> playerNames = config.getKeys(false);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3,"BetterRanksCheaters: CheckCheatersFromBetterRanks checking..");
        cheatersList.clear();
        for (String playerName : playerNames) {
            String rank = config.getString(playerName + ".rank");
            if (rank != null && rank.equalsIgnoreCase("CHEATER")) {
                cheatersList.add(playerName);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3,"BetterRanksCheaters: CheckCheatersFromBetterRanks: adding cheater "+playerName);
            }
        }
        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3,"BetterRanksCheaters: CheckCheatersFromBetterRanks: Cheaters found: "+ Arrays.toString(cheatersList.toArray()));
    }

    public List<String> getCheatersList() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3,"BetterRanksCheaters: getCheatersList called");
        return cheatersList;
    }
}
