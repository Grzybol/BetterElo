package betterbox.mine.game.betterelo;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;

public class CustomMobsFileManager {
    private JavaPlugin plugin;
    private PluginLogger pluginLogger;
    private File spawnersFile;
    public Map<String, SpawnerData> spawnersData = new HashMap<>();


    public CustomMobsFileManager(String folderPath, JavaPlugin plugin, PluginLogger pluginLogger) {

        this.plugin = plugin;
        this.pluginLogger = pluginLogger;
        CreateCustomMobsFolder(folderPath);
        CreateSpawnersFile(folderPath);


    }
    // Klasa wewnętrzna do przechowywania danych spawnera
    static class SpawnerData {
        String spawnerName;
        String location;
        String mobName;
        int cooldown;
        int mobCount;
        int spawnedMobCount; // Counter for spawned mobs
        int maxMobs;

        SpawnerData(String spawnerName, String location, String mobName, int cooldown, int mobCount, int maxMobs) {
            this.spawnerName = spawnerName;
            this.location = location;
            this.mobName = mobName;
            this.cooldown = cooldown;
            this.mobCount = mobCount;
            this.maxMobs = maxMobs;
            this.spawnedMobCount = 0; // Initialize the spawned mob counter to 0
        }
    }

    private void CreateCustomMobsFolder(String folderPath) {
        try {
            File customMobsFolder = new File(folderPath, "customMobs");
            if (!customMobsFolder.exists()) {
                customMobsFolder.mkdirs();
                pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobsFileManager.CreateCustomMobsFolder folder created.");
            } else {
                pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobsFileManager.CreateCustomMobsFolder folder already exists.");
            }
        } catch (Exception e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "CustomMobsFileManager.CreateCustomMobsFolder exception: " + e.getMessage());
        }
    }

    private void CreateSpawnersFile(String folderPath) {
        try {
            String spawnersFileName = "spawners.yml";
            spawnersFile = new File(folderPath + File.separator + "customMobs", spawnersFileName);
            if (!spawnersFile.exists()) {
                pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobsFileManager.CreateSpawnersFile creating new file.");
                spawnersFile.createNewFile();

                // Tworzymy domyślną strukturę pliku
                FileConfiguration config = YamlConfiguration.loadConfiguration(spawnersFile);
                config.createSection("spawners");
                config.set("spawners.exampleSpawner.location", "x y z");
                config.set("spawners.exampleSpawner.mobName", "exampleMobName");
                config.set("spawners.exampleSpawner.cooldown", "exampleCooldown");
                config.set("spawners.exampleSpawner.mobsPerSpawn", "mobsPerSpawn");
                config.set("spawners.exampleSpawner.mobsPerSpawn", "maxMobs");
                // Zapisujemy zmiany do pliku
                config.save(spawnersFile);
            } else {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "CustomMobsFileManager.CreateSpawnersFile file already exists.");
            }
        } catch (Exception e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "CustomMobsFileManager.CreateSpawnersFile exception: " + e.getMessage());
        }
    }

    public void loadSpawners() {
        if (spawnersFile == null) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Spawners file is not initialized.");
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(spawnersFile);
        ConfigurationSection spawnersSection = config.getConfigurationSection("spawners");
        if (spawnersSection == null) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "No spawners defined in spawners.yml.");
            return;
        }

        for (String key : spawnersSection.getKeys(false)) {
            ConfigurationSection spawnerSection = spawnersSection.getConfigurationSection(key);
            if (spawnerSection != null) {
                String location = spawnerSection.getString("location");
                String mobName = spawnerSection.getString("mobName");
                int cooldown = spawnerSection.getInt("cooldown");
                int mobCount = spawnerSection.getInt("mobsPerSpawn");
                int maxMobs = spawnerSection.getInt("maxMobs");
                // Zapisywanie danych spawnera do struktury w pamięci
                spawnersData.put(key, new SpawnerData(key,location, mobName, cooldown,mobCount, maxMobs));
            }
        }
        pluginLogger.log(PluginLogger.LogLevel.INFO, "Loaded spawners data from file.");
    }

    public void saveSpawner(Location location, String spawnerName, String mobName, int cooldown, int mobCount, int maxMobs) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(spawnersFile);

        // Zapisywanie danych spawnera
        String path = "spawners." + spawnerName;
        config.set(path + ".location", location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
        config.set(path + ".mobName", mobName);
        config.set(path + ".cooldown", cooldown);
        config.set(path + ".mobsPerSpawn", mobCount);
        config.set(path + ".maxMobs", maxMobs);
        try {
            config.save(spawnersFile);
            pluginLogger.log(PluginLogger.LogLevel.INFO, "Spawner " + spawnerName + " saved to file.");
        } catch (Exception e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Could not save spawner " + spawnerName + " to file: " + e.getMessage());
        }

    }
    public String getSpawnerMobName(String spawnerName){
        if (spawnersData.containsKey(spawnerName)) {
            // Retrieve the SpawnerData object corresponding to the spawnerName
            SpawnerData spawnerData = spawnersData.get(spawnerName);
            // Return the cooldown value
            return spawnerData.mobName;
        } else {
            // If the spawnerName is not found, log an error and return a default value or throw an exception
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Spawner '" + spawnerName + "' not found.");
            return null; // or any other default value that indicates an error
        }
    }
    public int getSpawnerCooldown(String spawnerName) {
        // Check if the spawnerName exists in the spawnersData map
        if (spawnersData.containsKey(spawnerName)) {
            // Retrieve the SpawnerData object corresponding to the spawnerName
            SpawnerData spawnerData = spawnersData.get(spawnerName);
            // Return the cooldown value
            return spawnerData.cooldown;
        } else {
            // If the spawnerName is not found, log an error and return a default value or throw an exception
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Spawner '" + spawnerName + "' not found.");
            return -1; // or any other default value that indicates an error
        }
    }
    public int getSpawnerMaxMobs(String spawnerName){
        if (spawnersData.containsKey(spawnerName)) {
            // Retrieve the SpawnerData object corresponding to the spawnerName
            SpawnerData spawnerData = spawnersData.get(spawnerName);
            // Return the cooldown value
            return spawnerData.maxMobs;
        } else {
            // If the spawnerName is not found, log an error and return a default value or throw an exception
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Spawner '" + spawnerName + "' not found.");
            return -1; // or any other default value that indicates an error
        }
    }
    public void saveItemStackData(YamlConfiguration mobData, String path, ItemStack itemStack) {
        mobData.set(path + ".type", itemStack.getType().toString());
        if (!itemStack.getEnchantments().isEmpty()) {
            for (Map.Entry<Enchantment, Integer> enchant : itemStack.getEnchantments().entrySet()) {
                mobData.set(path + ".enchants." + enchant.getKey().getKey().getKey(), enchant.getValue());
            }
        }
    }


    /*
    public CustomMobs.CustomMob loadCustomMob(JavaPlugin plugin, FileRewardManager dropFileManager, String mobName) {
        File customMobsFolder = new File(plugin.getDataFolder(), "customMobs");
        File mobFile = new File(customMobsFolder, mobName + ".yml");

        if (!mobFile.exists()) {
            plugin.getLogger().warning("Plik dla mobka " + mobName + " nie istnieje!");
            return null;
        }

        YamlConfiguration mobData = YamlConfiguration.loadConfiguration(mobFile);

        // Wczytanie wyposażenia z pliku
        ItemStack helmet = loadItemStack(mobData, "equipment.helmet");
        ItemStack chestplate = loadItemStack(mobData, "equipment.chestplate");
        ItemStack leggings = loadItemStack(mobData, "equipment.leggings");
        ItemStack boots = loadItemStack(mobData, "equipment.boots");

        // Wczytanie pozostałych danych
        double armor = mobData.getDouble("armor");
        int hp = mobData.getInt("hp");
        double speed = mobData.getDouble("speed");
        double attackDamage = mobData.getDouble("attackDamage");
        String entityTypeString = mobData.getString("type");
        EntityType entityType = EntityType.valueOf(entityTypeString);

        // Wczytanie niestandardowych metadanych i ustawienie spawnerName
        Map<String, Object> customMetadata = (Map<String, Object>) mobData.getConfigurationSection("customMetadata").getValues(false);
        //customMetadata.put("SpawnerName", spawnerName); // Dopisanie nazwy spawnera

        // Utworzenie instancji CustomMob
        // Zakładamy, że LivingEntity jest nullem, ponieważ tworzymy moba bez konkretnej encji w świecie
        CustomMobs.CustomMob customMob = new CustomMobs.CustomMob(plugin, dropFileManager, mobName, entityType, helmet, chestplate, leggings, boots, armor, hp, speed, attackDamage, customMetadata);

        return customMob;
    }

     */
    public CustomMobs.CustomMob loadCustomMob(JavaPlugin plugin, FileRewardManager dropFileManager, File mobFile) {
        //File customMobsFolder = new File(plugin.getDataFolder(), "customMobs");
        //File mobFile = new File(customMobsFolder, mobName + ".yml");

        if (!mobFile.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Mob file '" + mobFile.toString() + "' not found.");
            //plugin.getLogger().warning("Plik dla mobka " + mobFile.toString() + " nie istnieje!");
            return null;
        }

        try{
            YamlConfiguration mobData = YamlConfiguration.loadConfiguration(mobFile);

            // Wczytanie wyposażenia z pliku
            ItemStack helmet = loadItemStack(mobData, "equipment.helmet");
            ItemStack chestplate = loadItemStack(mobData, "equipment.chestplate");
            ItemStack leggings = loadItemStack(mobData, "equipment.leggings");
            ItemStack boots = loadItemStack(mobData, "equipment.boots");
            ItemStack weapon = loadItemStack(mobData, "equipment.weapon");
            // Wczytanie pozostałych danych
            double armor = mobData.getDouble("armor");
            int hp = mobData.getInt("hp");
            double speed = mobData.getDouble("speed");
            double attackDamage = mobData.getDouble("attackDamage");
            double EKMSchance = 0.0f;
            boolean dropEMKS = false;


            if(mobData.contains("dropEMKS")){
                dropEMKS = mobData.getBoolean("dropEMKS");
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "CustomMobsFileManager.loadCustomMob loaded dropEMKS:" + dropEMKS);
            }
            if(mobData.contains("EMKSchance")){
                EKMSchance = mobData.getDouble("EMKSchance");
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "CustomMobsFileManager.loadCustomMob loaded EKMSchance:" + EKMSchance);
            }
            String entityTypeString = mobData.getString("type");
            String mobName = mobData.getString("mobName");
            String dropTableName = mobData.getString("dropTable");

            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "CustomMobsFileManager.loadCustomMob armor:" + armor + ", hp: " + hp + ", speed: " + speed + ", attackDamage: " + attackDamage + ", type: " + entityTypeString+", dropEMKS: "+dropEMKS+", EKMSchance: "+EKMSchance+", dropTablename: "+dropTableName);
            EntityType entityType = EntityType.valueOf(entityTypeString);

            // Wczytanie niestandardowych metadanych i ustawienie spawnerName
            Map<String, Object> customMetadata = (Map<String, Object>) mobData.getConfigurationSection("customMetadata").getValues(false);
            //customMetadata.put("SpawnerName", spawnerName); // Dopisanie nazwy spawnera

            // Utworzenie instancji CustomMob
            // Zakładamy, że LivingEntity jest nullem, ponieważ tworzymy moba bez konkretnej encji w świecie
            CustomMobs.CustomMob customMob = new CustomMobs.CustomMob(plugin, this, mobName, entityType, helmet, chestplate, leggings, boots,weapon, armor, hp, speed, attackDamage, customMetadata, dropTableName, dropEMKS, EKMSchance);
            pluginLogger.log(PluginLogger.LogLevel.DROP,"CustomMobsFileManager.loadCustomMob customMob.dropTablename: "+customMob.dropTableName);

            return customMob;
        }catch (Exception e){
            pluginLogger.log(PluginLogger.LogLevel.ERROR,"CustomMobsFileManager.loadCustomMob exception: " + e.getMessage());
        }
        return null;

    }

    private ItemStack loadItemStack(YamlConfiguration mobData, String path) {
        if (!mobData.contains(path + ".type")) return null; // Zabezpieczenie przed brakiem danych

        Material material = Material.valueOf(mobData.getString(path + ".type"));
        ItemStack itemStack = new ItemStack(material);

        // Wczytanie zaklęć
        if (mobData.contains(path + ".enchants")) {
            ConfigurationSection enchantsSection = mobData.getConfigurationSection(path + ".enchants");
            for (String key : enchantsSection.getKeys(false)) {
                Enchantment enchantment = Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft(key.toLowerCase()));
                if (enchantment != null) {
                    itemStack.addEnchantment(enchantment, enchantsSection.getInt(key));
                }
            }
        }

        return itemStack;
    }
    public List<File> getCustomMobFiles() {
        List<File> customMobFiles = new ArrayList<>();
        File customMobsFolder = new File(plugin.getDataFolder(), "customMobs");

        if (customMobsFolder.exists() && customMobsFolder.isDirectory()) {
            File[] files = customMobsFolder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".yml") &&!file.getName().equalsIgnoreCase("spawners.yml")) {
                        customMobFiles.add(file);
                    }
                }
            }
        }

        return customMobFiles;
    }
    public HashMap<Double, ItemStack> loadCustomDrops(String dropTableName) {
        pluginLogger.log(PluginLogger.LogLevel.DROP, "CustomMobsFileManager.loadCustomDrops called, dropTableName: " + dropTableName);
        HashMap<Double, ItemStack> drops = new HashMap<>();
        File dropTableFile = new File(plugin.getDataFolder() + File.separator + "customDropTables", dropTableName + ".yml");
        if (!dropTableFile.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "loadCustomDrops dropTable " + dropTableName + " does not exist!");
            return drops;
        }

        FileConfiguration dropTableConfig = YamlConfiguration.loadConfiguration(dropTableFile);

        dropTableConfig.getKeys(false).forEach(key -> {
            String itemPath = dropTableConfig.getString(key + ".itemPath");
            double dropChance = dropTableConfig.getDouble(key + ".dropChance");
            pluginLogger.log(PluginLogger.LogLevel.DROP, "CustomMobsFileManager.loadCustomDrops itemPath: " + itemPath+", dropChance: "+dropChance);
            File itemFile = new File(plugin.getDataFolder(), itemPath);
            if (itemFile.exists()) {
                try {
                    FileConfiguration itemConfig = YamlConfiguration.loadConfiguration(itemFile);
                    ItemStack itemStack = itemConfig.getItemStack("item");
                    if (itemStack != null) {
                        drops.put(dropChance, itemStack);
                        pluginLogger.log(PluginLogger.LogLevel.DROP, "CustomMobsFileManager.loadCustomDrops item: " + itemStack);
                    }
                } catch (Exception e) {
                    pluginLogger.log(PluginLogger.LogLevel.ERROR, "Nie można wczytać przedmiotu z pliku: " + itemPath + ". Błąd: " + e.getMessage());
                }
            }
        });
        pluginLogger.log(PluginLogger.LogLevel.DROP, "CustomMobsFileManager.loadCustomDrops drops: " + drops);
        return drops;
    }

}
