package betterbox.mine.game.betterelo;

import it.unimi.dsi.fastutil.Pair;
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

        public DropItem(double dropChance, ItemStack itemStack, boolean avgDmgBonus) {
            this.dropChance = dropChance;
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
                maxDistance=spawnerSection.getInt("maxDistance");
                // Zapisywanie danych spawnera do struktury w pamięci
                spawnersData.put(key, new SpawnerData(key,location, mobName, cooldown,mobCount, maxMobs,maxDistance));
                pluginLogger.log(PluginLogger.LogLevel.INFO, "Spawner "+key+" with mobName: "+mobName+", location: ("+location+"), cooldown: "+cooldown+", mobsPerSpawn: "+mobCount+", maxMobs: "+maxMobs+" loaded!");
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
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Spawner '" + spawnerName + "' not found.");
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
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Spawner '" + spawnerName + "' not found.");
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
            String entityTypeString = mobData.getString("type");
            ItemStack helmet=null;
            ItemStack chestplate=null;
            ItemStack leggings=null;
            ItemStack boots=null;
            ItemStack weapon=null;
            pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobsFileManager.loadCustomMob entityTypeString: "+entityTypeString);
            if (entityTypeString.equals("SKELETON")||entityTypeString.equals("ZOMBIE")) {// Wczytanie wyposażenia z pliku
                pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobsFileManager.loadCustomMob mob is ZOMBIE or SKELETON");
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
            double EKMSchance = 0.0d;
            boolean dropEMKS = false;
            int attackSpeed = 1;
            int defense = 0;


            if(mobData.contains("dropEMKS")){
                dropEMKS = mobData.getBoolean("dropEMKS");
                pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobsFileManager.loadCustomMob loaded dropEMKS:" + dropEMKS);
            }
            if(mobData.contains("EMKSchance")){
                EKMSchance = mobData.getDouble("EMKSchance");
                pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobsFileManager.loadCustomMob loaded EKMSchance:" + EKMSchance);
            }
            if(mobData.contains("attackSpeed")){
                attackSpeed = mobData.getInt("attackSpeed");
                pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobsFileManager.loadCustomMob loaded AttackSpeed:" + attackSpeed);
            }
            if(mobData.contains("defense")){
                defense = mobData.getInt("defense");
                pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobsFileManager.loadCustomMob loaded defense:" + defense);
            }
            if(mobData.contains("armor")){
                armor = mobData.getDouble("armor");
                pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobsFileManager.loadCustomMob loaded armor:" + armor);
            }

            String mobName = mobData.getString("mobName");
            String dropTableName = mobData.getString("dropTable");

            pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobsFileManager.loadCustomMob armor:" + armor + ", hp: " + hp + ", speed: " + speed + ", attackDamage: " + attackDamage + ", type: " + entityTypeString+", dropEMKS: "+dropEMKS+", EKMSchance: "+EKMSchance+", dropTablename: "+dropTableName);
            EntityType entityType = EntityType.valueOf(entityTypeString);

            // Wczytanie niestandardowych metadanych i ustawienie spawnerName
            Map<String, Object> customMetadata = (Map<String, Object>) mobData.getConfigurationSection("customMetadata").getValues(false);
            //customMetadata.put("SpawnerName", spawnerName); // Dopisanie nazwy spawnera

            // Utworzenie instancji CustomMob
            // Zakładamy, że LivingEntity jest nullem, ponieważ tworzymy moba bez konkretnej encji w świecie
            CustomMobs.CustomMob customMob=null;
            if (entityTypeString.equals("SKELETON")||entityTypeString.equals("ZOMBIE")){
                pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobsFileManager.loadCustomMob mob is ZOMBIE or SKELETON");
                customMob = new CustomMobs.CustomMob(plugin, this, mobName, entityType, helmet, chestplate, leggings, boots,weapon, armor, hp, speed, attackDamage,attackSpeed, customMetadata, dropTableName, dropEMKS, EKMSchance, defense);

            }else{
                customMob = new CustomMobs.CustomMob(plugin, this, mobName, entityType, armor, hp, speed, attackDamage,attackSpeed, customMetadata, dropTableName, dropEMKS, EKMSchance, defense);

            }

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
                try {
                Enchantment enchantment = Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft(key.toLowerCase()));
                if (enchantment != null) {

                        itemStack.addEnchantment(enchantment, enchantsSection.getInt(key));

                }
                }catch (Exception e){
                    pluginLogger.log(PluginLogger.LogLevel.ERROR, "Invalid enchant "+key.toLowerCase()+" in "+mobData.getString("mobName"));
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
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "cannot read unbreakable from file. Exception: "+e.getMessage());
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
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Error reading attributes from file. Exception: " + e.getMessage());
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
        pluginLogger.log(PluginLogger.LogLevel.DROP, "CustomMobsFileManager.loadCustomDrops called, dropTableName: " + dropTableName);
        List<DropItem> drops = new ArrayList<>();
        File dropTableFile = new File(plugin.getDataFolder() + File.separator + "customDropTables", dropTableName + ".yml");
        if (!dropTableFile.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "loadCustomDrops dropTable " + dropTableName + " does not exist!");
            return drops;
        }

        YamlConfiguration dropTableConfig = YamlConfiguration.loadConfiguration(dropTableFile);

        dropTableConfig.getKeys(false).forEach(key -> {
            String itemPath = dropTableConfig.getString(key + ".itemPath");
            double dropChance = dropTableConfig.getDouble(key + ".dropChance");
            boolean avgDmgBonus = dropTableConfig.getBoolean(key + ".avgDmgBonus");
            pluginLogger.log(PluginLogger.LogLevel.DROP, "CustomMobsFileManager.loadCustomDrops itemPath: " + itemPath+", dropChance: "+dropChance+", avgDmgBonus: "+avgDmgBonus);
            File itemFile = new File(plugin.getDataFolder(), itemPath);
            if (itemFile.exists()) {
                try {
                    FileConfiguration itemConfig = YamlConfiguration.loadConfiguration(itemFile);
                    ItemStack itemStack = itemConfig.getItemStack("item");
                    if (itemStack != null) {
                        DropItem dropItem = new DropItem(dropChance, itemStack, avgDmgBonus);
                        drops.add(dropItem);
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
