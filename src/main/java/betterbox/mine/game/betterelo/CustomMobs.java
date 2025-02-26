package betterbox.mine.game.betterelo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
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
    private Map<ChatColor, String> formattingMap = new HashMap<>();
    private final PluginLogger pluginLogger;
    private final JavaPlugin plugin;
    private final BetterElo betterElo;
    private final FileRewardManager fileRewardManager;
    private final CustomMobsFileManager fileManager;
    private BukkitTask spawnerTask;
    public Map<String, Long> spawnerLastSpawnTimes = new HashMap<>(); // Mapa przechowująca czas ostatniego respa mobów z każdego spawnera
    public Map<UUID,String> spawnedMobsMap = new HashMap<>();

    static class CustomMob {
        String mobName, dropTableName, spawnerName;
        boolean dropEMKS;
        EntityType entityType;
        LivingEntity entity;
        ItemStack helmet, chestplate, leggings, boots, weapon;
        //HashMap< Double,ItemStack> dropTable;
        //List<String> dropTableNames;
        List<Map.Entry<String, Double>> dropTableNames;
        List<CustomMobsFileManager.DropItem> dropTable;
        List<List<CustomMobsFileManager.DropItem>> dropTableLists;
        double armor, speed, attackDamage, EMKSchance, regenPercent, knockbackResistance, eloPoints, eloMultiplier;
        String passengerMobName; // Nowe pole dla nazwy pasażera
        int hp, attackSpeed, defense, regenSeconds;
        Map<String, Object> customMetadata; // Nowe pole do przechowywania niestandardowych metadanych
        JavaPlugin plugin;
        CustomMobsFileManager dropFileManager;

        CustomMob(JavaPlugin plugin, CustomMobsFileManager dropFileManager, String mobName, EntityType entityType, ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots, ItemStack weapon, double armor, int hp, double speed, double attackDamage, int attackSpeed, Map<String, Object> customMetadata, List<Map.Entry<String, Double>> dropTableNames, int defense, String passengerMobName, int regenSeconds,double regenPercent, double knockbackResistance, double eloPoints, double eloMultiplier) {
            this.plugin = plugin;
            this.regenSeconds=regenSeconds;
            this.regenPercent=regenPercent;
            this.passengerMobName=passengerMobName;
            this.mobName = mobName;
            this.entity = entity;
            if (weapon != null) {
                this.weapon = weapon;
            } else {
                //plugin.getLogger().warning(mobName + " does not have weapon set. Setting AIR");
                this.weapon = new ItemStack(Material.AIR);
            }

            this.entityType = entityType;
            if (helmet != null) {
                this.helmet = helmet;
            }
            if (chestplate != null) {
                this.chestplate = chestplate;
            }
            if (leggings != null) {
                this.leggings = leggings;
            }
            if (boots != null) {
                this.boots = boots;
            }
            this.dropFileManager = dropFileManager;
            this.dropTableNames = dropTableNames;
            //this.dropTable = dropFileManager.loadCustomDrops(dropTableName);
            this.dropTableLists = dropFileManager.loadCustomDropsv2(dropTableNames);
            this.armor = armor;
            this.hp = hp;
            this.attackSpeed = attackSpeed;
            this.speed = speed;
            this.defense = defense;
            this.attackDamage = attackDamage;
            this.customMetadata = customMetadata;
            this.knockbackResistance = knockbackResistance;
            this.eloPoints = eloPoints;
            this.eloMultiplier = eloMultiplier;
            //setupMob();
        }

        CustomMob(JavaPlugin plugin, CustomMobsFileManager dropFileManager, String mobName, EntityType entityType, double armor, int hp, double speed, double attackDamage, int attackSpeed, Map<String, Object> customMetadata, List<Map.Entry<String, Double>> dropTableNames, int defense, int regenSeconds,double regenPercent, double knockbackResistance, double eloPoints, double eloMultiplier) {
            this.plugin = plugin;
            this.regenSeconds=regenSeconds;
            this.regenPercent=regenPercent;
            this.mobName = mobName;
            this.entity = entity;
            this.entityType = entityType;
            this.dropFileManager = dropFileManager;
            this.dropTableNames = dropTableNames;
            //this.dropTable = dropFileManager.loadCustomDrops(dropTableName);
            this.dropTableLists = dropFileManager.loadCustomDropsv2(dropTableNames);
            this.armor = armor;
            this.hp = hp;
            this.attackSpeed = attackSpeed;
            this.speed = speed;
            this.defense = defense;
            this.attackDamage = attackDamage;
            this.customMetadata = customMetadata;
            this.knockbackResistance = knockbackResistance;
            this.eloPoints = eloPoints;
            this.eloMultiplier = eloMultiplier;
            //setupMob();
        }
        CustomMob(JavaPlugin plugin, CustomMobsFileManager dropFileManager, String mobName, EntityType entityType, double armor, int hp, double speed, double attackDamage, int attackSpeed, Map<String, Object> customMetadata, List<Map.Entry<String, Double>> dropTableNames, int defense, String passengerMobName,int regenSeconds,double regenPercent, double knockbackResistance, double eloPoints, double eloMultiplier) {
            this.plugin = plugin;
            this.regenSeconds=regenSeconds;
            this.knockbackResistance = knockbackResistance;
            this.regenPercent=regenPercent;
            this.mobName = mobName;
            this.entity = entity;
            this.entityType = entityType;
            this.dropFileManager = dropFileManager;
            this.dropTableNames = dropTableNames;
            //this.dropTable = dropFileManager.loadCustomDrops(dropTableName);
            this.dropTableLists = dropFileManager.loadCustomDropsv2(dropTableNames);
            this.passengerMobName = passengerMobName;
            this.armor = armor;
            this.hp = hp;
            this.attackSpeed = attackSpeed;
            this.speed = speed;
            this.defense = defense;
            this.attackDamage = attackDamage;
            this.customMetadata = customMetadata;
            this.eloPoints = eloPoints;
            this.eloMultiplier = eloMultiplier;
            //setupMob();
        }

        // Metoda do stworzenia i ustawienia encji moba
        public void spawnMob(Location location) {
            if (this.entityType == null) {
                plugin.getLogger().warning("EntityType not set for " + mobName);
                return;
            }
            this.entity = (LivingEntity) location.getWorld().spawnEntity(location, entityType);

            setupMob(); // Teraz wywołujemy setupMob() po stworzeniu encji
            ((BetterElo) plugin).registerCustomMob(this.entity, this);

        }
        public Entity getEntity(){
            return entity;
        }


        public CustomMob cloneForSpawn(Location spawnLocation, String mobType) {

            CustomMob newMob = null;
            if (mobType.equals("SKELETON") || mobType.equals("ZOMBIE") || mobType.equals("STRAY")|| mobType.equals("HUSK")|| mobType.equals("WITHER_SKELETON")) {
                newMob = new CustomMob(this.plugin, this.dropFileManager, this.mobName, this.entityType,
                        this.helmet.clone(), this.chestplate.clone(),
                        this.leggings.clone(), this.boots.clone(), this.weapon.clone(),
                        this.armor, this.hp, this.speed,
                        this.attackDamage, this.attackSpeed, new HashMap<>(this.customMetadata), this.dropTableNames,  this.defense, this.passengerMobName, this.regenSeconds, this.regenPercent, this.knockbackResistance, this.eloPoints, this.eloMultiplier);
                newMob.spawnMob(spawnLocation);
            } else {
                newMob = new CustomMob(this.plugin, this.dropFileManager, this.mobName, this.entityType,
                        this.armor, this.hp, this.speed,
                        this.attackDamage, this.attackSpeed, new HashMap<>(this.customMetadata), this.dropTableNames, this.defense, this.regenSeconds, this.regenPercent, this.knockbackResistance, this.eloPoints, this.eloMultiplier);
                newMob.spawnMob(spawnLocation);
            }
            return newMob;
        }
        public CustomMob cloneForSpawn(Location spawnLocation) {

            CustomMob newMob = null;
            newMob = new CustomMob(this.plugin, this.dropFileManager, this.mobName, this.entityType,
                        this.armor, this.hp, this.speed,
                        this.attackDamage, this.attackSpeed, new HashMap<>(this.customMetadata), this.dropTableNames,  this.defense, this.passengerMobName, this.regenSeconds, this.regenPercent, this.knockbackResistance, this.eloPoints, this.eloMultiplier);
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
            entity.setPersistent(true);
            if (weapon != null) {
                entity.getEquipment().setItemInMainHand(weapon);
            } else {
                //plugin.getLogger().warning(mobName + " does not have weapon set. Setting AIR");
                entity.getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
            }
            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(hp);
            entity.setHealth(hp);
            entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
            entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(attackDamage);
            entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(knockbackResistance); // Ustawienie knockbackResistance
            //plugin.getLogger().info("attackSpeed: "+attackSpeed);
            //entity.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(attackSpeed);

            if (customMetadata.containsKey("MobName")){
                entity.setCustomName(FormatUtil.applyFormatting(customMetadata.get("MobName").toString())+" Elo: "+eloPoints);
            }else{
                entity.setCustomName(mobName+" Elo: "+eloPoints);
            }


            entity.setCustomNameVisible(true);

            // Ustawianie niestandardowych metadanych
            customMetadata.forEach((key, value) -> entity.setMetadata(key, new FixedMetadataValue(plugin, value)));
            entity.setMetadata("armor", new FixedMetadataValue(plugin, armor));
            entity.setMetadata("defense", new FixedMetadataValue(plugin, defense));
        }

        public String getMobName() {
            return this.mobName;
        }



    }
    public enum ChatColor {
        BLACK("&0"),
        DARK_BLUE("&1"),
        DARK_GREEN("&2"),
        DARK_AQUA("&3"),
        DARK_RED("&4"),
        DARK_PURPLE("&5"),
        GOLD("&6"),
        GRAY("&7"),
        DARK_GRAY("&8"),
        BLUE("&9"),
        GREEN("&a"),
        AQUA("&b"),
        RED("&c"),
        LIGHT_PURPLE("&d"),
        YELLOW("&e"),
        WHITE("&f"),
        OBFUSCATED("&k"),
        BOLD("&l"),
        STRIKETHROUGH("&m"),
        UNDERLINE("&n"),
        ITALIC("&o"),
        RESET("&r");

        private final String code;

        ChatColor(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public static String translateAlternateColorCodes(String message) {
            for (ChatColor color : values()) {
                message = message.replace(color.getCode(), color.name());
            }
            return message;
        }
    }
    public static class FormatUtil {
        public static String applyFormatting(String input) {
            return ChatColor.translateAlternateColorCodes(input);
        }
    }

    //Przykład użycia
    /*
    public static void main(String[] args) {
        String input = "&6&ltest";
        String formatted = FormatUtil.applyFormatting(input);
        System.out.println(formatted);  // Wypisze coś w stylu "GOLD BOLD test" jeśli zaimplementujesz odpowiednie zamiany w ChatColor.translateAlternateColorCodes
    }

     */



    public CustomMobs(PluginLogger pluginLogger, JavaPlugin plugin, CustomMobsFileManager fileManager, FileRewardManager fileRewardManager, BetterElo betterElo) {
        this.plugin = plugin;
        this.betterElo = betterElo;
        this.pluginLogger = pluginLogger;
        this.fileManager = fileManager;
        this.fileRewardManager = fileRewardManager;
        loadCustomMobs(UUID.randomUUID().toString());
    }

    public void spawnModifiedZombie(Player player, String mobName, int mobCount,String transactionID) {
        pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobs.spawnModifiedZombie called, player: " + player.getName() + ", mobName: " + mobName + ", mobCount: " + mobCount);
        Location playerLocation = player.getLocation();
        //World world = player.getWorld();
        for (int i = 0; i < mobCount; i++) {
            spawnCustomMob(playerLocation, mobName,transactionID);
        }
    }

    public void updateCustomMobName(LivingEntity mob,double eloPoints, String transactionID) {
        pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobs.updateZombieCustomName called, mob.getName(): " + mob.getName(),transactionID);
        if (!mob.hasMetadata("MobName")) {
            return;
        }
        pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobs.updateZombieCustomName mobName: " + mob.getName() + ", currentHP: " + mob.getHealth() + ", maxHP: " + mob.getMaxHealth(),transactionID);
        List<MetadataValue> values = mob.getMetadata("MobName");
        MetadataValue value = values.get(0);
        String customName = value.asString();
        Component nameComponent;

        nameComponent = Component.text(FormatUtil.applyFormatting(customName))
                //.append(Component.text(" HP: " + Math.round(mob.getHealth()) + "/" + Math.round(mob.getMaxHealth())+" Elo: "+eloPoints, NamedTextColor.WHITE));
                .append(Component.text(" Elo: "+eloPoints+" HP: " + Math.round(mob.getHealth()) + "/" + Math.round(mob.getMaxHealth()), NamedTextColor.WHITE));


        mob.customName(nameComponent);
        mob.setCustomNameVisible(true);
    }



    /*
    public static int dropAverageDamage() {
        // Używamy funkcji wykładniczej do zmniejszenia prawdopodobieństwa wyższych wartości
        double x = -Math.log(random.nextDouble()) / 5; // Zmniejszamy stromość rozkładu
        int bonus = (int) Math.round(x * 70); // Zwiększamy zakres skalowania, ale nadal ograniczamy do 60

        // Ograniczamy wartość bonusu do maksymalnie 60%
        bonus = Math.min(bonus, 60);

        return bonus;
    }

     */

    public void spawnCustomMobFromSpawner() {
        String transactionID = UUID.randomUUID().toString();
        Map<String, CustomMobsFileManager.SpawnerData> spawnersData = fileManager.spawnersData;
        pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobs.spawnCustomMobFromSpawner called. Loaded spawners: " + spawnersData,transactionID);
        // Sprawdzenie, czy istnieją spawnerzy w pliku
        if (spawnersData.isEmpty()) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "No spawners found in spawners.yml.",transactionID);
            return;
        }

        long currentTime = System.currentTimeMillis();

        for (Map.Entry<String, CustomMobsFileManager.SpawnerData> entry : spawnersData.entrySet()) {
            pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobs.spawnZombieFromSpawner checking spawner: " + entry,transactionID);
            String spawnerName = entry.getKey();
            CustomMobsFileManager.SpawnerData spawnerData = entry.getValue();
            Location location = getLocationFromString(spawnerData.location);

            while (location.getBlock().getType() != Material.AIR) {
                location.add(0, 1, 0); // Zwiększ y o 1
                if (location.getBlockY() > location.getWorld().getMaxHeight()) {
                    // Jeśli przekraczamy maksymalną wysokość, przerwij pętlę, aby uniknąć pętli nieskończonej
                    System.out.println("Reached the top of the world without finding an AIR block.");
                    break;
                }
            }
            location.add(0, 1, 0);

            // Sprawdzenie cooldownu
            if (!canSpawnMobs(spawnerName, fileManager.getSpawnerCooldown(spawnerName),transactionID)) {
                pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "Spawner " + spawnerName + " is on cooldown. Current spawnedMobCount: " + spawnerData.spawnedMobCount,transactionID);
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
                    if (remainingSlots == 0) {
                        pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobs.spawnZombieFromSpawner 0 remaining slots for " + spawnerName,transactionID);
                        continue;
                    }

                    int mobsToSpawn = Math.min(mobCount, remainingSlots);
                    int spawnedMobs = 0;
                    pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobs.spawnZombieFromSpawner " + spawnerName + ", maxMobs: " + maxMobs + ", remaining slots: " + remainingSlots + ", mobsToSpawn: " + mobsToSpawn + ", spawnerData.spawnedMobCount: " + spawnerData.spawnedMobCount,transactionID);
                    for (int i = 0; i < mobsToSpawn; i++) {
                        spawnCustomMob(location, spawnerName, mobName,transactionID);
                        spawnerData.spawnedMobCount++;
                        spawnedMobs++;
                    }
                    pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobs.spawnZombieFromSpawner spawnedMobs: " + spawnedMobs,transactionID);
                    // Ustawianie czasu ostatniego respa mobów z tego spawnera
                    spawnerLastSpawnTimes.put(spawnerName, currentTime);
                } else {
                    pluginLogger.log(PluginLogger.LogLevel.ERROR, "Invalid world specified for spawner " + spawnerName,transactionID);
                }
            } else {
                pluginLogger.log(PluginLogger.LogLevel.ERROR, "Invalid location specified for spawner " + spawnerName,transactionID);
            }
        }
    }

    public void spawnerForceSpawn(String spawnerName) {
        String transactionID = UUID.randomUUID().toString();
        //pluginLogger.log(PluginLogger.LogLevel.INFO, "CustomMobs.spawnerForceSpawn called with " + spawnerName+" spawnedMobsMap"+spawnedMobsMap.toString());

        Map<String, CustomMobsFileManager.SpawnerData> spawnersData = fileManager.spawnersData;
        if (spawnedMobsMap == null || spawnedMobsMap.isEmpty()) {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "CustomMobs.spawnerForceSpawn. The spawnedMobsMap is empty or null.",transactionID);
        }else {
            try {

                Iterator<Map.Entry<UUID, String>> iterator = spawnedMobsMap.entrySet().iterator();
                //CustomMob customMob = betterElo.getCustomMobFromEntity(entity) ;
                while (iterator.hasNext()) {
                    Map.Entry<UUID, String> entry = iterator.next();
                    pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobs.spawnerForceSpawn. Checking key " + entry.getKey() + " with value " + entry.getValue(),transactionID);
                    if (spawnerName.equals(entry.getValue())) {
                        pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobs.spawnerForceSpawn.  " + spawnerName + " matching key " + entry.getKey() + ", value " + entry.getValue(),transactionID);
                        Entity entity = Bukkit.getServer().getEntity(entry.getKey());
                        if (entity != null && !entity.isDead()) {
                            entity.remove(); // Usuwa encję z świata

                            iterator.remove();  // Bezpieczne usuwanie wpisu podczas iteracji
                            pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobs.spawnerForceSpawn. Removing living mob from spawner" + spawnerName,transactionID);
                        }
                    }
                }
            } catch (Exception e) {
                pluginLogger.log(PluginLogger.LogLevel.ERROR, "CustomMobs.spawnerForceSpawn exception: " + e.getMessage(),transactionID);
            }
        }
        pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobs.spawnerForceSpawn called. Loaded spawners: " + spawnersData);
        // Sprawdzenie, czy istnieją spawnerzy w pliku
        if (spawnersData.isEmpty()) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "No spawners found in spawners.yml.",transactionID);
            return;
        }

        long currentTime = System.currentTimeMillis();

        for (Map.Entry<String, CustomMobsFileManager.SpawnerData> entry : spawnersData.entrySet()) {
            if(!entry.getKey().equals(spawnerName)){
                pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobs.spawnerForceSpawn checking spawner: " + entry,transactionID);
                continue;
            }
            pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobs.spawnerForceSpawn spawner: " + spawnerName+" found.",transactionID);
            CustomMobsFileManager.SpawnerData spawnerData = entry.getValue();
            Location location = getLocationFromString(spawnerData.location);


            // Spawnowanie zombiaków na podanej lokalizacji
            if (location != null) {
                World world = location.getWorld();
                if (world != null) {
                    int mobCount = spawnerData.mobCount;

                    String mobName = fileManager.getSpawnerMobName(spawnerName);
                    //pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobs.spawnZombieFromSpawner "+spawnerName+", maxMobs: "+maxMobs+", remaining slots: "+remainingSlots);
                    int spawnedMobs = 0;
                    pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobs.spawnerForceSpawn " + spawnerName + ", spawnerData.spawnedMobCount: " + spawnerData.spawnedMobCount,transactionID);
                    for (int i = 0; i < mobCount; i++) {
                        spawnCustomMob(location, spawnerName, mobName,transactionID);
                        spawnerData.spawnedMobCount++;
                        spawnedMobs++;
                    }
                    pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobs.spawnerForceSpawn spawnedMobs: " + spawnedMobs,transactionID);
                    // Ustawianie czasu ostatniego respa mobów z tego spawnera
                    spawnerLastSpawnTimes.put(spawnerName, currentTime);
                } else {
                    pluginLogger.log(PluginLogger.LogLevel.ERROR, "Invalid world specified for spawner " + spawnerName,transactionID);
                }
            } else {
                pluginLogger.log(PluginLogger.LogLevel.ERROR, "Invalid location specified for spawner " + spawnerName,transactionID);
            }
            pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobs.spawnerForceSpawn spawnedMobsMap"+spawnedMobsMap.toString(),transactionID);
        }
    }

    public Location getLocationFromString(String locationString) {
        try {
            String[] parts = locationString.split(",");
            if (parts.length == 4) {
                World world = Bukkit.getWorld(parts[0]);
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                double z = Double.parseDouble(parts[3]);
                //pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobs.getLocationFromString locationString: "+locationString+", ");
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
        }.runTaskTimer(plugin, 0, 300); // Interval converted to ticks (1 second)
    }

    public void stopSpawnerScheduler() {
        if (spawnerTask != null) {
            spawnerTask.cancel();
        }
    }
    private boolean canSpawnMobs(String spawnerName, int cooldown,String transactionID) {
        pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobs.canSpawnMobs " + spawnerName + " cooldown: "+cooldown,transactionID);
        if (!spawnerLastSpawnTimes.containsKey(spawnerName)) {
            pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobs.canSpawnMobs check passed, spawner not on the list, return true",transactionID);
            return true;
        }
        long lastUsage = spawnerLastSpawnTimes.get(spawnerName);
        long currentTime = System.currentTimeMillis();
        pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "CustomMobs.canSpawnMobs spawnerName: "+spawnerName+", lastUsage: "+lastUsage+", currentTime: "+currentTime+", timeleft: "+(cooldown-((currentTime-lastUsage)/1000)),transactionID);
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
    public void loadCustomMobs(String transactionID) {
        pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS,"CustomMobs.loadCustomMobs called.",transactionID);
        // Wczytaj customowe moby i przechowaj je w pamięci
        // Dla każdego pliku moba w folderze customMobs
        for (File mobFile : fileManager.getCustomMobFiles()) {
            if (!mobFile.getName().equals("customMobs/spawners.yml"))
            {
                try {
                    CustomMob customMob = fileManager.loadCustomMob(plugin, fileRewardManager, mobFile,transactionID);
                    if (customMob != null) {
                        customMobsMap.put(customMob.getMobName(), customMob);
                    }else
                    {
                        pluginLogger.log(PluginLogger.LogLevel.ERROR, "CustomMobs.loadCustomMobs could not load custom mob "+customMob,transactionID);
                    }
                } catch (Exception e) {
                    pluginLogger.log(PluginLogger.LogLevel.ERROR, "CustomMobs.loadCustomMobs exception: " + e.getMessage(),transactionID);
                }
            }
            pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobs.loadCustomMobs spawners.yml file detected, skipping",transactionID);
        }
    }


    public Entity getEntityForCustomMob(CustomMob customMob) {
        // Sprawdzenie, czy przypisana encja do moba istnieje
        if (customMob != null && customMob.entity != null) {
            return customMob.entity;  // Zwróć encję, jeśli jest już przypisana
        }
        // W przypadku braku encji, możemy zwrócić null lub rozważyć inne działanie, np. logowanie
        return null;  // Zwróć null, jeśli encja nie jest ustawiona
    }

    public void spawnCustomMob(Location location, String spawnerName, String mobName,String transactionID) {
        pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobs.spawnCustomMob called, mobName: " + mobName+", spawnerName: "+spawnerName+", location: "+location,transactionID);
        CustomMob templateMob = customMobsMap.get(mobName);
        if (templateMob != null) {
            Location adjustedLocation = adjustLocationToAirAbove(location);
            CustomMob newMob=null;

            if(templateMob.passengerMobName!=null){
                newMob = templateMob.cloneForSpawn(adjustedLocation);
                newMob.customMetadata.put("SpawnerName", spawnerName);
                newMob.spawnerName = spawnerName;
                CustomMob passengerTemplateMob = customMobsMap.get(templateMob.passengerMobName);
                if(passengerTemplateMob!=null) {
                    pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobs.spawnCustomMob spawning newMob.passengerMobName: "+newMob.passengerMobName,transactionID);
                    CustomMob newPassengerMob = passengerTemplateMob.cloneForSpawn(adjustedLocation, passengerTemplateMob.entityType.toString());
                    pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobs.spawnCustomMob newMob.passengerMobName: "+newMob.passengerMobName+" spawned, adding as passenger",transactionID);
                    newMob.entity.addPassenger(newPassengerMob.entity);
                    newPassengerMob.customMetadata.put("SpawnerName", spawnerName);
                    newPassengerMob.spawnerName = spawnerName;
                    spawnedMobsMap.put(newPassengerMob.entity.getUniqueId(), spawnerName);
                }
            }else{
                newMob = templateMob.cloneForSpawn(adjustedLocation, templateMob.entityType.toString());
                newMob.customMetadata.put("SpawnerName", spawnerName);
                newMob.spawnerName = spawnerName;
            }
            pluginLogger.log(PluginLogger.LogLevel.DROP, "CustomMobs.spawnCustomMob newMob.dropTablename: "+newMob.dropTableName+",  newMob.dropTable: "+newMob.dropTable,transactionID);
            pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobs.spawnCustomMob newMob.spawnerName: "+newMob.spawnerName,transactionID);
            try{
                spawnedMobsMap.put(newMob.entity.getUniqueId(), spawnerName);
                pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Mob spawned with UUID: " + newMob.entity.getUniqueId() + ", spawnerName:" + newMob.spawnerName,transactionID);
            }catch (Exception e){
                pluginLogger.log(PluginLogger.LogLevel.ERROR, "CustomMobs.spawnCustomMob  exception: "+e.getMessage(),transactionID);
            }
        } else {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "CustomMobs.spawnCustomMob failed, mob not found: " + mobName,transactionID);
        }
    }
    public void spawnCustomMob(Location location, String mobName,String transactionID) {

        CustomMob templateMob = customMobsMap.get(mobName);
        pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobs.spawnCustomMob called, mobName: " + mobName+", location: "+location+", mobtype: "+templateMob.entityType.toString(),transactionID);
        if (templateMob != null) {
            Location adjustedLocation = adjustLocationToAirAbove(location);
            CustomMob newMob = null;

            pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobs.spawnCustomMob newMob.passengerMobName: "+templateMob.passengerMobName);
            if(templateMob.passengerMobName!=null){
                newMob = templateMob.cloneForSpawn(adjustedLocation);
                CustomMob passengerTemplateMob = customMobsMap.get(templateMob.passengerMobName);
                if(passengerTemplateMob!=null) {
                    pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobs.spawnCustomMob spawning newMob.passengerMobName: "+newMob.passengerMobName);
                    CustomMob newPassengerMob = passengerTemplateMob.cloneForSpawn(adjustedLocation, passengerTemplateMob.entityType.toString());
                    pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobs.spawnCustomMob newMob.passengerMobName: "+newMob.passengerMobName+" spawned, adding as passenger");
                    newMob.entity.addPassenger(newPassengerMob.entity);
                }
            }else{
                newMob = templateMob.cloneForSpawn(adjustedLocation, templateMob.entityType.toString());
            }
        } else {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "CustomMobs.spawnCustomMob failed, mob not found: " + mobName);
        }
    }





}
