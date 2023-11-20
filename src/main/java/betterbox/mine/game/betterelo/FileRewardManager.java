package betterbox.mine.game.betterelo;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;;

public class FileRewardManager {

    private final JavaPlugin plugin;
    private final PluginLogger pluginLogger; // Dodajemy referencję do PluginLogger

    private String rewardType;

    public FileRewardManager(JavaPlugin plugin, PluginLogger pluginLogger) {
        this.plugin = plugin;
        this.pluginLogger = pluginLogger; // Inicjalizujemy PluginLogger
        pluginLogger.log("FileRewardManager: Inicjalizacja");
    }

    public void setRewardType(String rewardType,String subType) {
        pluginLogger.log("FileRewardManager: getRewardType: rewardType: "+rewardType+" subType: "+subType);
        this.rewardType = rewardType + "_" + subType;

    }

    public String getRewardType() {
        pluginLogger.log("FileRewardManager: getRewardType: rewardType:"+rewardType);
        return rewardType;
    }

    public List<ItemStack> loadRewards() {
        File rewardsFile = new File(plugin.getDataFolder(), rewardType + ".yml");
        if (!rewardsFile.exists()) {
            return new ArrayList<>(); // return empty list if the file does not exist
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(rewardsFile);
        List<ItemStack> rewards = new ArrayList<>();
        for (String key : config.getConfigurationSection("items").getKeys(false)) {
            ItemStack item = config.getItemStack("items." + key);
            if (item != null && item.getType() != Material.AIR) {
                rewards.add(item);
            }
        }
        return rewards;
    }
    public List<ItemStack> getReward(String rewardType) {
        // Załaduj konfigurację z pliku dla danego typu nagrody
        File rewardFile = new File(plugin.getDataFolder(), rewardType + ".yml");
        pluginLogger.log("FileRewardManager: getReward: rewardType:" + rewardType);
        FileConfiguration rewardConfig = YamlConfiguration.loadConfiguration(rewardFile);
        // Pobierz dane o nagrodzie z pliku konfiguracyjnego
        List<ItemStack> rewards = new ArrayList<>();
        ConfigurationSection itemsSection = rewardConfig.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ItemStack item = itemsSection.getItemStack(key);
                if (item != null) {
                    rewards.add(item);
                }
            }
        } else {
            String materialName = rewardConfig.getString("material", "DIAMOND");
            Material material = Material.getMaterial(materialName);
            int amount = rewardConfig.getInt("amount", 1);
            pluginLogger.log("FileRewardManager: getReward: materialName:" + materialName);
            pluginLogger.log("FileRewardManager: getReward: material:" + material);
            ItemStack defaultItem = new ItemStack(material, amount);
            ItemMeta meta = defaultItem.getItemMeta();
            List<String> lore = rewardConfig.getStringList("lore");
            if (meta != null && lore != null) {
                meta.setLore(lore);
                defaultItem.setItemMeta(meta);
            }
            rewards.add(defaultItem);
        }
        pluginLogger.log("FileRewardManager: getReward: items:" + rewards);
        return rewards;
    }
}
