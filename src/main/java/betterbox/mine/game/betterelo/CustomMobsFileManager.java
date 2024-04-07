package betterbox.mine.game.betterelo;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
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

}
