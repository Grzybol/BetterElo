package betterbox.mine.game.betterelo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;

import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;;

public class FileRewardManager {

    private final JavaPlugin plugin;
    private final PluginLogger pluginLogger; // Dodajemy referencję do PluginLogger

    private String rewardType;
    private File dataFolder;

    public FileRewardManager(JavaPlugin plugin, PluginLogger pluginLogger) {
        this.plugin = plugin;
        this.pluginLogger = pluginLogger; // Inicjalizujemy PluginLogger
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"FileRewardManager: Inicjalizacja");
        dataFolder = new File(plugin.getDataFolder(),"data");
    }

    public void setRewardType(String rewardType,String subType) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"FileRewardManager: setRewardType: rewardType: "+rewardType+" subType: "+subType);
        this.rewardType = rewardType + "_" + subType;

    }

    public String getRewardType() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"FileRewardManager: getRewardType: rewardType:"+rewardType);
        return rewardType;
    }

    public List<ItemStack> loadRewards() {

        File rewardsFile = new File(dataFolder, rewardType + ".yml");
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
    public List<ItemStack> loadReRollCost() {
        File rewardsFile = new File(dataFolder, "reroll.yml");
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
    public List<ItemStack> loadRewards(Player player) {
        File rewardsFile = new File(dataFolder, rewardType + ".yml");
        if (!rewardsFile.exists()) {
            return new ArrayList<>(); // return empty list if the file does not exist
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(rewardsFile);
        return (List<ItemStack>) config.getList(player.getName());
    }
    public List<ItemStack> getReward(String rewardType) {
        // Załaduj konfigurację z pliku dla danego typu nagrody
        File rewardFile = new File(dataFolder, rewardType + ".yml");
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"FileRewardManager: getReward: rewardType:" + rewardType);
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
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"FileRewardManager: getReward: materialName:" + materialName);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"FileRewardManager: getReward: material:" + material);
            ItemStack defaultItem = new ItemStack(material, amount);
            ItemMeta meta = defaultItem.getItemMeta();
            List<String> lore = rewardConfig.getStringList("lore");
            if (meta != null && lore != null) {
                meta.setLore(lore);
                defaultItem.setItemMeta(meta);
            }
            rewards.add(defaultItem);
        }
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"FileRewardManager: getReward: items:" + rewards);
        return rewards;
    }
    public void saveRewards(String fileName, List<ItemStack> rewards) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "FileRewardManager.saveRewards called. fileName: "+fileName);
        File rewardsFile = new File(dataFolder, fileName + ".yml");
        if (!rewardsFile.getParentFile().exists()) {
            rewardsFile.getParentFile().mkdirs(); // Tworzy katalog, jeśli nie istnieje
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(rewardsFile);

        ConfigurationSection itemsSection = config.createSection("items");
        int index = 0;
        for (ItemStack item : rewards) {
            itemsSection.set("item" + index, item); // Zapisuje każdy element pod kluczem itemX, gdzie X to indeks elementu
            index++;
        }

        try {
            config.save(rewardsFile); // Zapisuje konfigurację do pliku
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "FileRewardManager: saveRewards: Zapisano nagrody do " + fileName + ".yml");
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "FileRewardManager: saveRewards: Nie udało się zapisać nagród: " + e.getMessage());
        }
    }






}
