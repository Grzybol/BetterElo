package betterbox.mine.game.betterelo;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class Lang {
    private final JavaPlugin plugin;
    private final PluginLogger pluginLogger;
    public String currentBonusString = "§6§lCurrent Bonus: ";
    public String newAvgBonusString = "New Average Damage Bonus: ";
    public String mobDamageLore = "§6§lMob Damage ";
    public String averageDamageLore = "§6§lAverage Damage +";
    public String enchantItemName = (ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Enchant Item");
    public List<String> chatNotifierMessagesList = List.of(" Remember to use /be claim to claim your rewards!", " Use /shop  to get our Item-Shop link", " Use /discord  to get our Discord link");
    public List<String> enchantItemLore = List.of(ChatColor.GRAY + "Removes the current Average Damage bonus", ChatColor.GRAY + " from the item and adds a new one.");
    public String mobDefenseLore = "§6§lMob Defense ";
    public String pointsLostMessage = "You have lost";
    public String pointsGainedMessage = "You have gained";
    public String eloMultiplierMessage = "Elo multiplier: x";
    public String eloDifferenceTooBig = "Your Elo difference in the ranking is too big! No reward for this one";
    public String moneyAddedMessage = "Money added: ";
    public String antywebMessage = "Elo cost for removing webs: ";
    public String zephyrErrorMessage = "You cannot use Zephyr while wearing Elytra";
    public String noEloZoneMessage = "No elo reward in this zone!";
    public String noPermissionMessage = "You do not have permission to use this command!";
    public String remainingTimeMessage = "Remaining time: ";
    public String dailyTranslation = "Daily";
    public String weeklyTranslation = "Weekly";
    public String monthlyTranslation = "Monthly";
    public String eventTranslation = "Event";
    public String rankingRewardMessage = " rewarded for reaching";
    public String rewardForLore = "Reward for ";
    public String prefix = ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]";

    public Lang(JavaPlugin plugin, PluginLogger pluginLogger) {
        this.plugin = plugin;
        this.pluginLogger = pluginLogger;
        loadLangFile();
    }

    public void loadLangFile() {
        String transactionID = UUID.randomUUID().toString();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Lang.loadLangFile called", transactionID);
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Creating lang directory...", transactionID);
            langDir.mkdirs();
        }

        File langFile = new File(langDir, "lang.yml");
        if (!langFile.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Creating lang.yml file...", transactionID);
            createDefaultLangFile(langFile, transactionID);
        }

        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Loading lang.yml file...", transactionID);
        FileConfiguration config = YamlConfiguration.loadConfiguration(langFile);
        validateAndLoadConfig(config, langFile, transactionID);
    }

    private void createDefaultLangFile(File langFile, String transactionID) {
        try {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Creating lang.yml file...", transactionID);
            langFile.createNewFile();
            FileConfiguration config = YamlConfiguration.loadConfiguration(langFile);
            setDefaultValues(config);
            config.save(langFile);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "lang.yml file created successfully!", transactionID);
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Error creating lang.yml file: " + e.getMessage(), transactionID);
        }
    }

    private void setDefaultValues(FileConfiguration config) {
        config.set("currentBonusString", currentBonusString);
        config.set("newAvgBonusString", newAvgBonusString);
        config.set("mobDamageLore", mobDamageLore);
        config.set("averageDamageLore", averageDamageLore);
        config.set("enchantItemName", enchantItemName);
        config.set("enchantItemLore", enchantItemLore);
        config.set("mobDefenseLore", mobDefenseLore);
        config.set("pointsLostMessage", pointsLostMessage);
        config.set("pointsGainedMessage", pointsGainedMessage);
        config.set("eloMultiplierMessage", eloMultiplierMessage);
        config.set("eloDifferenceTooBig", eloDifferenceTooBig);
        config.set("moneyAddedMessage", moneyAddedMessage);
        config.set("antywebMessage", antywebMessage);
        config.set("zephyrErrorMessage", zephyrErrorMessage);
        config.set("noEloZoneMessage", noEloZoneMessage);
        config.set("noPermissionMessage", noPermissionMessage);
        config.set("remainingTimeMessage", remainingTimeMessage);
        config.set("dailyTranslation", dailyTranslation);
        config.set("weeklyTranslation", weeklyTranslation);
        config.set("monthlyTranslation", monthlyTranslation);
        config.set("eventTranslation", eventTranslation);
        config.set("rankingRewardMessage", rankingRewardMessage);
        config.set("rewardForLore", rewardForLore);
        config.set("chatNotifierMessagesList", chatNotifierMessagesList);
        config.set("prefix", prefix);
    }

    private void validateAndLoadConfig(FileConfiguration config, File langFile, String transactionID) {
        boolean saveRequired = false;

        if (!config.contains("currentBonusString")) {
            config.set("currentBonusString", currentBonusString);
            saveRequired = true;
        } else {
            currentBonusString = config.getString("currentBonusString");
        }

        if (!config.contains("newAvgBonusString")) {
            config.set("newAvgBonusString", newAvgBonusString);
            saveRequired = true;
        } else {
            newAvgBonusString = config.getString("newAvgBonusString");
        }

        if (!config.contains("mobDamageLore")) {
            config.set("mobDamageLore", mobDamageLore);
            saveRequired = true;
        } else {
            mobDamageLore = config.getString("mobDamageLore");
        }

        if (!config.contains("averageDamageLore")) {
            config.set("averageDamageLore", averageDamageLore);
            saveRequired = true;
        } else {
            averageDamageLore = config.getString("averageDamageLore");
        }

        if (!config.contains("enchantItemName")) {
            config.set("enchantItemName", enchantItemName);
            saveRequired = true;
        } else {
            enchantItemName = config.getString("enchantItemName");
        }

        if (!config.contains("enchantItemLore")) {
            config.set("enchantItemLore", enchantItemLore);
            saveRequired = true;
        } else {
            enchantItemLore = config.getStringList("enchantItemLore");
        }

        if (!config.contains("mobDefenseLore")) {
            config.set("mobDefenseLore", mobDefenseLore);
            saveRequired = true;
        } else {
            mobDefenseLore = config.getString("mobDefenseLore");
        }

        if (!config.contains("pointsLostMessage")) {
            config.set("pointsLostMessage", pointsLostMessage);
            saveRequired = true;
        } else {
            pointsLostMessage = config.getString("pointsLostMessage");
        }

        if (!config.contains("pointsGainedMessage")) {
            config.set("pointsGainedMessage", pointsGainedMessage);
            saveRequired = true;
        } else {
            pointsGainedMessage = config.getString("pointsGainedMessage");
        }

        if (!config.contains("eloMultiplierMessage")) {
            config.set("eloMultiplierMessage", eloMultiplierMessage);
            saveRequired = true;
        } else {
            eloMultiplierMessage = config.getString("eloMultiplierMessage");
        }

        if (!config.contains("eloDifferenceTooBig")) {
            config.set("eloDifferenceTooBig", eloDifferenceTooBig);
            saveRequired = true;
        } else {
            eloDifferenceTooBig = config.getString("eloDifferenceTooBig");
        }

        if (!config.contains("moneyAddedMessage")) {
            config.set("moneyAddedMessage", moneyAddedMessage);
            saveRequired = true;
        } else {
            moneyAddedMessage = config.getString("moneyAddedMessage");
        }

        if (!config.contains("antywebMessage")) {
            config.set("antywebMessage", antywebMessage);
            saveRequired = true;
        } else {
            antywebMessage = config.getString("antywebMessage");
        }

        if (!config.contains("zephyrErrorMessage")) {
            config.set("zephyrErrorMessage", zephyrErrorMessage);
            saveRequired = true;
        } else {
            zephyrErrorMessage = config.getString("zephyrErrorMessage");
        }

        if (!config.contains("noEloZoneMessage")) {
            config.set("noEloZoneMessage", noEloZoneMessage);
            saveRequired = true;
        } else {
            noEloZoneMessage = config.getString("noEloZoneMessage");
        }

        if (!config.contains("noPermissionMessage")) {
            config.set("noPermissionMessage", noPermissionMessage);
            saveRequired = true;
        } else {
            noPermissionMessage = config.getString("noPermissionMessage");
        }

        if (!config.contains("remainingTimeMessage")) {
            config.set("remainingTimeMessage", remainingTimeMessage);
            saveRequired = true;
        } else {
            remainingTimeMessage = config.getString("remainingTimeMessage");
        }

        if (!config.contains("dailyTranslation")) {
            config.set("dailyTranslation", dailyTranslation);
            saveRequired = true;
        } else {
            dailyTranslation = config.getString("dailyTranslation");
        }

        if (!config.contains("weeklyTranslation")) {
            config.set("weeklyTranslation", weeklyTranslation);
            saveRequired = true;
        } else {
            weeklyTranslation = config.getString("weeklyTranslation");
        }

        if (!config.contains("monthlyTranslation")) {
            config.set("monthlyTranslation", monthlyTranslation);
            saveRequired = true;
        } else {
            monthlyTranslation = config.getString("monthlyTranslation");
        }

        if (!config.contains("eventTranslation")) {
            config.set("eventTranslation", eventTranslation);
            saveRequired = true;
        } else {
            eventTranslation = config.getString("eventTranslation");
        }

        if (!config.contains("rankingRewardMessage")) {
            config.set("rankingRewardMessage", rankingRewardMessage);
            saveRequired = true;
        } else {
            rankingRewardMessage = config.getString("rankingRewardMessage");
        }

        if (!config.contains("rewardForLore")) {
            config.set("rewardForLore", rewardForLore);
            saveRequired = true;
        } else {
            rewardForLore = config.getString("rewardForLore");
        }

        if (!config.contains("chatNotifierMessagesList")) {
            config.set("chatNotifierMessagesList", chatNotifierMessagesList);
            saveRequired = true;
        } else {
            chatNotifierMessagesList = config.getStringList("chatNotifierMessagesList");
        }

        if (!config.contains("prefix")) {
            config.set("prefix", prefix);
            saveRequired = true;
        } else {
            prefix = config.getString("prefix");
        }
        if (saveRequired) {
            try {
                config.save(langFile);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "lang.yml file updated with missing values", transactionID);
            } catch (IOException e) {
                pluginLogger.log(PluginLogger.LogLevel.ERROR, "Error saving lang.yml file: " + e.getMessage(), transactionID);
            }
        }

        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "lang.yml file loaded successfully!, loaded values: currentBonusString: " + currentBonusString + ", newAvgBonusString: " + newAvgBonusString + ", mobDamageLore: " + mobDamageLore + ", averageDamageLore: " + averageDamageLore + ", enchantItemName: " + enchantItemName + ", enchantItemLore: " + enchantItemLore + ", mobDefenseLore: " + mobDefenseLore, transactionID);
    }
}