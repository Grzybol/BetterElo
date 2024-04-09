package betterbox.mine.game.betterelo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;

import java.util.UUID;

public class CustomMobs {
    private Map<String, CustomMob> customMobsMap = new HashMap<>();
    private final PluginLogger pluginLogger;
    private final JavaPlugin plugin;
    private final FileRewardManager fileRewardManager;
    private final CustomMobsFileManager fileManager;
    private static final Random random = new Random();
    private BukkitTask spawnerTask;
    private Map<String, Long> spawnerLastSpawnTimes = new HashMap<>(); // Mapa przechowująca czas ostatniego respa mobów z każdego spawnera

    static class CustomMob {
        String mobName, dropTablename;
        EntityType entityType;
        LivingEntity entity;
        ItemStack helmet, chestplate, leggings, boots;
        HashMap< Double,ItemStack> dropTable;
        double armor, speed, attackDamage;
        int hp;
        Map<String, Object> customMetadata; // Nowe pole do przechowywania niestandardowych metadanych
        JavaPlugin plugin;
        FileRewardManager dropFileManager;

        CustomMob(JavaPlugin plugin,FileRewardManager dropFileManager, String mobName, EntityType entityType, ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots, double armor, int hp, double speed, double attackDamage, Map<String, Object> customMetadata) {
            this.plugin = plugin;

            this.mobName = mobName;
            this.entity = entity;
            this.entityType = entityType;
            this.helmet = helmet;
            this.chestplate = chestplate;
            this.leggings = leggings;
            this.boots = boots;
            this.dropFileManager = dropFileManager;
            this.dropTable = dropFileManager.loadCustomDrops(mobName);
            this.armor = armor;
            this.hp = hp;
            this.speed = speed;
            this.attackDamage = attackDamage;
            this.customMetadata = customMetadata;
            setupMob();
        }

        // Metoda do stworzenia i ustawienia encji moba
        public void spawnMob(Location location) {
            if (this.entityType == null) {
                plugin.getLogger().warning("EntityType nie jest ustawiony dla " + mobName);
                return;
            }
            this.entity = (LivingEntity) location.getWorld().spawnEntity(location, entityType);
            setupMob(); // Teraz wywołujemy setupMob() po stworzeniu encji
            ((BetterElo) plugin).registerCustomMob(this.entity, this);
        }
        public CustomMob cloneForSpawn(Location spawnLocation) {
            CustomMob newMob = new CustomMob(this.plugin, this.dropFileManager, this.mobName, this.entityType,
                    this.helmet.clone(), this.chestplate.clone(),
                    this.leggings.clone(), this.boots.clone(),
                    this.armor, this.hp, this.speed,
                    this.attackDamage, new HashMap<>(this.customMetadata));
            newMob.spawnMob(spawnLocation);
            return newMob;
        }

        private void setupMob() {
            if (entity == null) {
                plugin.getLogger().warning("Encja nie została stworzona dla " + mobName);
                return;
            }
            //this.dropTable = dropFileManager.loadCustomDrops(mobName);
            // Ustawienie wyposażenia i atrybutów
            entity.getEquipment().setHelmet(helmet);
            entity.getEquipment().setChestplate(chestplate);
            entity.getEquipment().setLeggings(leggings);
            entity.getEquipment().setBoots(boots);

            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(hp);
            entity.setHealth(hp);
            entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
            entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(attackDamage);

            entity.setCustomName(mobName);
            entity.setCustomNameVisible(true);

            // Ustawianie niestandardowych metadanych
            customMetadata.forEach((key, value) -> entity.setMetadata(key, new FixedMetadataValue(plugin, value)));
        }
        public String getMobName() {
            return this.mobName;
        }



    }



    public CustomMobs(PluginLogger pluginLogger, JavaPlugin plugin, CustomMobsFileManager fileManager,FileRewardManager fileRewardManager){
        this.plugin = plugin;
        this.pluginLogger = pluginLogger;
        this.fileManager = fileManager;
        this.fileRewardManager = fileRewardManager;
        loadCustomMobs();
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
        //updateCustomMobName(zombie);

        // Ustawienie niestandardowego dropu
        //zombie.setCustomDropItems(customDrops);
    }
    public void updateCustomMobName(LivingEntity mob) {
        pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobs.updateZombieCustomName called, mob.getName(): "+mob.getName());
        if (!mob.hasMetadata("MobName")) {
            return;
        }
        pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobs.updateZombieCustomName mobName: "+mob.getName()+", currentHP: "+mob.getHealth()+", maxHP: "+mob.getMaxHealth());
        List<MetadataValue> values = mob.getMetadata("MobName");
        MetadataValue value = values.get(0);
        String customName = value.asString();
        Component nameComponent;

            nameComponent = Component.text(customName, NamedTextColor.DARK_RED)
                    .append(Component.text(" HP: " + Math.round(mob.getHealth()) + "/" + Math.round(mob.getMaxHealth()), NamedTextColor.WHITE));


        mob.customName(nameComponent);
        mob.setCustomNameVisible(true);
    }

    public static String dropAverageDamage() {
        // Używamy funkcji wykładniczej do zmniejszenia prawdopodobieństwa wyższych wartości
        double x = -Math.log(random.nextDouble()) / 10.0; // Dostosuj parametr 10.0, aby zmienić rozkład
        int bonus = (int) Math.round(x * 100); // Skalowanie wyniku

        // Ograniczamy wartość bonusu do maksymalnie 60%
        bonus = Math.min(bonus, 60);

        return "§6§lAverage Damage +" + bonus + "%";
    }

