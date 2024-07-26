package betterbox.mine.game.betterelo;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import me.clip.placeholderapi.libs.kyori.adventure.platform.facet.Facet;
import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.checkerframework.checker.units.qual.C;

public final class BetterElo extends JavaPlugin {
    HashMap<LivingEntity, BukkitTask> mobTasks = new HashMap<>();
    private static BetterElo instance;
    private PluginLogger pluginLogger;
    private DataManager dataManager;
    public final Map<Entity, CustomMobs.CustomMob> customMobsMap = new HashMap<>();
    public int eventDuration;
    public boolean isEventEnabled;
    public String eventUnit;
    private Placeholders placeholders;
    private CustomMobs customMobs;
    private CustomMobsFileManager customMobsFileManager;
    private CheaterCheckScheduler cheaterCheckScheduler;
    private BetterRanksCheaters betterRanksCheaters;
    private GuiManager guiManager;
    private FileRewardManager fileRewardManager;
    private Event event;
    private BukkitTask dailyTask;
    private BukkitTask weeklyTask;
    private BukkitTask monthlyTask;
    private PlayerKillDatabase PKDB;
    private BetterEloCommand betterEloCommand;
    private ExtendedConfigManager configManager;
    public Map<String, Boolean> rewardStates = new HashMap<>();
    public boolean useHolographicDisplays;
    //public static final Flag<StateFlag.State> NO_ELO_FLAG = new StateFlag("noElo", false);
    public static StateFlag IS_ELO_ALLOWED;
    private String folderPath;
    private NamespacedKey mobDefenseKey,mobDamageKey,averageDamageKey;
    @Override
    public void onLoad() {
        getLogger().info("Registering custom WorldGuard flags.");
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag allowElo = new StateFlag("allowElo", false);
            registry.register(allowElo);
            IS_ELO_ALLOWED = allowElo; // only set our field if there was no error
            getLogger().info("Custom WorldGuard flags registered.");
        } catch (Exception e) {
            getLogger().info("NoElo flag registration exception: " + e);
        }
    }

    @Override
    public void onEnable() {
        int pluginId = 22747; // Zamień na rzeczywisty ID twojego pluginu na bStats
        Metrics metrics = new Metrics(this, pluginId);
        this.mobDefenseKey = new NamespacedKey(this, "mob_defense");
        this.mobDamageKey = new NamespacedKey(this, "mob_damage");
        this.averageDamageKey = new NamespacedKey(this, "average_damage");
        instance = this;
        createPluginFolders();
        createExampleDropTablesFiles();
        createExampleDropsFiles();
        createExampleMobsFiles();
        createExampleConfigFiles();
        // Inicjalizacja PluginLoggera
        Set<PluginLogger.LogLevel> defaultLogLevels = EnumSet.of(PluginLogger.LogLevel.INFO,PluginLogger.LogLevel.DEBUG, PluginLogger.LogLevel.WARNING, PluginLogger.LogLevel.ERROR);
        folderPath = getDataFolder().getAbsolutePath();
        pluginLogger = new PluginLogger(folderPath, defaultLogLevels,this);
        pluginLogger.log(PluginLogger.LogLevel.INFO,"BetterElo: onEnable: Starting BetterElo plugin");
        pluginLogger.log(PluginLogger.LogLevel.INFO,"Plugin created by "+this.getDescription().getAuthors());
        pluginLogger.log(PluginLogger.LogLevel.INFO,"Plugin version "+this.getDescription().getVersion());
        pluginLogger.log(PluginLogger.LogLevel.INFO,"https://github.com/Grzybol/BetterElo");
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterElo: onEnable: Loading config.yml");
        configManager = new ExtendedConfigManager(this, pluginLogger);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterElo: onEnable: Zaladowano loggera.");
        PKDB = new PlayerKillDatabase(pluginLogger);
        // Przekazujemy pluginLogger do innych klas
        dataManager = new DataManager(this, pluginLogger);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterElo: onEnable: Zaladowano DataManager.");
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterElo: onEnable: Zaladowano RewardManager.");
        // ... dla innych klas również
        dataManager.initializeDataFolder();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterElo: onEnable: Zaladowano folder.");
        dataManager.initializeDatabaseFile();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterElo: onEnable: Zaladowano baze danych.");
        dataManager.loadDataFromFile();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterElo: onEnable: DataManager został zainicjowany i dane zostały wczytane.");
        // Inicjalizacja RewardManager
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterElo: onEnable: RewardManager został zainicjowany.");
        // Inicjalizacja FileRewardManager
        fileRewardManager = new FileRewardManager(this, pluginLogger);
        // Jeśli FileRewardManager wymaga inicjalizacji, dodaj tutaj
        // Inicjalizacja GuiManager

        // Inicjalizacja Placeholders
        placeholders = new Placeholders(dataManager, this);
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholders.register();
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterElo: onEnable: Placeholders zostały zarejestrowane w PlaceholderAPI.");
        } else {
            pluginLogger.log(PluginLogger.LogLevel.WARNING,"BetterElo: onEnable: Warning: PlaceholderAPI not found, placeholders will NOT be available.");
        }



        customMobsFileManager = new CustomMobsFileManager(folderPath,this, pluginLogger);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterElo: onEnable: calling customMobsFileManager.loadSpawners()");
        customMobsFileManager.loadSpawners();
        customMobs = new CustomMobs(pluginLogger,this,customMobsFileManager, fileRewardManager,this );
        pluginLogger.log(PluginLogger.LogLevel.INFO,"Starting spawners scheduler...");
        customMobs.startSpawnerScheduler();

        guiManager = new GuiManager(fileRewardManager, pluginLogger, this, dataManager, customMobsFileManager, customMobs, this);
        // Rejestracja komendy
        betterRanksCheaters = new BetterRanksCheaters(this,pluginLogger);
        CheaterCheckScheduler cheaterCheckScheduler = new CheaterCheckScheduler(this, betterRanksCheaters, getServer().getScheduler(), pluginLogger);
        // Rejestracja listenera eventów
        event = new Event(dataManager, pluginLogger,this,betterRanksCheaters,configManager,this,customMobs,fileRewardManager,guiManager,customMobsFileManager);
        getServer().getPluginManager().registerEvents(event, this);
        getCommand("be").setExecutor(new BetterEloCommand(this, dataManager, guiManager, pluginLogger, this, configManager,event,PKDB, customMobs, customMobsFileManager));
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterElo: onEnable: Plugin BetterElo został włączony pomyślnie.");
        // Inicjalizacja RewardManagera (kod z konstruktora RewardManager)
        rewardStates.put("daily", true);
        rewardStates.put("weekly", true);
        rewardStates.put("monthly", true);
        // Inicjalizacja nagród i ich harmonogramów (kod z metody onEnable z klasy RewardManager)
        loadRewards();
        pluginLogger.log(PluginLogger.LogLevel.INFO,"Scheduling daily ranking rewards...");
        scheduleRewards("daily", TimeUnit.DAYS.toMillis(1), false);
        pluginLogger.log(PluginLogger.LogLevel.INFO,"Scheduling weekly ranking rewards...");
        scheduleRewards("weekly", TimeUnit.DAYS.toMillis(7), false);
        pluginLogger.log(PluginLogger.LogLevel.INFO,"Scheduling monthly ranking rewards...");
        scheduleRewards("monthly", 0, true);

        File dataFolder = new File(getDataFolder(),"data");
        File databaseFile = new File(dataFolder, "database.txt");
        //WebRankingServer server = new WebRankingServer(databaseFile.getAbsolutePath(), 39378,pluginLogger);

        //server.startServer();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterElo: onEnable: web ranking server started");

        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterElo: onEnable: starting ChatNotifier every 30min");
        // Tworzenie nowego zadania i ustalanie interwału (30 minut = 30 * 60 * 20 ticks)
        new ChatNotifier(this).runTaskTimer(this, 0, 36000);
        // Uzyskaj dostęp do loggera pluginu
        java.util.logging.Logger logger = this.getLogger();


        // Użyj loggera do rejestrowania wiadomości
        logger.info("[BetterElo] Running");
        logger.info("[BetterElo] Author " + this.getDescription().getAuthors());
        logger.info("[BetterElo] Version  " + this.getDescription().getVersion());
        logger.info("[BetterElo] " + this.getDescription().getDescription());
        // Inicjalizacja BetterRanksCheaters


        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterElo: onEnable: calling cheaterCheckScheduler.startScheduler()");
        cheaterCheckScheduler.startScheduler();
        pluginLogger.log(PluginLogger.LogLevel.INFO,"Checking HolographicDisplays...");
        useHolographicDisplays = Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays");
        if(useHolographicDisplays){
            pluginLogger.log(PluginLogger.LogLevel.INFO,"HolographicDisplays found.");
        }else{
            pluginLogger.log(PluginLogger.LogLevel.WARNING,"HolographicDisplays not found! Some feature might not be available");
        }
        killAllCustomMobs();

        checkMobsExistence();

    }
    public void createPluginFolders() {
        // Lista folderów do utworzenia
        String[] folders = {"customDrops", "customDropTables", "customMobs", "logs","data"};

        // Tworzenie folderów, jeśli nie istnieją
        for (String folderName : folders) {
            File folder = new File(getDataFolder(), folderName);
            if (!folder.exists()) {
                folder.mkdirs();  // Metoda mkdirs() tworzy folder oraz wszystkie wymagane nadrzędne foldery
                getLogger().severe("Catalogs "+folders.toString()+" created!");
            }
        }
    }
    private void createExampleDropsFiles() {
        // Tworzenie docelowego folderu, jeśli nie istnieje
        File customFolder = new File(getDataFolder(), "customDrops");
        if (!customFolder.exists()) {
            customFolder.mkdirs();
        }

        // Lokalizacja docelowego pliku konfiguracyjnego
        String[] fileNames = {"ExampleDropTable_item0.yml", "ExampleDropTable_item1.yml", "ExampleDropTable_item2.yml", "ExampleDropTable_item3.yml", "ExampleDropTable_item4.yml", "ExampleDropTable_item5.yml"};  // Dodaj więcej nazw plików jak potrzebujesz

        for (String fileName : fileNames) {
            File file = new File(customFolder, fileName);
            if (!file.exists()) {
                try (InputStream in = getResource("customDrops/" + fileName)) {
                    if (in == null) {
                        getLogger().severe("Resource 'customDrops/" + fileName + "' not found.");
                        continue;
                    }
                    Files.copy(in, file.toPath());
                } catch (IOException e) {
                    getLogger().severe("Could not save " + fileName + " to " + file + ": " + e.getMessage());
                }
            }
        }
    }
    private void createExampleDropTablesFiles() {
        // Tworzenie docelowego folderu, jeśli nie istnieje
        File customFolder = new File(getDataFolder(), "customDropTables");
        if (!customFolder.exists()) {
            customFolder.mkdirs();
        }

        // Lokalizacja docelowego pliku konfiguracyjnego
        String[] fileNames = {"ExampleDropTable.yml"};

        for (String fileName : fileNames) {
            File file = new File(customFolder, fileName);
            if (!file.exists()) {
                try (InputStream in = getResource("customDropTables/" + fileName)) {
                    if (in == null) {
                        getLogger().severe("Resource 'customDropTables/" + fileName + "' not found.");
                        continue;
                    }
                    Files.copy(in, file.toPath());
                } catch (IOException e) {
                    getLogger().severe("Could not save " + fileName + " to " + file + ": " + e.getMessage());
                }
            }
        }
    }
    private void createExampleConfigFiles() {
        // Tworzenie docelowego folderu, jeśli nie istnieje
        File customFolder = getDataFolder();
        if (!customFolder.exists()) {
            customFolder.mkdirs();
        }

        // Lokalizacja docelowego pliku konfiguracyjnego
        String[] fileNames = {"config.yml"};

        for (String fileName : fileNames) {
            File file = new File(customFolder, fileName);
            if (!file.exists()) {
                try (InputStream in = getResource(fileName)) {
                    if (in == null) {
                        getLogger().severe(fileName + "' not found.");
                        continue;
                    }
                    Files.copy(in, file.toPath());
                } catch (IOException e) {
                    getLogger().severe("Could not save " + fileName + " to " + file + ": " + e.getMessage());
                }
            }
        }
    }
    private void createExampleMobsFiles() {
        // Tworzenie docelowego folderu, jeśli nie istnieje
        File customFolder = new File(getDataFolder(), "customMobs");
        if (!customFolder.exists()) {
            customFolder.mkdirs();
        }

        // Lokalizacja docelowego pliku konfiguracyjnego
        String[] fileNames = {"Marksman.yml","Panda.yml"};

        for (String fileName : fileNames) {
            File file = new File(customFolder, fileName);
            if (!file.exists()) {
                try (InputStream in = getResource("customMobs/" + fileName)) {
                    if (in == null) {
                        getLogger().severe("Resource 'customMobs/" + fileName + "' not found.");
                        continue;
                    }
                    Files.copy(in, file.toPath());
                } catch (IOException e) {
                    getLogger().severe("Could not save " + fileName + " to " + file + ": " + e.getMessage());
                }
            }
        }
    }


    @Override
    public void onDisable() {
        saveCustomMobsToCache();
        removeAndKillAllCustomMobs();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterElo: onDisable: Zapisywanie danych przed wyłączeniem pluginu...");
        dataManager.saveDataToFile();
        dataManager.saveDataToFileDaily();
        dataManager.saveDataToFileWeekly();
        dataManager.saveDataToFileMonthly();
        dataManager.saveDataToFileEvent();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterElo: onDisable: Plugin BetterElo został wyłączony.");
        // Wyłączanie nagród (kod z metody onDisable z klasy RewardManager)
        if (dailyTask != null) dailyTask.cancel();
        if (weeklyTask != null) weeklyTask.cancel();
        if (monthlyTask != null) monthlyTask.cancel();

        //customMobs.stopSpawnerScheduler();
    }
    public void saveCustomMobsToCache() {
        List<String> mobNames = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.hasMetadata("CustomMob")) {
                    String customName = entity.getName();
                    if (!customName.isEmpty()) {
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo.saveCustomMobsToCache saving entity "+customName);
                        mobNames.add(customName);
                    }
                }
            }
        }

        File cacheFile = new File(getDataFolder(), "customMobsCache.yml");
        YamlConfiguration cacheConfig = YamlConfiguration.loadConfiguration(cacheFile);
        cacheConfig.set("customMobNames", mobNames);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo.saveCustomMobsToCache data to save: " + mobNames);

        try {
            cacheConfig.save(cacheFile);
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "BetterElo.saveCustomMobsToCache IOException: " + e.getMessage());
        }
    }
    public void loadAndKillCustomMobsFromCache() {
        File cacheFile = new File(getDataFolder(), "customMobsCache.yml");
        YamlConfiguration cacheConfig = YamlConfiguration.loadConfiguration(cacheFile);
        List<String> mobNames = cacheConfig.getStringList("customMobNames");
        int killedMobsCount = 0;

        if (mobNames == null || mobNames.isEmpty()) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo.saveCustomMobsToCache cache is empty!");
            return;
        } // Jeśli nie ma danych, zakończ metodę
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo.saveCustomMobsToCache mobs from cache: "+mobNames);
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo.saveCustomMobsToCache checking entity "+entity.getName());
                //entity.getName();
                if (mobNames.contains(entity.getName())) {
                    entity.remove();
                    killedMobsCount++;
                    //customMobs.decreaseMobCount();
                }
            }
        }

        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo.loadAndKillCustomMobsFromCache killed " + killedMobsCount + " mobs");

        // Opcjonalnie: wyczyść plik cache po wczytaniu
        cacheConfig.set("customMobNames", new ArrayList<String>());
        try {
            cacheConfig.save(cacheFile);
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "BetterElo.loadAndKillCustomMobsFromCache IOException: " + e.getMessage());
        }
    }



    // Dodajemy nowe metody do uzyskania pozostałego czasu dla nagród
    public long getRemainingTimeForRewards(String period) {
        //pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterElo: getRemainingTimeForRewards: period: "+period);
        FileConfiguration config = getConfig();
        long periodMillis = 0;
        boolean useNextMonthTime = false;

        switch (period.toLowerCase()) {
            case "daily":
                periodMillis = TimeUnit.DAYS.toMillis(1);
                break;
            case "weekly":
                periodMillis = TimeUnit.DAYS.toMillis(7);
                break;
            case "monthly":
                useNextMonthTime = true;
                break;
            case "event":
                if(Objects.equals(eventUnit, "h")) {
                    periodMillis = TimeUnit.HOURS.toMillis(eventDuration);
                }else if(Objects.equals(eventUnit, "m")){
                    periodMillis = TimeUnit.MINUTES.toMillis(eventDuration);
                }else{
                    pluginLogger.log(PluginLogger.LogLevel.ERROR,"BetterElo.getRemainingTimeForRewards: eventUnit: "+eventUnit);
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid period: " + period);
        }

        long lastScheduledTime = config.getLong(period + "LastScheduledTime", System.currentTimeMillis());

        if (useNextMonthTime) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(lastScheduledTime);
            calendar.add(Calendar.MONTH, 1);
            return calendar.getTimeInMillis() - System.currentTimeMillis();
        } else {
            return periodMillis - (System.currentTimeMillis() - lastScheduledTime);
        }
    }

    public void loadRewards() {
        for (String rewardType : rewardStates.keySet()) {
            File rewardFile = new File(getDataFolder(), rewardType + ".yml");
            if (!rewardFile.exists()) {
                createDefaultRewardFile(rewardFile, rewardType);
            } else {
                loadReward(rewardFile, rewardType);
            }
        }
    }
    private void createDefaultRewardFile(File rewardFile, String rewardType) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: createDefaultRewardFile: called");
        try {
            rewardFile.createNewFile();
            FileConfiguration rewardConfig = YamlConfiguration.loadConfiguration(rewardFile);
            rewardConfig.set("material", "DIAMOND");
            rewardConfig.set("amount", 1);
            rewardConfig.save(rewardFile);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: createDefaultRewardFile: default reward file created");
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "BetterElo: createDefaultRewardFile: "+e);
        }
    }
    private void loadReward(File rewardFile, String rewardType) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: loadReward: called");
        FileConfiguration rewardConfig = YamlConfiguration.loadConfiguration(rewardFile);
        String materialName = rewardConfig.getString("material", "DIAMOND");
        Material material = Material.getMaterial(materialName);
        int amount = rewardConfig.getInt("amount", 1);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: loadReward: loaded");
    }
    public void updateLastScheduledTime(String period) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: updateLastScheduledTime: period: "+period+" setting current system time as last scheduled time");
        FileConfiguration config = getConfig();
        config.set(period + "LastScheduledTime", System.currentTimeMillis());
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: updateLastScheduledTime: calling PKDB.clearTable(period) wit parameters: "+period);
        PKDB.clearTable(period);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: updateLastScheduledTime: period: "+period+" starting saveConfig");

        saveConfig();
    }

    public void scheduleRewards(String period, long periodMillis, boolean useNextMonthTime) {
        FileConfiguration config = getConfig();
        long lastScheduledTime = config.getLong(period + "LastScheduledTime", System.currentTimeMillis());
        long delay;
        try{
            pluginLogger.log(PluginLogger.LogLevel.RANKING_REWARDS, "BetterElo: scheduleRewards: period: " + period + " periodMillis: " + periodMillis + " LastScheduledTime: " + lastScheduledTime);
            if (useNextMonthTime) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(lastScheduledTime);
                calendar.add(Calendar.MONTH, 1);
                delay = (calendar.getTimeInMillis() - System.currentTimeMillis()) / 1000 * 20;
            } else {
                delay = (periodMillis - (System.currentTimeMillis() - lastScheduledTime)) / 1000 * 20;
            }

            pluginLogger.log(PluginLogger.LogLevel.RANKING_REWARDS, "BetterElo: scheduleRewards: period: " + period + " periodMillis: " + periodMillis + " Computed delay: " + delay);

            if (delay < 0) {
                pluginLogger.log(PluginLogger.LogLevel.RANKING_REWARDS, "BetterElo: scheduleRewards: Negative delay detected, starting rewardAndReschedule");
                rewardAndReschedule(period, periodMillis, useNextMonthTime);
                pluginLogger.log(PluginLogger.LogLevel.INFO, "Ranking "+period+" has finished!");
                return;
            }
            //saveConfig();
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (rewardStates.get(period)) {
                        pluginLogger.log(PluginLogger.LogLevel.RANKING_REWARDS, "BetterElo: scheduleRewards: BukkitRunnable: starting rewardAndReschedule");
                        rewardAndReschedule(period, periodMillis, useNextMonthTime);
                        pluginLogger.log(PluginLogger.LogLevel.RANKING_REWARDS, "BetterElo: scheduleRewards: BukkitRunnable: rewardAndReschedule done");
                    }
                }
            }.runTaskLater(this, delay);
        }catch (Exception e){
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "BetterElo: scheduleRewards exception: "+e.getMessage());
        }
    }
    public static BetterElo getInstance() {
        return instance;
    }
    public CustomMobs.CustomMob getCustomMob(String mobName) {
        // Zwróć customowego moba na podstawie nazwy
        return customMobsMap.get(mobName);
    }
    public void stopEvent(){
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo.stopEvent called");
        if(isEventEnabled) {
            pluginLogger.log(PluginLogger.LogLevel.INFO, "Event ended - removing data.");
            FileConfiguration config = getConfig();
            config.set("eventLastScheduledTime", null);
            rewardStates.remove("event");
            loadRewards();
            isEventEnabled = false;
            saveConfig();
        }
        else{
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo.stopEvent event not active");
        }
    }
    private void rewardAndReschedule(String period, long periodMillis, boolean useNextMonthTime) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo.rewardAndReschedule called");
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo.rewardAndReschedule calling notifyTopPlayers("+period+")");
        notifyTopPlayers(period);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: rewardAndReschedule: Starting rewardTopPlayers");
        rewardTopPlayers(period);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: rewardAndReschedule: rewardTopPlayers done");
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: rewardAndReschedule: Starting updateLastScheduledTime");
        if(period.equals("event")){
            pluginLogger.log(PluginLogger.LogLevel.INFO, "Event ended - removing data.");
            FileConfiguration config = getConfig();
            config.set(period + "LastScheduledTime", null);
            rewardStates.remove("event");
            loadRewards();
            isEventEnabled=false;
            saveConfig();
        return;
        }
        updateLastScheduledTime(period);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: rewardAndReschedule: updateLastScheduledTime done");
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: rewardAndReschedule: starting scheduleRewards");
        scheduleRewards(period, periodMillis, useNextMonthTime);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: rewardAndReschedule: scheduleRewards done");
    }
    public void rewardTopPlayers(String rewardType) {
        pluginLogger.log(PluginLogger.LogLevel.INFO, "BetterElo: rewardTopPlayers: rewardType: "+rewardType);
        for (int i = 1; i <= 10; i++) {
            String playerName = null;
            switch (rewardType) {
                case "daily":
                    playerName = dataManager.getPlayerAtPosition(i, dataManager.dailyPlayerPoints);

                    break;
                case "weekly":
                    playerName = dataManager.getPlayerAtPosition(i, dataManager.weeklyPlayerPoints);

                    break;
                case "monthly":
                    playerName = dataManager.getPlayerAtPosition(i, dataManager.monthlyPayerPoints);
                    break;
                case "event":
                    playerName = dataManager.getPlayerAtPosition(i, dataManager.eventPlayerPoints);
                    break;
            }
            if (playerName != null) {
                String foramttedMessage =  ChatColor.GOLD +""+ ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Rewarding player "+ChatColor.GOLD+ ChatColor.BOLD +playerName+ChatColor.AQUA +" in "+ChatColor.GOLD+ChatColor.BOLD+rewardType+" ranking for reaching "+ ChatColor.GOLD+ChatColor.BOLD+"TOP"+i;
                Bukkit.getServer().broadcastMessage(foramttedMessage);
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
                if (offlinePlayer != null && offlinePlayer.hasPlayedBefore()) {
                    // Ustaw odpowiedni subType zależnie od pozycji gracza
                    String subType;
                    if (i == 1) subType = "top1";
                    else if (i == 2) subType = "top2";
                    else if (i == 3) subType = "top3";
                    else subType = "top4-10";

                    // Ustaw typ nagrody i pobierz odpowiednie nagrody
                    fileRewardManager.setRewardType(rewardType, subType);
                    List<ItemStack> rewardItems = fileRewardManager.getReward(fileRewardManager.getRewardType());

                    if (rewardItems == null || rewardItems.isEmpty()) {
                        pluginLogger.log(PluginLogger.LogLevel.WARNING, "BetterElo: rewardTopPlayers: Reward items are null or empty for reward type: " + rewardType + "_" + subType);
                        continue;
                    }
                    Player player = offlinePlayer.getPlayer();
                    if (player != null && player.isOnline()) {
                        for (ItemStack rewardItem : rewardItems) {
                            // Dodaj lore jeśli przedmiot jest na liście eventItemsPlaceholder
                            if (configManager.eventItemsPlaceholder.contains(rewardItem.getType().toString())) {
                                ItemMeta itemMeta = rewardItem.getItemMeta();
                                List<String> lore = itemMeta.getLore();
                                if (lore == null) {
                                    lore = new ArrayList<>();
                                }
                                lore.add(ChatColor.GRAY + "Reward for "+ChatColor.GOLD+ChatColor.BOLD + player.getName());
                                itemMeta.setLore(lore);
                                rewardItem.setItemMeta(itemMeta);
                            }
                            if (player.getInventory().firstEmpty() != -1) {
                                player.getInventory().addItem(rewardItem);
                                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: rewardTopPlayers: Rewarding player: " + player.getName());
                            } else {
                                pluginLogger.log(PluginLogger.LogLevel.WARNING, "BetterElo: rewardTopPlayers: No space in inventory for player: " + player.getName());
                                break;
                            }
                        }
                    } else {
                        // Gracz jest offline, zapisz nagrodę do późniejszego przyznania
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: rewardTopPlayers: Player is not online, saving rewards for: " + offlinePlayer.getName());
                        saveOfflineReward(playerName, rewardItems);

                    }
                } else {
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: rewardTopPlayers: Player has not played before: " + offlinePlayer.getName());
                }
            } else {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: rewardTopPlayers: No player at position: " + i);
            }
        }
        switch (rewardType) {
            case "daily":
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: rewardTopPlayers: Clearing the daily ranking");
                dataManager.dailyPlayerPoints.clear();
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: rewardTopPlayers: Saving the daily ranking");
                dataManager.saveDataToFileDaily();
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: rewardTopPlayers: Resetting the daily ranking timer");
                updateLastScheduledTime(rewardType);
                break;
            case "weekly":
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: rewardTopPlayers: Clearing the weekly ranking");
                dataManager.weeklyPlayerPoints.clear();
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: rewardTopPlayers: Saving the weekly ranking");
                dataManager.saveDataToFileWeekly();
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: rewardTopPlayers: Resetting the weekly ranking timer");
                updateLastScheduledTime(rewardType);
                break;
            case "monthly":
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: rewardTopPlayers: Clearing the monthly ranking");
                dataManager.monthlyPayerPoints.clear();
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: rewardTopPlayers: Saving the monthly ranking");
                dataManager.saveDataToFileMonthly();
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: rewardTopPlayers: Resetting the monthly ranking timer");
                updateLastScheduledTime(rewardType);
                break;
            case "event":
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: rewardTopPlayers: Clearing the event ranking");
                dataManager.eventPlayerPoints.clear();
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: rewardTopPlayers: Saving the event ranking");
                dataManager.saveDataToFileEvent();
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: rewardTopPlayers: Resetting the event ranking timer");
                break;
        }
    }
    private void saveOfflineReward(String playerName, List<ItemStack> rewardItems) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: saveOfflineReward: Checking if there are any saved rewards for player: " + playerName);
        File rewardsFile = new File(this.getDataFolder(), "offlineRewards.yml");
        FileConfiguration rewardsConfig = YamlConfiguration.loadConfiguration(rewardsFile);

        List<?> rawList = rewardsConfig.getList(playerName); // Pobierz surową listę
        List<ItemStack> existingRewards = new ArrayList<>();

        if (rawList != null) {
            for (Object obj : rawList) {
                if (obj instanceof ItemStack) { // Sprawdź, czy obiekt jest ItemStack
                    existingRewards.add((ItemStack) obj);
                }
            }
        } else {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: saveOfflineReward: No saved rewards for player: " + playerName);
        }

        existingRewards.addAll(rewardItems);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: saveOfflineReward: Rewards added for player: " + playerName);

        rewardsConfig.set(playerName, existingRewards);
        try {
            rewardsConfig.save(rewardsFile);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: saveOfflineReward: rewardsFile saved for player: " + playerName);
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "BetterElo: saveOfflineReward: Exception while saving rewards: " + e);
        }
    }
    public void notifyTopPlayers(String period) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo.notifyTopPlayers called with period=" + period);
        DecimalFormat df = new DecimalFormat("#.##");
        Duration fadeIn = Duration.ofMillis(500);  // czas pojawiania się
        Duration stay = Duration.ofSeconds(5);    // czas wyświetlania
        Duration fadeOut = Duration.ofMillis(500); // czas znikania
        Title.Times times = Title.Times.times(fadeIn, stay, fadeOut);
        Component rankingNotificationTileComponent = Component.text(ChatColor.GOLD + "" + ChatColor.BOLD + period.toUpperCase() + " ranking has ended!");
        Component rankingNotificationSubtileComponent;
        // Notify all players
        for (Player player : Bukkit.getOnlinePlayers()){
            pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2, "BetterElo.notifyTopPlayers." + period+" notifying "+player.getName());
            double points = dataManager.getPoints(player.getUniqueId().toString(),period);
            int rank = dataManager.getPlayerRank(player.getUniqueId().toString(),period);
            rankingNotificationSubtileComponent = Component.text(ChatColor.AQUA + "Your position: " + ChatColor.RED+""+ChatColor.BOLD+rank+". "+ChatColor.AQUA +"Your points: "+ ChatColor.RED+""+ChatColor.BOLD+points);
            Title killerTitle = Title.title(rankingNotificationTileComponent, rankingNotificationSubtileComponent, times);
            player.showTitle(killerTitle);
        }
    }
    public void notiyBannedPlayer(String bannedPlayer){
        Player player = Bukkit.getPlayer(bannedPlayer);
        DecimalFormat df = new DecimalFormat("#.##");

        Duration fadeIn = Duration.ofMillis(300);  // czas pojawiania się
        Duration stay = Duration.ofMillis(10000);    // czas wyświetlania
        Duration fadeOut = Duration.ofMillis(5000); // czas znikania
        Title.Times times = Title.Times.times(fadeIn, stay, fadeOut);
        Component BannedTitleComponent = Component.text(ChatColor.DARK_RED +"!!!"+ChatColor.BLACK+""+ChatColor.BOLD+"CHEATER"+ChatColor.DARK_RED +"!!!");
        Component BannedSubtitleComponent = Component.text(ChatColor.GREEN +"Buy unban on website "+ChatColor.GOLD+ChatColor.BOLD+" BetterBox.top" );
        // Notify the killer
        Title bannedTitle = Title.title(BannedTitleComponent,BannedSubtitleComponent,times);
        if (player !=null) {
            player.showTitle(bannedTitle);
        }else{
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "Banned player "+bannedPlayer+" is offline - cannot send notification");
        }

    }
    public void killAllCustomMobs() {

        int killedMobCount=0;
        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (entity.hasMetadata("CustomMob")) {
                    // Zabijamy niestandardowego zombiaka
                    entity.remove();
                    killedMobCount++;
                }
            }
        }
        pluginLogger.log(PluginLogger.LogLevel.INFO, "BetterElo.killAllCustomMobs killed "+killedMobCount+" custom mobs.");
    }
    public void removeAndKillAllCustomMobs() {
        for (CustomMobs.CustomMob customMob : customMobsMap.values()) {
            // Sprawdzanie, czy encja jest nadal żywa przed próbą jej zabicia
            if (customMob.entity != null && !customMob.entity.isDead()) {
                customMob.entity.remove(); // Usuwa encję z świata

            }
        }
        for (CustomMobs.CustomMob customMob : customMobsMap.values()) {
            // Sprawdzanie, czy encja jest nadal żywa przed próbą jej zabicia
            if (customMob.entity != null && !customMob.entity.isDead()) {
                customMob.entity.remove(); // Usuwa encję z świata

            }
        }
        customMobsMap.clear(); // Czyści mapę po usunięciu wszystkich encji
    }
    public void registerCustomMob(Entity entity, CustomMobs.CustomMob customMob) {
        pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "BetterElo.registerCustomMob calleed.   entity: "+entity+", customMob: "+customMob);
        schedulePercentageHealthRegeneration(customMob.entity, customMob.regenSeconds, customMob.regenPercent);
        customMobsMap.put(entity, customMob);
    }
    public void unregisterCustomMob(Entity entity) {
        pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "BetterElo.unregisterCustomMob calleed.   entity: "+entity);
        customMobsMap.remove(entity);
        if (mobTasks.containsKey(entity)) {
            mobTasks.get(entity).cancel(); // Anuluje zadanie
            mobTasks.remove(entity); // Usuwa referencję do zadania z mapy
        }
    }
    public void schedulePercentageHealthRegeneration(LivingEntity mob, int regenSeconds, double regenPercentage) {
        // Usuń istniejące zadanie regeneracji dla tego moba, jeśli istnieje
        if (mobTasks.containsKey(mob)) {
            mobTasks.get(mob).cancel();
        }

        // Utwórz nowe zadanie regeneracji
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - mob.getLastDamage() >= regenSeconds * 1000L) { // Przekształcenie sekund na milisekundy
                    double maxHealth = mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    double regenAmount = maxHealth * (regenPercentage / 100.0);
                    double newHealth = Math.min(mob.getHealth() + regenAmount, maxHealth);
                    mob.setHealth(newHealth);
                }
            }
        }.runTaskTimer(this, 0L, regenSeconds * 20L); // Uruchamia zadanie co regenSeconds sekund

        // Przechowuje zadanie dla tego moba
        mobTasks.put(mob, task);
    }

    // Opcjonalnie, metoda do anulowania zadania regeneracji dla moba
    public void cancelHealthRegeneration(LivingEntity mob) {
        if (mobTasks.containsKey(mob)) {
            mobTasks.get(mob).cancel();
            mobTasks.remove(mob);
        }
    }

    public CustomMobs.CustomMob getCustomMobFromEntity(Entity entity) {
        return customMobsMap.get(entity);
    }
    public void checkMobsExistence() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Użyj iteratora, aby uniknąć ConcurrentModificationException podczas usuwania
                Iterator<Map.Entry<Entity, CustomMobs.CustomMob>> iterator = customMobsMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Entity, CustomMobs.CustomMob> entry = iterator.next();
                    if (entry.getKey().isDead() || !entry.getKey().isValid()) {
                        pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "BetterElo.  mob "+entry.getKey()+" is dead or does not exists, removing from the list and from the spawner "+entry.getValue().spawnerName);
                        // Mob nie istnieje, więc możemy go usunąć z mapy
                        customMobs.decreaseMobCount(entry.getValue().spawnerName);
                        iterator.remove();
                        String spawnerName = entry.getValue().spawnerName;
                        customMobs.spawnedMobsMap.remove(entry.getKey().getUniqueId());


                        if(!entry.getKey().isValid()){
                            pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "BetterElo.checkMobsExistence  mob "+entry.getKey()+" does not exists! SpawnerName: "+spawnerName);
                            if (spawnerName != null) {

                                Map<String, CustomMobsFileManager.SpawnerData> spawnersData = customMobsFileManager.spawnersData;
                                CustomMobsFileManager.SpawnerData spawnerData = spawnersData.get(spawnerName);
                                pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "BetterElo.checkMobsExistence  mob "+entry.getKey()+" does not exists! spawnerData.spawnedMobCount: "+spawnerData.spawnedMobCount);
                                if (spawnerData.spawnedMobCount == 0) {
                                    pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "BetterElo.checkMobsExistence  resetting cooldown on spawner: "+spawnerName);
                                    customMobs.spawnerLastSpawnTimes.put(spawnerName, System.currentTimeMillis() - spawnerData.cooldown * 1000L);
                                }
                            }
                        }

                        // Tutaj możesz również zaimplementować logikę re-spawnu lub inne akcje
                    }
                }
            }
        }.runTaskTimer(this, 0L, 20L * 10); // Uruchom co 1 minutę (20 ticków = 1 sekunda)
    }
    public void addMobDefenseAttribute(ItemStack item, int value){
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo.addMobDefenseAttribute called with value: "+value);
        if (item != null) {
            if(!item.hasItemMeta()){
                item.setItemMeta(Bukkit.getItemFactory().getItemMeta(item.getType()));
            }
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
            dataContainer.set(mobDefenseKey, PersistentDataType.INTEGER, value);
            item.setItemMeta(meta);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo.addMobDefenseAttribute value "+value+" was added to the item "+item);
        }else{
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "BetterElo.addMobDefenseAttribute null item!"+item);
        }
    }
    public void addMobDamageAttribute(ItemStack item, String value){
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo.addMobDamageAttribute called with value: "+value);
        if (item != null) {
            if(!item.hasItemMeta()){
                item.setItemMeta(Bukkit.getItemFactory().getItemMeta(item.getType()));
            }
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
            dataContainer.set(mobDamageKey, PersistentDataType.STRING, value);
            item.setItemMeta(meta);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo.addMobDamageAttribute value "+value+" was added to the item "+item);
        }else{
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "BetterElo.addMobDamageAttribute null item!"+item);
        }
    }
    public void addAverageDamageAttribute(ItemStack item, int value){
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo.addAverageDamageAttribute called with value: "+value);
        if (item != null) {
            if(!item.hasItemMeta()){
                item.setItemMeta(Bukkit.getItemFactory().getItemMeta(item.getType()));
            }
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
            dataContainer.set(averageDamageKey, PersistentDataType.INTEGER, value);
            item.setItemMeta(meta);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo.addAverageDamageAttribute value "+value+" was added to the item "+item);
        }else{
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "BetterElo.addAverageDamageAttribute null item!"+item);
        }
    }
    public int[] getMobDamageAttribute(ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
            if (dataContainer.has(mobDamageKey, PersistentDataType.STRING)) {
                String damageRange = dataContainer.get(mobDamageKey, PersistentDataType.STRING);
                String[] parts = damageRange.split("-");
                int minDamage = Integer.parseInt(parts[0]);
                int maxDamage = Integer.parseInt(parts[1]);
                return new int[]{minDamage, maxDamage};
            }
        }
        return new int[]{0, 0};
    }

    public int getMobDefenseAttribute(List<ItemStack> wornItems) {
        int totalDefense = 0;

        for (ItemStack item : wornItems) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
                if (dataContainer.has(mobDefenseKey, PersistentDataType.INTEGER)) {
                    totalDefense += dataContainer.get(mobDefenseKey, PersistentDataType.INTEGER);
                }
            }
        }

        return totalDefense;
    }

    public int getAverageDamageAttribute(List<ItemStack> wornItems) {
        int totalDamage = 0;

        for (ItemStack item : wornItems) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
                if (dataContainer.has(averageDamageKey, PersistentDataType.INTEGER)) {
                    totalDamage += dataContainer.get(averageDamageKey, PersistentDataType.INTEGER);
                }
            }
        }

        return totalDamage;
    }
    public int getAverageDamageAttribute(ItemStack item) {
        int totalDamage = 0;

            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
                if (dataContainer.has(averageDamageKey, PersistentDataType.INTEGER)) {
                    totalDamage += dataContainer.get(averageDamageKey, PersistentDataType.INTEGER);
                }
            }

        return totalDamage;
    }
    public boolean hasMobDamageAttribute(ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
            if (dataContainer.has(mobDamageKey, PersistentDataType.STRING)) {
                return true;
            }
        }
        return false;
    }
    public boolean hasMobDefenseAttribute(ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
            if (dataContainer.has(mobDefenseKey, PersistentDataType.INTEGER)) {
                return true;
            }
        }
        return false;
    }
    public boolean hasAverageDamageAttribute(ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
            if (dataContainer.has(averageDamageKey, PersistentDataType.INTEGER)) {
                return true;
            }
        }
        return false;
    }

}
