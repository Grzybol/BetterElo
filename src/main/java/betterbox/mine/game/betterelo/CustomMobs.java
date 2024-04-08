package betterbox.mine.game.betterelo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
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

import java.util.*;

import java.util.UUID;

public class CustomMobs {
    private final PluginLogger pluginLogger;
    private final JavaPlugin plugin;
    private final FileRewardManager fileRewardManager;
    private final CustomMobsFileManager fileManager;
    private static final Random random = new Random();
    private BukkitTask spawnerTask;
    private Map<String, Long> spawnerLastSpawnTimes = new HashMap<>(); // Mapa przechowująca czas ostatniego respa mobów z każdego spawnera

    static class CustomMob {
        String mobName, dropTablename;
        LivingEntity entity;
        ItemStack helmet, chestplate, leggings, boots;
        HashMap< Double,ItemStack> dropTable;
        double armor, speed, attackDamage;
        int hp;
        Map<String, Object> customMetadata; // Nowe pole do przechowywania niestandardowych metadanych
        JavaPlugin plugin;

        CustomMob(FileRewardManager dropFileManager, String mobName, LivingEntity entity, ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots, HashMap<Double,ItemStack> dropTable, double armor, int hp, double speed, double attackDamage) {
            this.mobName = mobName;
            this.entity = entity;
            this.helmet = helmet;
            this.chestplate = chestplate;
            this.leggings = leggings;
            this.boots = boots;
            this.dropTable = dropTable;
            this.armor = armor;
            this.hp = hp;
            this.speed = speed;
            this.attackDamage = attackDamage;
            this.dropTable = dropFileManager.loadCustomDrops(mobName);
            setupMob();
        }
        // Przeciążony konstruktor przyjmujący dodatkowe metadane
        CustomMob(JavaPlugin plugin,FileRewardManager dropFileManager, String mobName, LivingEntity entity, ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots, double armor, int hp, double speed, double attackDamage, Map<String, Object> customMetadata) {
            this.plugin = plugin;
            this.mobName = mobName;
            this.entity = entity;
            this.helmet = helmet;
            this.chestplate = chestplate;
            this.leggings = leggings;
            this.boots = boots;
            //this.dropTable = dropTable;
            this.dropTable = dropFileManager.loadCustomDrops(mobName);
            this.armor = armor;
            this.hp = hp;
            this.speed = speed;
            this.attackDamage = attackDamage;
            this.customMetadata = customMetadata;
            setupMob();
        }

        private void setupMob() {
            entity.getEquipment().setHelmet(helmet);
            entity.getEquipment().setChestplate(chestplate);
            entity.getEquipment().setLeggings(leggings);
            entity.getEquipment().setBoots(boots);

            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(hp);
            entity.setHealth(hp);
            entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
            entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(attackDamage);
            // Dodaj więcej atrybutów według potrzeb.

            entity.setCustomName(mobName);
            entity.setCustomNameVisible(true);
            // Ustawianie niestandardowych metadanych
            customMetadata.forEach((key, value) -> {
                entity.setMetadata(key, new FixedMetadataValue(plugin, value));
            });

            // Tutaj można dodać logikę dotyczącą dropTable
        }
        public String getMobName(){
            return this.mobName;
        }
        public Double getMobArmor(){
            return this.armor;
        }
        public int getMaxHealth() {
            return this.hp; // Tutaj hp to maksymalne zdrowie
        }

        // Zwraca aktualne zdrowie moba
        public double getHealth() {
            if (this.entity != null) {
                return this.entity.getHealth();
            }
            return 0; // W przypadku, gdy entity jest null, zwracamy 0
        }
        public Entity getEntity() {
            return this.entity;
        }
        public void setDisplayName(String name) {
            if (this.entity != null) {
                this.entity.setCustomName(name);
                this.entity.setCustomNameVisible(true); // Upewnij się, że nazwa jest zawsze widoczna
                this.mobName = name; // Opcjonalnie aktualizuj również wewnętrzną nazwę moba
            } else {
                // Logika obsługi przypadku, gdy entity nie jest zainicjowane
                plugin.getLogger().warning("Próba ustawienia nazwy dla niezainicjowanego moba: " + name);
            }
        }




    }



    public CustomMobs(PluginLogger pluginLogger, JavaPlugin plugin, CustomMobsFileManager fileManager,FileRewardManager fileRewardManager){
        this.plugin = plugin;
        this.pluginLogger = pluginLogger;
        this.fileManager = fileManager;
        this.fileRewardManager = fileRewardManager;
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
        helmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL,1);
        chestplate.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL,1);
        leggings.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL,1);
        boots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL,1);

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
        //updateCustomMobName(customMob);

        // Ustawienie niestandardowego dropu
        //zombie.setCustomDropItems(customDrops);
    }
    public void updateCustomMobName(LivingEntity mob) {
        pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobs.updateZombieCustomName called, mob.getName(): "+mob.getName());
        if (!mob.hasMetadata("MobName")) {
            return;
        }
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
                        //spawnModifiedZombie(location,spawnerName);
                        spawnModifiedZombieUsingCustomMob(location,spawnerName);
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
    public void spawnModifiedZombieUsingCustomMob(Location location, String spawnerName) {
        pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS,"CustomMobs.spawnModifiedZombieUsingCustomMob called, location: "+location.toString());

        // Przygotowanie atrybutów i wyposażenia dla CustomMoba
        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
        helmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        chestplate.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
        leggings.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
        boots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);

        String mobName = "Modyfikowany_Zombiak_v2";

        // Przykładowa pusta tabela dropu (w rzeczywistych zastosowaniach należy ją odpowiednio załadować)
        HashMap<Double, ItemStack> dropTable = new HashMap<>();

        // Dodatkowe metadane
        Map<String, Object> customMetadata = new HashMap<>();
        customMetadata.put("CustomZombie", true);
        customMetadata.put("SpawnerName", spawnerName);
        customMetadata.put("DropTable", mobName);
        customMetadata.put("MobName", mobName);

        // Tworzenie zombiaka przy użyciu klasy CustomMob
        Zombie zombie = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
        try{
            CustomMob customMob = new CustomMob(plugin,fileRewardManager, mobName, zombie, helmet, chestplate, leggings, boots, 100.0, 2000, 0.4, 8.0, customMetadata);
        }catch (Exception e){
            pluginLogger.log(PluginLogger.LogLevel.ERROR,"CustomMobs.spawnModifiedZombieUsingCustomMob exception "+e.getMessage());
        }
    }


}