    public void spawnCustomMobFromSpawner() {
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
                pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "Spawner " + spawnerName + " is on cooldown. Current spawnedMobCount: "+spawnerData.spawnedMobCount);
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
                    String mobName = fileManager.getSpawnerMobName(spawnerName);
                    //pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobs.spawnZombieFromSpawner "+spawnerName+", maxMobs: "+maxMobs+", remaining slots: "+remainingSlots);
                    if(remainingSlots==0){
                        //pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "0 remaining slots for "+spawnerName);
                        continue;
                    }
                    int mobsToSpawn = Math.min(mobCount, remainingSlots);
                    pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobs.spawnZombieFromSpawner "+spawnerName+", maxMobs: "+maxMobs+", remaining slots: "+remainingSlots+", mobsToSpawn: "+mobsToSpawn);
                    for (int i = 0; i < mobsToSpawn; i++) {
                        spawnCustomMob(location,spawnerName,mobName);
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
                spawnCustomMobFromSpawner();
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
            if(spawnerData.spawnedMobCount>0) {
                spawnerData.spawnedMobCount--; // Zmniejszenie liczby mobów o 1
                pluginLogger.log(PluginLogger.LogLevel.SPAWNERS,"CustomMobs.decreaseMobCount decreased spawnedMobCount for "+spawnerName+". Current spawnedMobCount: "+spawnerData.spawnedMobCount);
            }else{
                pluginLogger.log(PluginLogger.LogLevel.SPAWNERS,"CustomMobs.decreaseMobCount  spawnedMobCount for "+spawnerName+" in lower than 0, setting 0. Current spawnedMobCount: "+spawnerData.spawnedMobCount);
                spawnerData.spawnedMobCount=0;
            }

        } else {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Spawner " + spawnerName + " not found.");
        }
    }
    private Location adjustLocationToAirAbove(Location startLocation) {
        // Zaczynamy od aktualnej lokalizacji
        Location currentLocation = startLocation.clone();
        while (currentLocation.getBlock().getType() != Material.AIR && currentLocation.getY() < currentLocation.getWorld().getMaxHeight()) {
            // Przesuń o jeden blok w górę
            currentLocation.add(0, 1, 0);
        }
        return currentLocation;
    }
    public void saveCustomMobData(CustomMob customMob) {
        pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS,"CustomMobs.saveCustomMobData called");
        File customMobsFolder = new File(plugin.getDataFolder(), "customMobs");
        if (!customMobsFolder.exists()) {
            customMobsFolder.mkdirs();
        }

        File mobFile = new File(customMobsFolder, customMob.getMobName() + ".yml");
        YamlConfiguration mobData = new YamlConfiguration();

        // Zapisz podstawowe dane moba
        mobData.set("type", customMob.entity.getType().toString()); // Dodano typ moba
        mobData.set("mobName", customMob.mobName);
        mobData.set("armor", customMob.armor);
        mobData.set("hp", customMob.hp);
        mobData.set("speed", customMob.speed);
        mobData.set("attackDamage", customMob.attackDamage);
        mobData.set("customMetadata", customMob.customMetadata);

        // Zapisz wyposażenie i zaklęcia
        fileManager.saveItemStackData(mobData, "equipment.helmet", customMob.helmet);
        fileManager.saveItemStackData(mobData, "equipment.chestplate", customMob.chestplate);
        fileManager.saveItemStackData(mobData, "equipment.leggings", customMob.leggings);
        fileManager.saveItemStackData(mobData, "equipment.boots", customMob.boots);

        try {
            mobData.save(mobFile);
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR,"CustomMobs.saveCustomMobData exception "+e.getMessage());
        }
    }

    // Metoda pomocnicza do zapisywania danych ItemStack, w tym zaklęć
    public void loadCustomMobs() {
        pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS,"CustomMobs.loadCustomMobs called.");
        // Wczytaj customowe moby i przechowaj je w pamięci
        // Dla każdego pliku moba w folderze customMobs
        for (File mobFile : fileManager.getCustomMobFiles()) {
            if (!mobFile.getName().equals("spawners.yml"))
            {
                try {
                    CustomMob customMob = fileManager.loadCustomMob(plugin, fileRewardManager, mobFile);
                    if (customMob != null) {
                        customMobsMap.put(customMob.getMobName(), customMob);
                    }
                } catch (Exception e) {
                    pluginLogger.log(PluginLogger.LogLevel.ERROR, "CustomMobs.loadCustomMobs exception: " + e.getMessage());
                }
            }
            pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobs.loadCustomMobs spawners.yml file detected, skipping");
        }
    }

    public CustomMob getCustomMob(String mobName) {
        // Zwróć customowego moba na podstawie nazwy
        return customMobsMap.get(mobName);
    }

    public void spawnCustomMob(Location location, String spawnerName, String mobName) {
        pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobs.spawnCustomMob called, mobName: " + mobName+", spawnerName: "+spawnerName+", location: "+location);
        CustomMob templateMob = customMobsMap.get(mobName);
        if (templateMob != null) {
            Location adjustedLocation = adjustLocationToAirAbove(location);
            CustomMob newMob = templateMob.cloneForSpawn(adjustedLocation);
            newMob.customMetadata.put("SpawnerName", spawnerName);
        } else {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "CustomMobs.spawnCustomMob failed, mob not found: " + mobName);
        }
    }


}
