package betterbox.mine.game.betterelo;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

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
        int maxDistance;
        String location;
        String mobName;
        int cooldown;
        int mobCount;
        int spawnedMobCount; // Counter for spawned mobs
        int maxMobs;
        String passengerMobName;  // Nazwa CustomMob jako pasażer

        SpawnerData(String spawnerName, String location, String mobName, int cooldown, int mobCount, int maxMobs, int maxDistance) {
            this.spawnerName = spawnerName;
            this.location = location;
            this.mobName = mobName;
            this.cooldown = cooldown;
            this.mobCount = mobCount;
            this.maxMobs = maxMobs;
            this.maxDistance = maxDistance;
            this.spawnedMobCount = 0; // Initialize the spawned mob counter to 0
        }
        SpawnerData(String spawnerName, String location, String mobName, String passengerMobName, int cooldown, int mobCount, int maxMobs, int maxDistance) {
            this.spawnerName = spawnerName;
            this.location = location;
            this.mobName = mobName;
            this.passengerMobName = passengerMobName;  // Inicjalizacja z nazwą CustomMob jako pasażer
            this.cooldown = cooldown;
            this.mobCount = mobCount;
            this.maxMobs = maxMobs;
            this.maxDistance = maxDistance;
            this.spawnedMobCount = 0;
        }
        public int getSpawnedMobCount() {
            return this.spawnedMobCount;
        }
        public int getMaxMobs() {
            return this.maxMobs;
        }
        public int getMobsPerSpawn() {
            return this.mobCount;
        }
        public int getCooldown() {
            return this.cooldown;
        }
        public int getMaxDistance() {return this.maxDistance;}
    }
    public class DropItem {
        private double dropChance;
        private ItemStack itemStack;
        private boolean avgDmgBonus;
        private int maxDamage;

        public DropItem(double dropChance, ItemStack itemStack, boolean avgDmgBonus) {
            this.dropChance = dropChance;
            this.itemStack = itemStack;
            this.avgDmgBonus = avgDmgBonus;
        }

        public DropItem(double dropChance, ItemStack itemStack, boolean avgDmgBonus, int maxDamage) {
            this.dropChance = dropChance;
            this.maxDamage = maxDamage;
            this.itemStack = itemStack;
            this.avgDmgBonus = avgDmgBonus;
        }
        public double getDropChance() {
            return dropChance;
        }

        public void setDropChance(double dropChance) {
            this.dropChance = dropChance;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public void setItemStack(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        public boolean isAvgDmgBonus() {
            return avgDmgBonus;
        }

        public int getMaxDamage() {
            return maxDamage;
        }

        public boolean hasmaxDamage() {
            return maxDamage>0;
        }

        public void setAvgDmgBonus(boolean avgDmgBonus) {
            this.avgDmgBonus = avgDmgBonus;
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
    private void CreateExampleSpawnersFile(File spawnersFile){
        try (InputStream in = plugin.getResource("customMobs/spawners.yml")) {
            if (in == null) {
                plugin.getLogger().severe("Resource 'customDropTables/spawners.yml not found.");
                return;
            }
            Files.copy(in, spawnersFile.toPath());
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save spawners.yml to " + spawnersFile + ": " + e.getMessage());
        }
    }
    private void CreateSpawnersFile(String folderPath) {
        try {
            String spawnersFileName = "spawners.yml";
            spawnersFile = new File(folderPath + File.separator + "customMobs", spawnersFileName);
            if (!spawnersFile.exists()) {
                CreateExampleSpawnersFile(spawnersFile);
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
        pluginLogger.log(PluginLogger.LogLevel.INFO, "CustomMobsFileManager.loadSpawners ");
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
                //default 20 blocks
                int maxDistance = 20;
                if(spawnerSection.contains("maxDistance")) {
                    maxDistance = spawnerSection.getInt("maxDistance");
                }

                String passengerMobName = spawnerSection.getString("passengerMobName", null);

                // Zapisywanie danych spawnera do struktury w pamięci
                spawnersData.put(key, new SpawnerData(key, location, mobName, passengerMobName, cooldown, mobCount, maxMobs, maxDistance));
                pluginLogger.log(PluginLogger.LogLevel.INFO, "Spawner " + key + " loaded with passenger " + passengerMobName);
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
    public int getMaxDistance(String spawnerName){
        if (spawnersData.containsKey(spawnerName)) {
            // Retrieve the SpawnerData object corresponding to the spawnerName
            SpawnerData spawnerData = spawnersData.get(spawnerName);
            // Return the cooldown value
            //pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "Spawner '" + spawnerName + "' spawnerData.maxDistance: "+spawnerData.maxDistance);
            return spawnerData.maxDistance;
        } else {
            // If the spawnerName is not found, log an error and return a default value or throw an exception
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "getMaxDistance Spawner '" + spawnerName + "' not found.");
            return 20; // or any other default value that indicates an error
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
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "getSpawnerMobName Spawner '" + spawnerName + "' not found.");
            return null; // or any other default value that indicates an error
        }
    }
    public String getSpawnerLocation(String spawnerName){
        if (spawnersData.containsKey(spawnerName)) {
            // Retrieve the SpawnerData object corresponding to the spawnerName
            SpawnerData spawnerData = spawnersData.get(spawnerName);
            // Return the cooldown value
            return spawnerData.location;
        } else {
            // If the spawnerName is not found, log an error and return a default value or throw an exception
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "getSpawnerLocation Spawner '" + spawnerName + "' not found.");
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
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "getSpawnerCooldown Spawner '" + spawnerName + "' not found.");
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
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "getSpawnerMaxMobs Spawner '" + spawnerName + "' not found.");
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

    public CustomMobs.CustomMob loadCustomMob(JavaPlugin plugin, FileRewardManager dropFileManager, File mobFile, String transactionID) {
        //String transactionID = UUID.randomUUID().toString();
        //File customMobsFolder = new File(plugin.getDataFolder(), "customMobs");
        //File mobFile = new File(customMobsFolder, mobName + ".yml");

        if (!mobFile.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Mob file '" + mobFile.toString() + "' not found.",transactionID);
            //plugin.getLogger().warning("Plik dla mobka " + mobFile.toString() + " nie istnieje!");
            return null;
        }

        try{
            YamlConfiguration mobData = YamlConfiguration.loadConfiguration(mobFile);
            String entityTypeString = mobData.getString("type");
            ItemStack helmet=null;
            ItemStack chestplate=null;
            ItemStack leggings=null;
            ItemStack boots=null;
            ItemStack weapon=null;
            pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobsFileManager.loadCustomMob entityTypeString: "+entityTypeString,transactionID);
            if (entityTypeString.equals("SKELETON")||entityTypeString.equals("ZOMBIE")|| entityTypeString.equals("STRAY")|| entityTypeString.equals("WITHER_SKELETON") || entityTypeString.equals("HUSK")) {// Wczytanie wyposażenia z pliku
                pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobsFileManager.loadCustomMob mob is ZOMBIE or SKELETON",transactionID);
                 helmet = loadItemStack(mobData, "equipment.helmet");
                 chestplate = loadItemStack(mobData, "equipment.chestplate");
                 leggings = loadItemStack(mobData, "equipment.leggings");
                 boots = loadItemStack(mobData, "equipment.boots");
                 weapon = loadItemStack(mobData, "equipment.weapon");
            }
            // Wczytanie pozostałych danych
            double armor = 1;
            int hp = mobData.getInt("hp");
            double speed = mobData.getDouble("speed");
            double attackDamage = mobData.getDouble("attackDamage");
            int attackSpeed = 1;
            int regenSeconds= 5;
            double regenPercent = 5, knockbackResistance=0, eloPoints=0, eloMultiplier=0;
            int defense = 0;
            String passengerMobName=null;

            if(mobData.contains("attackSpeed")){
                attackSpeed = mobData.getInt("attackSpeed");
                pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobsFileManager.loadCustomMob loaded AttackSpeed:" + attackSpeed,transactionID);
            }
            if(mobData.contains("defense")){
                defense = mobData.getInt("defense");
                pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobsFileManager.loadCustomMob loaded defense:" + defense,transactionID);
            }
            if(mobData.contains("armor")){
                armor = mobData.getDouble("armor");
                pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobsFileManager.loadCustomMob loaded armor:" + armor,transactionID);
            }
            if(mobData.contains("passengerMobName")){
                passengerMobName = mobData.getString("passengerMobName");
                pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobsFileManager.loadCustomMob loaded passengerMobName:" + passengerMobName,transactionID);
            }
            if(mobData.contains("regenSeconds")){
                regenSeconds = mobData.getInt("regenSeconds");
                pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobsFileManager.loadCustomMob loaded regenSeconds:" + regenSeconds,transactionID);
            }
            if(mobData.contains("regenPercent")){
                regenPercent = mobData.getInt("regenPercent");
                pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobsFileManager.loadCustomMob loaded regenPercent:" + regenPercent,transactionID);
            }
            if(mobData.contains("knockbackResistance")){
                knockbackResistance = mobData.getDouble("knockbackResistance");
                pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobsFileManager.loadCustomMob loaded knockbackResistance:" + knockbackResistance,transactionID);
            }
            if(mobData.contains("eloPoints")){
                eloPoints = mobData.getDouble("eloPoints");
                pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobsFileManager.loadCustomMob loaded eloPoints:" + eloPoints,transactionID);
            }
            if(mobData.contains("eloMultiplier")){
                eloMultiplier = mobData.getDouble("eloMultiplier");
                pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobsFileManager.loadCustomMob loaded eloMultiplier:" + eloMultiplier,transactionID);
            }

            String mobName = mobData.getString("mobName");
            String dropTableName = mobData.getString("dropTable");

            pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobsFileManager.loadCustomMob armor:" + armor + ", hp: " + hp + ", speed: " + speed + ", attackDamage: " + attackDamage + ", type: " + entityTypeString+", dropTablename: "+dropTableName+", passengerMobName: "+passengerMobName+", regenSeconds: "+regenSeconds+", regenPercent: "+regenPercent+", knockbackResistance: "+knockbackResistance+", eloPoints: "+eloPoints+", eloMultiplier: "+eloMultiplier,transactionID);
            EntityType entityType = EntityType.valueOf(entityTypeString);

            // Wczytanie niestandardowych metadanych i ustawienie spawnerName
            Map<String, Object> customMetadata = (Map<String, Object>) mobData.getConfigurationSection("customMetadata").getValues(false);
            //customMetadata.put("SpawnerName", spawnerName); // Dopisanie nazwy spawnera

            // Utworzenie instancji CustomMob
            // Zakładamy, że LivingEntity jest nullem, ponieważ tworzymy moba bez konkretnej encji w świecie
            CustomMobs.CustomMob customMob=null;
            if (entityTypeString.equals("SKELETON")||entityTypeString.equals("ZOMBIE")|| entityTypeString.equals("STRAY")|| entityTypeString.equals("WITHER_SKELETON")|| entityTypeString.equals("HUSK")){
                pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobsFileManager.loadCustomMob mob is ZOMBIE or SKELETON or STRAY",transactionID);
                customMob = new CustomMobs.CustomMob(plugin, this, mobName, entityType, helmet, chestplate, leggings, boots,weapon, armor, hp, speed, attackDamage,attackSpeed, customMetadata, dropTableName,  defense,null, regenSeconds,regenPercent,knockbackResistance, eloPoints, eloMultiplier);
                if(passengerMobName!=null) {
                    pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobsFileManager.loadCustomMob passengerMobName: " + passengerMobName,transactionID);
                    customMob = new CustomMobs.CustomMob(plugin, this, mobName, entityType, helmet, chestplate, leggings, boots,weapon, armor, hp, speed, attackDamage,attackSpeed, customMetadata, dropTableName,  defense, passengerMobName, regenSeconds,regenPercent,knockbackResistance, eloPoints, eloMultiplier);
                }
                }else if(passengerMobName!=null){
                pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobsFileManager.loadCustomMob passengerMobName: "+passengerMobName,transactionID);
                customMob = new CustomMobs.CustomMob(plugin, this, mobName, entityType, armor, hp, speed, attackDamage,attackSpeed, customMetadata, dropTableName,  defense,passengerMobName, regenSeconds,regenPercent,knockbackResistance, eloPoints, eloMultiplier);
            }
            else{
                pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobsFileManager.loadCustomMob normal mob",transactionID);
                customMob = new CustomMobs.CustomMob(plugin, this, mobName, entityType, armor, hp, speed, attackDamage,attackSpeed, customMetadata, dropTableName,  defense, regenSeconds,regenPercent,knockbackResistance, eloPoints, eloMultiplier);

            }

            pluginLogger.log(PluginLogger.LogLevel.DROP,"CustomMobsFileManager.loadCustomMob customMob.dropTablename: "+customMob.dropTableName,transactionID);

            return customMob;
        }catch (Exception e){
            pluginLogger.log(PluginLogger.LogLevel.ERROR,"CustomMobsFileManager.loadCustomMob exception: " + e.getMessage(),transactionID);
        }
        return null;

    }

    private ItemStack loadItemStack(YamlConfiguration mobData, String path) {
        String transactionID = UUID.randomUUID().toString();
        if (!mobData.contains(path + ".type")) return null; // Zabezpieczenie przed brakiem danych

        Material material = Material.valueOf(mobData.getString(path + ".type"));
        ItemStack itemStack = new ItemStack(material);

        // Wczytanie zaklęć
        if (mobData.contains(path + ".enchants")) {
            ConfigurationSection enchantsSection = mobData.getConfigurationSection(path + ".enchants");
            for (String key : enchantsSection.getKeys(false)) {
                try {
                Enchantment enchantment = Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft(key.toLowerCase()));
                if (enchantment != null) {

                        itemStack.addEnchantment(enchantment, enchantsSection.getInt(key));

                }
                }catch (Exception e){
                    pluginLogger.log(PluginLogger.LogLevel.ERROR, "Invalid enchant "+key.toLowerCase()+" in "+mobData.getString("mobName"),transactionID);
                }
            }
        }
        try {// Ustawienie przedmiotu jako niezniszczalnego
            if (mobData.contains(path + ".unbreakable") && mobData.getBoolean(path + ".unbreakable")) {
                ItemMeta meta = itemStack.getItemMeta();
                meta.setUnbreakable(true);
                itemStack.setItemMeta(meta);
            }
        }catch (Exception e){
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "cannot read unbreakable from file. Exception: "+e.getMessage(),transactionID);
        }

        // Wczytanie atrybutów armor i damage
        try {
            ItemMeta meta = itemStack.getItemMeta();
            if (mobData.contains(path + ".attributes.damage")) {
                meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(), "generic.attackDamage", mobData.getDouble(path + ".attributes.damage"), AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
            }
            if (mobData.contains(path + ".attributes.armor")) {
                meta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(), "generic.armor", mobData.getDouble(path + ".attributes.armor"), AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.CHEST));
            }
            itemStack.setItemMeta(meta);
        } catch (Exception e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Error reading attributes from file. Exception: " + e.getMessage(),transactionID);
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
                    if (file.isFile() && file.getName().endsWith(".yml") &&!file.getName().equalsIgnoreCase("customMobs/spawners.yml")) {
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

        YamlConfiguration dropTableConfig = YamlConfiguration.loadConfiguration(dropTableFile);

        dropTableConfig.getKeys(false).forEach(key -> {
            String itemPath = dropTableConfig.getString(key + ".itemPath");
            double dropChance = dropTableConfig.getDouble(key + ".dropChance");
            boolean AvgDmgBonus = dropTableConfig.getBoolean(key + ".avgDmgBonus");
            pluginLogger.log(PluginLogger.LogLevel.DROP, "CustomMobsFileManager.loadCustomDrops itemPath: " + itemPath+", dropChance: "+dropChance+", avgDmgBonus: "+AvgDmgBonus);
            File itemFile = new File(plugin.getDataFolder(), itemPath);
            if (itemFile.exists()) {
                try {
                    FileConfiguration itemConfig = YamlConfiguration.loadConfiguration(itemFile);
                    ItemStack itemStack = itemConfig.getItemStack("item");
                    if (itemStack != null) {
                        if(AvgDmgBonus){
                            ItemMeta meta = itemStack.getItemMeta();
                            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                            //List<String> lore = new ArrayList<>();
                            assert lore != null;
                            lore.add("AvgDmgBonus");
                            meta.setLore(lore);
                            itemStack.setItemMeta(meta);
                            pluginLogger.log(PluginLogger.LogLevel.DROP, "CustomMobsFileManager.loadCustomDrops added  AvgDmgBonus lore ");
                        }
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

    public List<DropItem> loadCustomDropsv2(String dropTableName) {
        String transactionID = UUID.randomUUID().toString();
        pluginLogger.log(PluginLogger.LogLevel.DROP, "CustomMobsFileManager.loadCustomDrops called, dropTableName: " + dropTableName,transactionID);
        List<DropItem> drops = new ArrayList<>();
        File dropTableFile = new File(plugin.getDataFolder() + File.separator + "customDropTables", dropTableName + ".yml");
        if (!dropTableFile.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "loadCustomDrops dropTable " + dropTableName + " does not exist!",transactionID);
            return drops;
        }

        YamlConfiguration dropTableConfig = YamlConfiguration.loadConfiguration(dropTableFile);

        dropTableConfig.getKeys(false).forEach(key -> {
            String itemPath = dropTableConfig.getString(key + ".itemPath");
            double dropChance = dropTableConfig.getDouble(key + ".dropChance");
            int maxDamage = dropTableConfig.getInt(key + ".maxDamage");
            boolean avgDmgBonus = dropTableConfig.getBoolean(key + ".avgDmgBonus");
            pluginLogger.log(PluginLogger.LogLevel.DROP, "CustomMobsFileManager.loadCustomDrops itemPath: " + itemPath+", dropChance: "+dropChance+", avgDmgBonus: "+avgDmgBonus,transactionID);
            File itemFile = new File(plugin.getDataFolder(), itemPath);
            if (itemFile.exists()) {
                try {
                    FileConfiguration itemConfig = YamlConfiguration.loadConfiguration(itemFile);
                    ItemStack itemStack = itemConfig.getItemStack("item");
                    if (itemStack != null) {
                        DropItem dropItem;
                        if(maxDamage>0){
                            dropItem = new DropItem(dropChance, itemStack, avgDmgBonus,maxDamage);
                        }else {
                            dropItem = new DropItem(dropChance, itemStack, avgDmgBonus);
                        }
                        drops.add(dropItem);
                        pluginLogger.log(PluginLogger.LogLevel.DROP, "CustomMobsFileManager.loadCustomDrops item: " + itemStack,transactionID);
                    }
                } catch (Exception e) {
                    pluginLogger.log(PluginLogger.LogLevel.ERROR, "Nie można wczytać przedmiotu z pliku: " + itemPath + ". Błąd: " + e.getMessage(),transactionID);
                }
            }
        });
        pluginLogger.log(PluginLogger.LogLevel.DROP, "CustomMobsFileManager.loadCustomDrops drops: " + drops,transactionID);
        return drops;
    }


}
