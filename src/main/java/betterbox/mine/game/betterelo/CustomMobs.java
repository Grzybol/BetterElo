package betterbox.mine.game.betterelo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

import java.util.UUID;

public class CustomMobs {
    private final PluginLogger pluginLogger;
    private final JavaPlugin plugin;
    private final CustomMobsFileManager fileManager;
    private static final Random random = new Random();
    private BukkitTask spawnerTask;
    private Map<String, Long> spawnerLastSpawnTimes = new HashMap<>(); // Mapa przechowująca czas ostatniego respa mobów z każdego spawnera

    public CustomMobs(PluginLogger pluginLogger, JavaPlugin plugin, CustomMobsFileManager fileManager){
        this.plugin = plugin;
        this.pluginLogger = pluginLogger;
        this.fileManager = fileManager;
    }
    public void spawnModifiedZombie(Player player) {
        pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS,"CustomMobs.spawnModifiedZombie called, player: "+player.getName());
        Location playerLocation = player.getLocation();
        World world = player.getWorld();

        // Tworzenie zombiaka
        Zombie zombie = (Zombie) world.spawnEntity(playerLocation, EntityType.ZOMBIE);
        zombie.setMetadata("CustomZombie", new FixedMetadataValue(plugin, true));
        // Ubieranie zombiaka w zestaw
        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
        ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);

        zombie.getEquipment().setHelmet(helmet);
        zombie.getEquipment().setChestplate(chestplate);
        zombie.getEquipment().setLeggings(leggings);
        zombie.getEquipment().setBoots(boots);

        // Modyfikowanie parametrów zombiaka
        // Przykładowe zmiany - możesz dostosować według własnych preferencji
        zombie.setMaxHealth(1000); // Zwiększenie maksymalnego zdrowia zombiaka
        zombie.setHealth(1000); // Ustawienie aktualnego zdrowia na maksymalne
        zombie.setBaby(false); // Upewnienie się, że zombiak nie jest dzieckiem
        zombie.setCustomName("Modyfikowany Zombiak"); // Ustawienie niestandardowego nazwy
        zombie.setCustomNameVisible(true); // Wyświetlanie niestandardowej nazwy nad zombiakiem

        // Dodanie modyfikatora do siły ataku zombiaka
        double attackDamage = 8.0; // Przykładowa wartość siły ataku
        AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "attack_damage", attackDamage, AttributeModifier.Operation.ADD_NUMBER);
        zombie.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).addModifier(modifier);

        // Modyfikacja dropu zombiaka
        List<ItemStack> customDrops = new ArrayList<>();
        ItemStack customDropItem = new ItemStack(Material.DIAMOND);
        ItemMeta customDropItemMeta = customDropItem.getItemMeta();
        if (customDropItemMeta != null) {
            customDropItemMeta.setDisplayName("Niestandardowy Drop");
            List<String> lore = new ArrayList<>();
            lore.add("To jest niestandardowy drop zombiaka.");
            customDropItemMeta.setLore(lore);
            customDropItem.setItemMeta(customDropItemMeta);
        }
        customDrops.add(customDropItem);
        updateZombieCustomName(zombie);

        // Ustawienie niestandardowego dropu
        //zombie.setCustomDropItems(customDrops);
    }

    public void spawnModifiedZombie(Location location,String spawnerName) {
        pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS,"CustomMobs.spawnModifiedZombie called, location: "+location.toString());
        World world = location.getWorld();
        // Tworzenie zombiaka
        Zombie zombie = (Zombie) world.spawnEntity(location, EntityType.ZOMBIE);

        zombie.setMetadata("CustomZombie", new FixedMetadataValue(plugin, true));
        zombie.setMetadata("SpawnerName", new FixedMetadataValue(plugin, spawnerName)); // Dodanie nazwy spawnera jako metadane
        //zombie.setMetadata("fromSpawner", new FixedMetadataValue(plugin, true)); // Dodanie nazwy spawnera jako metadane
        // Ubieranie zombiaka w zestaw
        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
        ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);

        zombie.getEquipment().setHelmet(helmet);
        zombie.getEquipment().setChestplate(chestplate);
        zombie.getEquipment().setLeggings(leggings);
        zombie.getEquipment().setBoots(boots);

        // Modyfikowanie parametrów zombiaka
        // Przykładowe zmiany - możesz dostosować według własnych preferencji
        zombie.setMaxHealth(1000); // Zwiększenie maksymalnego zdrowia zombiaka
        zombie.setHealth(1000); // Ustawienie aktualnego zdrowia na maksymalne
        zombie.setBaby(false); // Upewnienie się, że zombiak nie jest dzieckiem
        zombie.setCustomName("Modyfikowany Zombiak"); // Ustawienie niestandardowego nazwy
        zombie.setCustomNameVisible(true); // Wyświetlanie niestandardowej nazwy nad zombiakiem

        // Dodanie modyfikatora do siły ataku zombiaka
        double attackDamage = 8.0; // Przykładowa wartość siły ataku
        AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "attack_damage", attackDamage, AttributeModifier.Operation.ADD_NUMBER);
        zombie.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).addModifier(modifier);

        // Modyfikacja dropu zombiaka
        List<ItemStack> customDrops = new ArrayList<>();
        ItemStack customDropItem = new ItemStack(Material.DIAMOND);
        ItemMeta customDropItemMeta = customDropItem.getItemMeta();
        if (customDropItemMeta != null) {
            customDropItemMeta.setDisplayName("Niestandardowy Drop");
            List<String> lore = new ArrayList<>();
            lore.add("To jest niestandardowy drop zombiaka.");
            customDropItemMeta.setLore(lore);
            customDropItem.setItemMeta(customDropItemMeta);
        }
        customDrops.add(customDropItem);
        updateZombieCustomName(zombie);

        // Ustawienie niestandardowego dropu
        //zombie.setCustomDropItems(customDrops);
    }
    public void updateZombieCustomName(Zombie zombie) {
        pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "CustomMobs.updateZombieCustomName called");
        String customName = "Modyfikowany Zombiak";
        zombie.setCustomName(customName + " HP: " + Math.round(zombie.getHealth()) + "/" + Math.round(zombie.getMaxHealth()));
        zombie.setCustomNameVisible(true);
    }
    public static String dropAverageDamage() {
        // Używamy funkcji wykładniczej do zmniejszenia prawdopodobieństwa wyższych wartości
        double x = -Math.log(random.nextDouble()) / 10.0; // Dostosuj parametr 10.0, aby zmienić rozkład
        int bonus = (int) Math.round(x * 100); // Skalowanie wyniku

        // Ograniczamy wartość bonusu do maksymalnie 60%
        bonus = Math.min(bonus, 60);

        return "§6§lAverage Damage +" + bonus + "%";
    }

    public void spawnZombieFromSpawner() {
        Map<String, CustomMobsFileManager.SpawnerData> spawnersData = fileManager.spawnersData;

        // Sprawdzenie, czy istnieją spawnerzy w pliku
        if (spawnersData.isEmpty()) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "No spawners found in spawners.yml.");
            return;
        }

        long currentTime = System.currentTimeMillis();

        for (Map.Entry<String, CustomMobsFileManager.SpawnerData> entry : spawnersData.entrySet()) {
            String spawnerName = entry.getKey();
            CustomMobsFileManager.SpawnerData spawnerData = entry.getValue();
            Location location = getLocationFromString(spawnerData.location);

            // Sprawdzenie cooldownu
            if (!canSpawnMobs(spawnerName, fileManager.getSpawnerCooldown(spawnerName))) {
                pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "Spawner " + spawnerName + " is on cooldown.");
                continue; // Skip spawning if on cooldown
            }

            // Spawnowanie zombiaków na podanej lokalizacji
            if (location != null) {
                World world = location.getWorld();
                if (world != null) {
                    int mobCount = spawnerData.mobCount;
                    // Get the remaining slots for spawning mobs
                    int maxMobs = fileManager.getSpawnerMaxMobs(spawnerName);
                    int remainingSlots = Math.max(0, maxMobs - spawnerData.spawnedMobCount);
                    pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobs.spawnZombieFromSpawner "+spawnerName+", maxMobs: "+maxMobs+", remaining slots: "+remainingSlots);
                    if(remainingSlots==0){
                        //pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "0 remaining slots for "+spawnerName);
                        continue;
                    }
                    int mobsToSpawn = Math.min(mobCount, remainingSlots);
                    for (int i = 0; i < mobsToSpawn; i++) {
                        spawnModifiedZombie(location,spawnerName);
                        // Increment the spawned mob count for this spawner
                        spawnerData.spawnedMobCount++;
                    }

                    // Ustawianie czasu ostatniego respa mobów z tego spawnera
                    spawnerLastSpawnTimes.put(spawnerName, currentTime);
                } else {
                    pluginLogger.log(PluginLogger.LogLevel.ERROR, "Invalid world specified for spawner " + spawnerName);
                }
            } else {
                pluginLogger.log(PluginLogger.LogLevel.ERROR, "Invalid location specified for spawner " + spawnerName);
            }
        }
    }

    private Location getLocationFromString(String locationString) {
        try {
            String[] parts = locationString.split(",");
            if (parts.length == 4) {
                World world = Bukkit.getWorld(parts[0]);
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                double z = Double.parseDouble(parts[3]);
                pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobs.getLocationFromString locationString: "+locationString+", ");
                return new Location(world, x, y, z);
            } else {
                return null;
            }
        } catch (Exception e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Error parsing location string: " + e.getMessage());
            return null;
        }
    }

    public void startSpawnerScheduler() {
        spawnerTask = new BukkitRunnable() {
            @Override
            public void run() {
                spawnZombieFromSpawner();
            }
        }.runTaskTimer(plugin, 0, 100); // Interval converted to ticks (1 second)
    }

    public void stopSpawnerScheduler() {
        if (spawnerTask != null) {
            spawnerTask.cancel();
        }
    }
    private boolean canSpawnMobs(String spawnerName, int cooldown) {
        pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobs.canSpawnMobs " + spawnerName + " cooldown: "+cooldown);
        if (!spawnerLastSpawnTimes.containsKey(spawnerName)) {
            pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobs.canSpawnMobs check passed, spawner not on the list, return true");
            return true; // Gracz jeszcze nie używał fajerwerka
        }
        long lastUsage = spawnerLastSpawnTimes.get(spawnerName);
        long currentTime = System.currentTimeMillis();
        pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobs.canSpawnMobs spawnerName: "+spawnerName+", lastUsage: "+lastUsage+", currentTime: "+currentTime);
        return (currentTime - lastUsage) >= (cooldown*1000L);
    }
    public void decreaseMobCount(String spawnerName) {
        pluginLogger.log(PluginLogger.LogLevel.SPAWNERS,"CustomMobs.decreaseMobCount called spawnerName: "+spawnerName);
        if (fileManager.spawnersData.containsKey(spawnerName)) {
            CustomMobsFileManager.SpawnerData spawnerData = fileManager.spawnersData.get(spawnerName);
            spawnerData.spawnedMobCount--; // Zmniejszenie liczby mobów o 1
            pluginLogger.log(PluginLogger.LogLevel.SPAWNERS,"CustomMobs.decreaseMobCount decreased spawnedMobCount for "+spawnerName+". Current spawnedMobCount: "+spawnerData.spawnedMobCount);
        } else {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Spawner " + spawnerName + " not found.");
        }
    }


}
