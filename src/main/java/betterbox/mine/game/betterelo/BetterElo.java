package betterbox.mine.game.betterelo;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
public final class BetterElo extends JavaPlugin {
    private PluginLogger pluginLogger;
    private DataManager dataManager;
    public int eventDuration;
    public boolean isEventEnabled;
    public String eventUnit;
    private Placeholders placeholders;
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
    @Override
    public void onEnable() {
        // Inicjalizacja PluginLoggera
        Set<PluginLogger.LogLevel> defaultLogLevels = EnumSet.of(PluginLogger.LogLevel.INFO,PluginLogger.LogLevel.DEBUG, PluginLogger.LogLevel.WARNING, PluginLogger.LogLevel.ERROR);
        pluginLogger = new PluginLogger(getDataFolder().getAbsolutePath(), defaultLogLevels,this);
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
        guiManager = new GuiManager(fileRewardManager, pluginLogger, this, dataManager);
        getServer().getPluginManager().registerEvents(guiManager, this);
        // Inicjalizacja Placeholders
        placeholders = new Placeholders(dataManager, this);
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholders.register();
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterElo: onEnable: Placeholders zostały zarejestrowane w PlaceholderAPI.");
        } else {
            pluginLogger.log(PluginLogger.LogLevel.WARNING,"BetterElo: onEnable: Warning: PlaceholderAPI not found, placeholders will NOT be available.");
        }
        // Rejestracja komendy
        betterRanksCheaters = new BetterRanksCheaters(this,pluginLogger);
        CheaterCheckScheduler cheaterCheckScheduler = new CheaterCheckScheduler(this, betterRanksCheaters, getServer().getScheduler(), pluginLogger);
        // Rejestracja listenera eventów
        event = new Event(dataManager, pluginLogger,this,betterRanksCheaters,configManager,this);
        getServer().getPluginManager().registerEvents(event, this);
        getCommand("be").setExecutor(new BetterEloCommand(this, dataManager, guiManager, pluginLogger, this, configManager,event,PKDB));
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterElo: onEnable: Plugin BetterElo został włączony pomyślnie.");
        // Inicjalizacja RewardManagera (kod z konstruktora RewardManager)
        rewardStates.put("daily", true);
        rewardStates.put("weekly", true);
        rewardStates.put("monthly", true);
        // Inicjalizacja nagród i ich harmonogramów (kod z metody onEnable z klasy RewardManager)
        loadRewards();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterElo: onEnable: Planowanie nagród dziennych...");
        scheduleRewards("daily", TimeUnit.DAYS.toMillis(1), false);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterElo: onEnable: Planowanie nagród tygodniowych...");
        scheduleRewards("weekly", TimeUnit.DAYS.toMillis(7), false);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterElo: onEnable: Planowanie nagród miesięcznych...");
        scheduleRewards("monthly", 0, true);

        File dataFolder = this.getDataFolder();
        File databaseFile = new File(dataFolder, "database.txt");
        WebRankingServer server = new WebRankingServer(databaseFile.getAbsolutePath(), 39378,pluginLogger);

        server.startServer();
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

    }
    @Override
    public void onDisable() {
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
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: scheduleRewards: period: "+period+" periodMillis: "+periodMillis+" LastScheduledTime: "+lastScheduledTime);
        if (useNextMonthTime) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(lastScheduledTime);
            calendar.add(Calendar.MONTH, 1);
            delay = (calendar.getTimeInMillis() - System.currentTimeMillis()) / 1000 * 20;
        } else {
            delay = (periodMillis - (System.currentTimeMillis() - lastScheduledTime)) / 1000 * 20;
        }

        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: scheduleRewards: period: "+period+" periodMillis: "+periodMillis+" Computed delay: " + delay);

        if (delay < 0) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: scheduleRewards: Negative delay detected, starting rewardAndReschedule");
            rewardAndReschedule(period, periodMillis, useNextMonthTime);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: scheduleRewards: BukkitRunnable: rewardAndReschedule done");
            return;
        }
        //saveConfig();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (rewardStates.get(period)) {
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: scheduleRewards: BukkitRunnable: starting rewardAndReschedule");
                    rewardAndReschedule(period, periodMillis, useNextMonthTime);
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: scheduleRewards: BukkitRunnable: rewardAndReschedule done");
                }
            }
        }.runTaskLater(this, delay);
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
                            if (player.getInventory().firstEmpty() != -1) {
                                player.getInventory().addItem(rewardItem);
                                pluginLogger.log(PluginLogger.LogLevel.INFO, "BetterElo: rewardTopPlayers: Nagradzanie gracza: " + player.getName());
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
                //updateLastScheduledTime(rewardType);
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

}
