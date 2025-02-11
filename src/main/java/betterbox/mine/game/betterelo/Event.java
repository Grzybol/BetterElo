package betterbox.mine.game.betterelo;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import io.papermc.paper.event.entity.EntityMoveEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.*;

public class  Event implements Listener {
    private final DataManager dataManager;
    private final JavaPlugin plugin;
    private final PluginLogger pluginLogger;
    private final BetterElo betterElo;
    private static Economy econ = null;
    private Map<String, Long> lastAttackTimes = new HashMap<>();
    private BetterRanksCheaters cheaters;
    private ExtendedConfigManager configManager;
    private HashMap<Player, Long> lastFireworkUsage = new HashMap<>();
    private HashMap<Player, Long> lastZephyrUsage = new HashMap<>();
    private HashMap<Player, Long> lastFlameUsage = new HashMap<>();
    private CustomMobs customMobs;
    private CustomMobsFileManager customMobsFileManager;
    private GuiManager guiManager;
    private FileRewardManager fileRewardManager;
    private int deathEventCounter;
    private final Random random = new Random();
    //public final long cooldownMillis = 1500; // 1.5s
    public Utils utils;
    private final Lang lang;
    private final MobNameUtil mobNameUtil;

    public Event(DataManager dataManager, PluginLogger pluginLogger, JavaPlugin plugin, BetterRanksCheaters cheaters, ExtendedConfigManager configManager, BetterElo betterElo, CustomMobs customMobs, FileRewardManager fileRewardManager, GuiManager guiManager, CustomMobsFileManager customMobsFileManager,Utils utils, Economy econ, Lang lang, MobNameUtil mobNameUtil) {
        this.dataManager = dataManager;
        this.econ = econ;
        this.mobNameUtil = mobNameUtil;
        this.fileRewardManager = fileRewardManager;
        this.lang = lang;
        this.pluginLogger = pluginLogger;
        this.betterElo = betterElo;
        this.plugin = plugin;
        this.cheaters = cheaters;
        this.configManager = configManager;
        this.customMobs = customMobs;
        this.customMobsFileManager = customMobsFileManager;
        this.guiManager = guiManager;
        this.utils = utils;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String transactionID = UUID.randomUUID().toString();
        Player player = event.getPlayer();
        String playerName = player.getName();
        String playerUUID = player.getUniqueId().toString();

        pluginLogger.log(PluginLogger.LogLevel.DEBUG,String.format("Event: onPlayerJoin: gracz %s wszedl na serwer", transactionID,playerName,playerUUID));

        if (!dataManager.playerExists(playerUUID,"main")) {
            pluginLogger.log(PluginLogger.LogLevel.INFO,"Player "+player.getName()+" doesn't exist in main database. Setting 1000 points", transactionID,playerName,playerUUID);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: onPlayerJoin: Player "+playerName+" does not exists in main database.", transactionID,playerName,playerUUID);
            dataManager.setPoints(playerUUID, 1000, "main");

        } else {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: onPlayerJoin: "+playerName+" Already in main database.", transactionID,playerName,playerUUID);
        }
        if (!dataManager.playerExists(playerUUID,"daily")) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: onPlayerJoin: Player "+playerName+" does not exists in daily database.", transactionID,playerName,playerUUID);
            dataManager.setPoints(playerUUID, 1000, "daily");

        } else {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: onPlayerJoin: "+playerName+" Already in daily database.", transactionID,playerName,playerUUID);
        }
        if (!dataManager.playerExists(playerUUID,"weekly")) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: onPlayerJoin: Player "+playerName+" does not exists in weekly database.", transactionID,playerName,playerUUID);
            dataManager.setPoints(playerUUID, 1000, "weekly");

        } else {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: onPlayerJoin: "+playerName+" Already in weekly database.", transactionID,playerName,playerUUID);
        }
        if (!dataManager.playerExists(playerUUID,"monthly")) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: onPlayerJoin: Player "+playerName+" does not exists in monthly database.", transactionID,playerName,playerUUID);
            dataManager.setPoints(playerUUID, 1000, "monthly");

        } else {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: onPlayerJoin: "+playerName+" Already in monthly database.", transactionID,playerName,playerUUID);
        }
        Utils.enableMoneyPickup(player);

    }

    private double handleKillEvent(String rankingType, Player victim, Player killer,String transactionID) {
        pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: handleKillEvent called with parameters: " + rankingType+" "+victim+" "+killer,transactionID);
        double pointsEarned = 0;

        if (killer != null && !killer.equals(victim)) {
            // Gracz zabił innego gracza
            double victimElo = getElo(victim.getUniqueId().toString(), rankingType);
            double killerElo = getElo(killer.getUniqueId().toString(), rankingType);
            double maxElo = getMaxElo(rankingType);
            double minElo = dataManager.getMinElo(rankingType);
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: handleKillEvent: rankingType: " + rankingType + " loaded variables: maxElo:" + maxElo + " minElo: " + minElo + " victimElo:" + victimElo + " victim name: " + victim.getName() + " killerElo:" + killerElo + " killer name: " + killer.getName(),transactionID);
            double basePoints = 100;
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: handleKillEvent calling calculatePointsEarned with parameters: basePoints " + basePoints + " killerElo " + killerElo + " victimElo " + victimElo + " maxElo " + maxElo + " minElo " + minElo,transactionID);
            pointsEarned = calculatePointsEarned(basePoints, killerElo, victimElo, maxElo, minElo);
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: handleKillEvent: pointsEarned: " + pointsEarned + "rankingType: " + rankingType,transactionID);

            // Zapisz informacje do bazy danych
            PlayerKillDatabase playerKillDatabase = new PlayerKillDatabase(pluginLogger);
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: handleKillEvent calling saveKillData",transactionID);
            playerKillDatabase.saveKillData(rankingType, victim.getName(), killer.getName(), pointsEarned, killerElo, victimElo);

            // Dodaj punkty graczowi, który zabił
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: handleKillEvent calling addPoints with parameters: " + killer.getUniqueId().toString() + " " + pointsEarned + " " + rankingType,transactionID);
            addPoints(killer, pointsEarned, rankingType);
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: handleKillEvent calling subtractPoints with parameters: " + victim.getUniqueId().toString() + " " + pointsEarned + " " + rankingType,transactionID);
            subtractPoints(victim, pointsEarned, rankingType);
            pluginLogger.log(PluginLogger.LogLevel.INFO, "BetterElo.Event.handleKillEvent: rankingType: " + rankingType + ", maxElo:" + maxElo + " minElo: " + minElo + " victimElo:" + victimElo + " victimName: " + victim.getName() + " killerElo:" + killerElo + " killerName: " + killer.getName()+", pointsEarned: "+pointsEarned,transactionID);
        }
        return pointsEarned;
    }
    private double handleMobKillEvent(String rankingType, CustomMobs.CustomMob victim, Player killer,String transactionID) {
        pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: handleKillEvent called with parameters: " + rankingType+" "+victim+" "+killer,transactionID);

            double pointsEarned = 0;
            double victimElo = victim.eloPoints;
            double eloMultiplier = victim.eloMultiplier;
            double killerElo = getElo(killer.getUniqueId().toString(), rankingType);
            double maxElo = getMaxElo(rankingType);
            double minElo = dataManager.getMinElo(rankingType);
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: handleKillEvent: rankingType: " + rankingType + ", loaded variables: maxElo:" + maxElo + ", minElo: " + minElo + ", victimElo:" + victimElo + ", eloMultiplier: "+eloMultiplier,transactionID);
            double basePoints = 100;
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: handleKillEvent calling calculatePointsEarned with parameters: basePoints " + basePoints + " killerElo " + killerElo + " victimElo " + victimElo + " maxElo " + maxElo + " minElo " + minElo,transactionID);
            pointsEarned = eloMultiplier * calculatePointsEarned(basePoints, killerElo, victimElo, maxElo, minElo);
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: handleKillEvent: pointsEarned: " + pointsEarned + "rankingType: " + rankingType,transactionID);
            addPoints(killer, pointsEarned, rankingType);
            pluginLogger.log(PluginLogger.LogLevel.INFO, "Event.handleMobKillEvent basePoints " + basePoints + " killerElo " + killerElo + " victimElo(mob) " + victimElo + " maxElo " + maxElo + " minElo " + minElo+", mobName: "+killer.getName()+" pointsEarned: "+pointsEarned,transactionID);
        return pointsEarned;
    }
    private void handleRankingPointsFromMobKill(Player killer, CustomMobs.CustomMob victim, String ranking,String transactionID) {
        if (dataManager.getPoints(killer.getUniqueId().toString(), ranking) - victim.eloPoints < 1000) {
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event.handleRankingPointsFromMobKill calling handleKillEvent with parameters: " + ranking + " " + victim + " " + killer,transactionID);
            double pointsEarned = handleMobKillEvent(ranking, victim, killer,transactionID);
            if (ranking.equals("main")) {
                notifyPlayerAboutPoints(killer, pointsEarned, victim.eloMultiplier, true);
            }
        } /*
        else {
            if (ranking.equals("main")) {
                killer.sendMessage(ChatColor.DARK_RED + lang.eloDifferenceTooBig);
            }
        }
        */

    }
    private void handleRankingPointsFromMobDeath(CustomMobs.CustomMob killer,Player victim, String ranking,String transactionID) {
        if (dataManager.getPoints(victim.getUniqueId().toString(), ranking) - killer.eloPoints < 1000) {
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event.handleRankingPointsFromMobKill calling handleKillEvent with parameters: " + ranking + " " + victim + " " + killer,transactionID);
            double pointsEarned = handleDeathFromCustomMobEvent(ranking, victim, killer,transactionID);
        }
    }
    private double handleDeathFromCustomMobEvent(String rankingType, Player victim, CustomMobs.CustomMob killer,String transactionID) {
        pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: handleKillEvent called with parameters: " + rankingType+" "+victim+" "+killer,transactionID);

        double pointsEarned = 0;
        double victimElo = getElo(victim.getUniqueId().toString(), rankingType);
        double eloMultiplier = killer.eloMultiplier;
        double killerElo = killer.eloPoints;
        double maxElo = getMaxElo(rankingType);
        double minElo = dataManager.getMinElo(rankingType);
        pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: handleKillEvent: rankingType: " + rankingType + ", loaded variables: maxElo:" + maxElo + ", minElo: " + minElo + ", victimElo:" + victimElo + ", eloMultiplier: "+eloMultiplier,transactionID);
        double basePoints = 100;
        pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: handleKillEvent calling calculatePointsEarned with parameters: basePoints " + basePoints + " killerElo " + killerElo + " victimElo " + victimElo + " maxElo " + maxElo + " minElo " + minElo,transactionID);
        pointsEarned = eloMultiplier * calculatePointsEarned(basePoints, killerElo, victimElo, maxElo, minElo);
        pluginLogger.log(PluginLogger.LogLevel.INFO, "BetterElo.Event.handleKillEvent: pointsEarned(lost): " + pointsEarned + "rankingType: " + rankingType+", victim: "+victim.getName()+", mob: "+killer.getMobName());
        subtractPoints(victim, pointsEarned, rankingType);
        return pointsEarned;
    }


    // Mapa do śledzenia ostatnich uderzeń
    private final Map<UUID, Long> lastHitTime = new HashMap<>();

    // Metoda do aktualizacji czasu ostatniego uderzenia
    public void updateLastHitTime(Player player) {
        //pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: updateLastHitTime saving "+player.getUniqueId()+" "+System.currentTimeMillis());
        lastHitTime.put(player.getUniqueId(), System.currentTimeMillis());
    }

    // Sprawdź, czy śmierć nastąpiła w wyniku walki
    private boolean deathDueToCombat(Player player) {
        pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: deathDueToCombat called");
        Long lastHit = lastHitTime.get(player.getUniqueId());
        return lastHit != null && (System.currentTimeMillis() - lastHit <= 10000); // 10 sekund
    }

    // Metoda do znalezienia ostatniego napastnika
    private Player getLastAttacker(Player victim) {
        pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: getLastAttacker called with parameters: " + victim);
        try {
            Player lastAttacker = null;
            long lastAttackTime = 0;
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: getLastAttacker: checking online players");
            // Iterowanie przez wszystkich graczy i znajdowanie tego, który ostatnio uderzył ofiarę
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                UUID playerId = player.getUniqueId();
                // Sprawdzamy, czy gracz uderzył ofiarę
                if (lastHitTime.containsKey(playerId)) {
                    long playerLastHitTime = lastHitTime.get(playerId);
                    // Sprawdzamy, czy to ostatnie uderzenie było wobec ofiary i czy było najświeższe
                    if (playerLastHitTime > lastAttackTime && !player.equals(victim)) {
                        lastAttacker = player;
                        lastAttackTime = playerLastHitTime;
                    }
                }
            }
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: getLastAttacker: lastAttacker " + lastAttacker);


            return lastAttacker;
        } catch (Exception e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Event: getLastAttacker exception: " + e.getMessage());
            return null;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        String transactionID = UUID.randomUUID().toString();
        Player victim = event.getEntity();
        if (victim.hasMetadata("handledDeath")) {
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event.onPlayerDeath event already handled! victim: "+victim.getName(),transactionID);
            return;
        }
        victim.setMetadata("handledDeath", new FixedMetadataValue(plugin, true));
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> victim.removeMetadata("handledDeath", plugin), 1L);
        pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event.handleKillEvent added handledDeath metadata to "+victim.getName(),transactionID);
        if (cheaters.getCheatersList().contains(victim.getName())) {
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: handleKillEvent: returning 0 points because either  " + victim + " " + cheaters.getCheatersList().contains(victim.getName()) + "  has CHEATER rank in BetterRanks.",transactionID);
            return;
        }
        if(!Utils.isEloAllowed(victim,victim.getLocation())){
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: onPlayerDeath noElo zone!",transactionID);
            //killer.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo] " + ChatColor.DARK_RED + "No elo reward in this zone!");
            return;
        }



        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        try {
            Player killer = victim.getKiller();
            String[] rankings = {"main", "daily", "weekly", "monthly"};
            boolean eventEnabled = betterElo.isEventEnabled;
            if (killer == null && deathDueToCombat(victim)) {
                pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event.onPlayerDeath: killer is null, deathDueToCombat(victim): " + deathDueToCombat(victim) + " victim: " + victim+", calling getLastAttacker(victim)",transactionID);
                    Player lastAttacker = getLastAttacker(victim);
                    pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: onPlayerDeath: lastAttacker " + lastAttacker,transactionID);
                    if (lastAttacker == null) {
                        return;
                    }else{
                        pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: onPlayerDeath setting killer=lastAttacker ",transactionID);
                        killer=lastAttacker;
                    }

            }else if(killer==null && !deathDueToCombat(victim)){
                EntityDamageEvent lastDamageCause = victim.getLastDamageCause();
                pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event.onPlayerDeath: killer is null, deathDueToCombat(victim)=false, lastDamageCause="+lastDamageCause.toString(),transactionID);
                if (lastDamageCause instanceof EntityDamageByEntityEvent) {
                    EntityDamageByEntityEvent damageByEntityEvent = (EntityDamageByEntityEvent) lastDamageCause;
                    pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event.onPlayerDeath: damageByEntityEvent="+damageByEntityEvent.toString(),transactionID);
                    Entity damager = damageByEntityEvent.getDamager();
                    pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event.onPlayerDeath: damager="+damager.toString());
                    // Sprawdzenie, czy damager jest projectilem
                    if (damager instanceof Projectile) {
                        Projectile projectile = (Projectile) damager;
                        ProjectileSource shooter = projectile.getShooter();

                        if (shooter instanceof Entity) {
                            damager = (Entity) shooter;
                            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event.onPlayerDeath: shooter="+damager.toString(),transactionID);
                        }
                    }
                    CustomMobs.CustomMob customMob =  betterElo.getCustomMobFromEntity(damager);
                    pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event.onPlayerDeath: customMob="+customMob.toString(),transactionID);
                    if (customMob!=null){
                        pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event.onPlayerDeath killer: "+customMob.mobName+", mobElo: "+customMob.eloPoints,transactionID);
                        for (String ranking : rankings) {
                            handleRankingPointsFromMobDeath(customMob, victim, ranking,transactionID);
                        }
                        if (eventEnabled) {
                            handleRankingPointsFromMobDeath(customMob, victim,  "event",transactionID);
                        }
                    }
                    return;

                }
            }
            if (cheaters.getCheatersList().contains(victim.getName()) || cheaters.getCheatersList().contains(killer.getName())) {
                pluginLogger.log(PluginLogger.LogLevel.INFO, "Event: handleKillEvent: returning 0 points because either  " + victim + " " + cheaters.getCheatersList().contains(victim.getName()) + " or " + killer + " " + cheaters.getCheatersList().contains(killer.getName()) + " has CHEATER rank in BetterRanks.",transactionID);
                return;
            }
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: onPlayerDeath: victim: " + victim + " killer: " + killer,transactionID);
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: onPlayerDeath calling deathDueToCombat(victim)",transactionID);

                String victimUUID = victim.getUniqueId().toString();
                String killerUUID = killer.getUniqueId().toString();

                for (String ranking : rankings) {
                    handleRankingPoints(killer, victim, killerUUID, victimUUID, ranking,transactionID);
                }

                if (eventEnabled) {
                    handleRankingPoints(killer, victim, killerUUID, victimUUID, "event",transactionID);
                }

                //PLAYER DEATH FROM CUSTOM MOB





        } catch (Exception e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Event: onPlayerDeath exception  " + e + " " + e.getMessage(),transactionID);
        }

        });
    }
    private void handleRankingPoints(Player killer, Player victim, String killerUUID, String victimUUID, String ranking,String transactionID) {
        if (dataManager.getPoints(killerUUID, ranking) - dataManager.getPoints(victimUUID, ranking) < 1000) {
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: onPlayerDeath calling handleKillEvent with parameters: " + ranking + " " + victim + " " + killer,transactionID);
            double pointsEarned = handleKillEvent(ranking, victim, killer,transactionID);
            if (ranking.equals("main")) {
                notifyPlayersAboutPoints(killer, victim, pointsEarned,transactionID);
            }
        } else {
            if (ranking.equals("main")) {
                killer.sendMessage(ChatColor.DARK_RED + lang.eloDifferenceTooBig);
            }
        }
    }

    private void notifyPlayersAboutPoints(Player killer, Player victim, double pointsEarned,String transactionID) {
        pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: notifyPlayersAboutPoints called with parameters: "+killer+" "+victim+" "+pointsEarned,transactionID);
        DecimalFormat df = new DecimalFormat("#.##");

        Duration fadeIn = Duration.ofMillis(300);  // czas pojawiania się
        Duration stay = Duration.ofMillis(900);    // czas wyświetlania
        Duration fadeOut = Duration.ofMillis(300); // czas znikania
        Title.Times times = Title.Times.times(fadeIn, stay, fadeOut);
        Component killerTitleComponent = Component.text(ChatColor.GREEN +""+ChatColor.BOLD+ "+"+df.format((double)Math.round(pointsEarned*100)/100)+" Elo");
        Component killerSubtitleComponent = Component.text(ChatColor.GOLD +"Victim: "+victim.getName());
        // Notify the killer
        Title killerTitle = Title.title(killerTitleComponent,killerSubtitleComponent,times);
        killer.showTitle(killerTitle);

        victim.sendMessage(lang.pointsLostMessage+ChatColor.RED + " " + ChatColor.BOLD +df.format(pointsEarned)+" Elo");
    }
    private void notifyPlayerAboutPoints(Player player, double pointsEarned, double blockReward, boolean adding) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: notifyPlayersAboutPoints called with parameters: "+player+" "+pointsEarned);
        DecimalFormat df = new DecimalFormat("#.##");

        Duration fadeIn = Duration.ofMillis(300);  // czas pojawiania się
        Duration stay = Duration.ofMillis(900);    // czas wyświetlania
        Duration fadeOut = Duration.ofMillis(300); // czas znikania
        Title.Times times = Title.Times.times(fadeIn, stay, fadeOut);
        Component killerTitleComponent;
        if (adding) {
            killerTitleComponent = Component.text(ChatColor.GREEN + "" + ChatColor.BOLD + "+" + df.format((double) Math.round(pointsEarned * 100) / 100) + " Elo");
        }else{
            killerTitleComponent = Component.text(ChatColor.DARK_RED + "" + ChatColor.BOLD + "-" + df.format((double) Math.round(pointsEarned * 100) / 100) + " Elo");
        }
        Component killerSubtitleComponent = Component.text(ChatColor.GOLD +lang.eloMultiplierMessage+blockReward);
        // Notify the killer
        Title killerTitle = Title.title(killerTitleComponent,killerSubtitleComponent,times);
        player.showTitle(killerTitle);
    }



    private double calculatePointsEarned(double base, double killerelo, double victimelo, double maxElo, double minElo) {
        pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT,"Event: calculatePointsEarned called with parameters : base "+base+" elo1 "+killerelo+" elo2 "+victimelo+" maxElo "+maxElo+" minElo "+minElo);
        double eloDifference = killerelo - victimelo;
        if(maxElo<victimelo){
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT,"Event.calculatePointsEarned maxElo<victimelo, setting maxElo=victimelo");
            maxElo=victimelo;
        }else if(maxElo<killerelo){
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT,"Event.calculatePointsEarned maxElo<killerelo, setting maxElo=killerelo");
            maxElo=killerelo;
        }
        if(minElo>victimelo){
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT,"Event.calculatePointsEarned minElo>victimelo, setting minElo=victimelo");
            minElo=victimelo;
        }else if(minElo>killerelo){
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT,"Event.calculatePointsEarned minElo>killerelo, setting minElo=killerelo");
            minElo=killerelo;
        }
        double normalizedDifference = eloDifference / (maxElo - minElo + 1);
        double points = base * (1 - normalizedDifference);
        pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT,"Event: calculatePointsEarned: eloDifference: "+eloDifference+" normalizedDifference: "+normalizedDifference+" points: "+points+" maxElo: "+maxElo+" minElo: "+minElo);
        pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT,"Event: calculatePointsEarned: PointsEarnedOut: "+(double)Math.round(points*100));
        pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT,"Event: calculatePointsEarned: PointsEarnedOut/100: "+(double)Math.round(points*100)/100);
        return points;
        //return (double)Math.round(points*100)/100;
    }
    private double calculatePointsEarnedFromBlock(double base, double playerElo,double blockReward, double maxElo, double minElo) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL4,"Event: calculatePointsEarnedFromBlock called with parameters : base "+base+" playerElo "+playerElo+" blockReward "+blockReward+" maxElo "+maxElo+" minElo "+minElo);
        double eloDifference = maxElo-minElo;
        double eloPlayerDiff = playerElo - minElo;
        if (eloDifference!=0) {
            double S = 1 - (eloPlayerDiff) / (eloDifference);
            double points =  base * blockReward * S;
            pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL4, "Event: calculatePointsEarnedFromBlock: eloDifference: "+eloDifference+", S: "+S+", points: "+points);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL4,"Event: calculatePointsEarnedFromBlock: PointsEarnedOut: "+(double)Math.round(points*100));
            pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL4,"Event: calculatePointsEarnedFromBlock: PointsEarnedOut/100: "+(double)Math.round(points*100)/100);
            return points;
            //return (double)Math.round(points*100)/100;
        }
        else {
            return 0;
        }
    }

    private void updatePoints(String playerUUID, double points, String rankingType, boolean isAdding) {
        double currentPoints = dataManager.getPoints(playerUUID, rankingType);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,String.format("Event: %sPoints: currentPoints: %sBefore: %s",
                (isAdding ? "add" : "subtract"), rankingType, currentPoints));
        currentPoints = isAdding ? currentPoints + points : currentPoints - points;
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,String.format("Event: %sPoints: currentPoints: %sAfter: %s",
                (isAdding ? "add" : "subtract"), rankingType, currentPoints));
        dataManager.setPoints(playerUUID, currentPoints, rankingType);
        //dataManager.setPoints(playerUUID, Math.round(currentPoints * 100) / 100.0, rankingType);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,String.format("Event: %sPoints: currentPoints: %s ranking saved", (isAdding ? "add" : "subtract"), rankingType));
    }


    public void addPoints(String playerUUID, double points, String rankingType) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL4,"Event: addPoints: playerUUID: "+player.getName()+" rankingType: "+rankingType+" points: "+points);
        //pluginLogger.log(PluginLogger.LogLevel.INFO,"Player "+player.getName() +" earned "+points+" in "+rankingType+" ranking");
        updatePoints(playerUUID, points, rankingType, true);
    }

    public void subtractPoints(String playerUUID, double points, String rankingType) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL4,"Event: subtractPoints: playerUUID: "+playerUUID+" rankingType: "+rankingType+" points: "+points);
        //pluginLogger.log(PluginLogger.LogLevel.INFO,"Player "+player.getName()+" lost "+points+" in "+rankingType+" ranking");
        updatePoints(playerUUID, points, rankingType, false);
    }
    public void addPoints(OfflinePlayer player, double points, String rankingType) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL4,"Event: addPoints: player: "+player.getName()+" rankingType: "+rankingType+" points: "+points);
        //pluginLogger.log(PluginLogger.LogLevel.INFO,"Player "+player.getName() +" earned "+points+" in "+rankingType+" ranking");
        updatePoints(player.getUniqueId().toString(), points, rankingType, true);
    }
    public void subtractPoints(OfflinePlayer player, double points, String rankingType) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL4,"Event: subtractPoints: player: "+player.getName()+" rankingType: "+rankingType+" points: "+points);
        //pluginLogger.log(PluginLogger.LogLevel.INFO,"Player "+player.getName()+" lost "+points+" in "+rankingType+" ranking");
        updatePoints(player.getUniqueId().toString(), points, rankingType, false);
    }

    private double getElo(String playerUUID, String rankingType) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: getElo: playerUUID: "+playerUUID+" rankingType: "+rankingType);
        return dataManager.getPoints(playerUUID, rankingType);
    }

    private double getMaxElo(String rankingType) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: getMaxElo: rankingType: "+rankingType);
        return dataManager.getMaxElo(rankingType);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        pluginLogger.log(PluginLogger.LogLevel.BLOCK_BREAK, "onBlockBreak called");
        Block block = event.getBlock();
        String blockType = block.getType().toString();

        if (configManager.getBlockRewards().containsKey(blockType)) {
            if (block.getMetadata("placed_by_player").stream().anyMatch(MetadataValue::asBoolean)) {
                pluginLogger.log(PluginLogger.LogLevel.BLOCK_BREAK, "onBlockBreak: block was placed by a player");
                return;
            }

            double blockReward = configManager.getBlockRewards().get(blockType);
            Player player = event.getPlayer();
            String uuid = player.getUniqueId().toString();
            double base = configManager.blockBase;

            if (!Utils.isEloAllowed(player, player.getLocation())) {
                //pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2, "Event: onPlayerDeath noElo zone!");
                //player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo] " + ChatColor.DARK_RED + "No elo reward in this zone!");
                return;
            }

            updatePlayerPoints(uuid, player, base, blockReward, "main");
            updatePlayerPoints(uuid, player, base, blockReward, "daily");
            updatePlayerPoints(uuid, player, base, blockReward, "weekly");
            updatePlayerPoints(uuid, player, base, blockReward, "monthly");

            if (betterElo.isEventEnabled) {
                updatePlayerPoints(uuid, player, base, blockReward, "event");
            }
        } else {
            pluginLogger.log(PluginLogger.LogLevel.BLOCK_BREAK, "Block " + blockType + " is not on the list");
        }
    }

    private void updatePlayerPoints(String uuid, Player player, double base, double blockReward, String ranking) {
        double playerElo = dataManager.getPoints(uuid, ranking);
        double pointsEarned = calculatePointsEarnedFromBlock(base, playerElo, blockReward, dataManager.getMaxElo(ranking), dataManager.getMinElo(ranking));
        addPoints(uuid, pointsEarned, ranking);
        pluginLogger.log(PluginLogger.LogLevel.BLOCK_BREAK, "Event: onBlockBreak: player: " + player.getName() + ", pointsEarned: " + pointsEarned + ", ranking " + ranking);

        if (ranking.equals("main") && pointsEarned > 0.001) {
            notifyPlayerAboutPoints(player, pointsEarned, blockReward, true);
        }
    }

    // Metoda do dodawania metadanych do bloku podczas stawiania przez gracza
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        pluginLogger.log(PluginLogger.LogLevel.BLOCK_PLACE,"Event.onBlockPlace: called");
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return; // Exit if the player is in Creative mode
        }
        try {
            Block block = event.getBlockPlaced();
            Player player = event.getPlayer();

            // Sprawdź, czy blok i gracz nie są nullami
            if (block == null || player == null) {
                return;
            }

            // Dodaj metadane do bloku informujące, że został postawiony przez gracza
            block.setMetadata("placed_by_player", new FixedMetadataValue(plugin, true));
        }catch (Exception e){
            pluginLogger.log(PluginLogger.LogLevel.ERROR,"Event.onBlockPlace: "+e.getMessage());
        }
    }
    private LivingEntity getTargetEntity(Player player) {
        // Prosta implementacja do uzyskania celu ataku gracza
        // Można użyć ray tracingu, aby określić, w co gracz celuje
        Vector direction = player.getEyeLocation().getDirection().normalize();
        for (int i = 0; i < 5; i++) { // Sprawdzenie w promieniu 5 bloków
            Vector targetPos = player.getEyeLocation().add(direction.clone().multiply(i)).toVector();
            for (LivingEntity entity : player.getWorld().getLivingEntities()) {
                if (entity.getLocation().toVector().distance(targetPos) < 1.0) { // Sprawdzenie odległości od celu
                    return entity;
                }
            }
        }
        return null;
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        String transactionID = UUID.randomUUID().toString();

        /*
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if(betterElo.hasMobDamageAttribute(event.getPlayer().getInventory().getItemInMainHand())) {
                Player player = event.getPlayer();
                LivingEntity target = getTargetEntity(player); // Musisz zaimplementować tę metodę do uzyskania celu
                if (target != null) {
                    double damage = 2.0; // Ustawienie wartości obrażeń
                    target.damage(damage, player);
                }
            }
        }

         */

        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event.onPlayerInteract called",transactionID);
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        int removeradius = 0;


        if (itemInHand != null && itemInHand.hasItemMeta()) {
            ItemMeta meta = itemInHand.getItemMeta();
            if (meta.hasDisplayName()) {
                String displayName = ChatColor.stripColor(meta.getDisplayName());

                if (displayName.equals("Coin") || displayName.equals("BetterCoin")) {
                    double amountToAdd = displayName.equals("Coin") ? 0.01 * itemInHand.getAmount() : 1.0 * itemInHand.getAmount();
                    EconomyResponse r = BetterElo.getEconomy().depositPlayer(player, amountToAdd);
                    if(r.transactionSuccess()) {
                        player.getInventory().setItemInMainHand(null);  // Usuwa item z ręki
                        player.sendMessage(ChatColor.GREEN +lang.moneyAddedMessage+ r.amount + "$");
                        pluginLogger.log(PluginLogger.LogLevel.INFO, "Event.onPlayerInteract: Added $" + r.amount + " to " + player.getName() + "'s account. New balance: $" + r.balance,transactionID,player.getName(), player.getUniqueId().toString(),amountToAdd);
                    } else {
                        player.sendMessage(ChatColor.DARK_RED+ "Transaction failed: " + r.errorMessage);
                        pluginLogger.log(PluginLogger.LogLevel.ERROR, "Event.onPlayerInteract: Transaction failed: " + r.errorMessage,transactionID);
                    }
                }
            }
        }
        /*
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            double flamethrowerCooldown = (double) (configManager.flamethrowerCooldown) /1000;
            if (canUseFlamethrower(player)) {
                if(hasFlamethrowerLore(player)) {
                    pluginLogger.log(PluginLogger.LogLevel.PLAYER_INTERACT, "Event.onPlayerInteract hasFlamethrowerLore passed");
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo] " + ChatColor.DARK_RED + "Cooldown " + flamethrowerCooldown + "s");
                    lastFlameUsage.put(player, System.currentTimeMillis());
                    return;
                }
            }

        }

         */
        pluginLogger.log(PluginLogger.LogLevel.PLAYER_INTERACT, "Event.checking canUseZephyr");
        if(canUseZephyr(player) && event.getAction() == Action.RIGHT_CLICK_AIR) {
            pluginLogger.log(PluginLogger.LogLevel.PLAYER_INTERACT, "Event.canUseZephyr  passed");
            hasZephyrLore(player);

        }


        if (hasAntywebLore(itemInHand) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            pluginLogger.log(PluginLogger.LogLevel.PLAYER_INTERACT,"Event.onPlayerInteract antywebcheck passed");
            removeradius = getAntywebRadius(itemInHand);
            pluginLogger.log(PluginLogger.LogLevel.PLAYER_INTERACT,"Event.onPlayerInteract removeradius "+removeradius);
            Location clickedBlockLocation = Objects.requireNonNull(event.getClickedBlock()).getLocation();
            double totalCost=0;
            double cost=configManager.antywebCost;
            // Iteruj po blokach w promieniu 3 od klikniętego bloku
            for (int x = -removeradius; x <= removeradius; x++) {
                for (int y = -removeradius; y <= removeradius; y++) {
                    for (int z = -removeradius; z <= removeradius; z++) {
                        Location location = clickedBlockLocation.clone().add(x, y, z);
                        Block block = location.getBlock();

                        // Sprawdź, czy blok to pajęczyna
                        if (block.getType() == Material.COBWEB) {

                            // Usuń pajęczynę
                            for (MetadataValue meta : block.getMetadata("placed_by_player")) {
                                if (meta.asBoolean()) {
                                    pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL4, "onPlayerInteract: cobweb was placed by a player");
                                    block.setType(Material.AIR);
                                    subtractPoints(player, cost, "main");
                                    subtractPoints(player, cost, "daily");
                                    subtractPoints(player, cost, "weekly");
                                    subtractPoints(player, cost, "monthly");
                                    if(betterElo.isEventEnabled){
                                        subtractPoints(player, cost, "event");
                                    }
                                    totalCost=totalCost+cost;
                                }

                            }

                        }
                    }
                }
            }
            if(totalCost>0){
                player.sendMessage(ChatColor.GREEN + lang.antywebMessage + ChatColor.RED + ChatColor.BOLD + totalCost);
            }
            return;
        }


        pluginLogger.log(PluginLogger.LogLevel.PLAYER_INTERACT,"Event.onPlayerInteract checking if its infinite firework");
        ItemStack item = event.getItem();


        double fireworkCooldown = (double) (configManager.fireworkCooldown) /1000;
        if (item != null && item.getType() == Material.FIREWORK_ROCKET) {
            if(event.getAction() != Action.RIGHT_CLICK_AIR) {
                event.setCancelled(true);
                return;
            }
            Location location = player.getLocation();
            Location blockBelowLocation = location.clone().subtract(0, 1, 0);
            boolean isNotOnGround = blockBelowLocation.getBlock().isPassable();

            ItemStack chestplate = player.getInventory().getChestplate();
            if(chestplate == null ){
                event.setCancelled(true);
                return;
            }
            if (chestplate.getType().toString().contains("ELYTRA") || hasElytraLore(chestplate)) {

                pluginLogger.log(PluginLogger.LogLevel.PLAYER_INTERACT,"Event.onPlayerInteract player is not wearing Elytra!");






                pluginLogger.log(PluginLogger.LogLevel.PLAYER_INTERACT, "Event.onPlayerInteract firework item check passed");
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasLore()) {
                    if (!canUseFirework(player)) {
                        event.setCancelled(true);
                        return;

                    }else{
                        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo] "+ChatColor.DARK_RED+"Cooldown "+fireworkCooldown+"s");
                    }
                    pluginLogger.log(PluginLogger.LogLevel.PLAYER_INTERACT, "Event.onPlayerInteract firework has lore check passed");
                    if (meta.getLore().contains("§6§lInfinite usage")) {// Gold bold "Infinite usage"
                        pluginLogger.log(PluginLogger.LogLevel.PLAYER_INTERACT, "Event.onPlayerInteract firework has formatted lore check passed");
                        if (isNotOnGround) {
                            lastFireworkUsage.put(player, System.currentTimeMillis());
                            pluginLogger.log(PluginLogger.LogLevel.PLAYER_INTERACT, "Event.onPlayerInteract isNotOnGround check passed");
                            event.setCancelled(true); // Cancel the event so the firework isn't consumed
                            FireworkMeta fireworkMeta = (FireworkMeta) item.getItemMeta();
                            int power = fireworkMeta.getPower();
                            launchFireworkEffect(event.getPlayer());
                            applyBoosterEffect(event.getPlayer(), power);
                        } else {
                            pluginLogger.log(PluginLogger.LogLevel.PLAYER_INTERACT, "Event.onPlayerInteract isNotOnGround check failed");
                            event.setCancelled(true);
                        }
                    }
                }
            }else{
                event.setCancelled(true);
                return;
            }
        }


    }
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        String transactionID = UUID.randomUUID().toString();
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (player.hasMetadata("handledPickup")) {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onItemPickup: event already handled! player: "+player.getName(),transactionID);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                player.removeMetadata("handledPickup", BetterElo.getInstance());
                            }
                        }.runTask(BetterElo.getInstance());
                    }
                }.runTaskLaterAsynchronously(BetterElo.getInstance(), 1L);
                return;
            }
            player.setMetadata("handledPickup", new FixedMetadataValue(plugin, true));
            ItemStack item = event.getItem().getItemStack();
            ItemMeta meta = item.getItemMeta();
            if (player.hasMetadata("addMoneyOnPickup") && !player.getMetadata("addMoneyOnPickup").get(0).asBoolean()) {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onItemPickup: addMoneyOnPickup is false, returning",transactionID);
                return; // Jeżeli funkcja jest wyłączona, przerwij obsługę eventu
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.removeMetadata("handledPickup", BetterElo.getInstance());
                        }
                    }.runTask(BetterElo.getInstance());
                }
            }.runTaskLaterAsynchronously(BetterElo.getInstance(), 1L);


            if (meta != null && meta.hasDisplayName()) {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onItemPickup: Item pickup event triggered. Player: " + player.getName(),transactionID);
                String displayName = ChatColor.stripColor(meta.getDisplayName());

                if (displayName.equals("Coin") || displayName.equals("BetterCoin")) {
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onItemPickup: Coin/BetterCoin pickup event triggered. Player: " + player.getName(),transactionID);
                    double amountToAdd = displayName.equals("Coin") ? 0.01 * item.getAmount() : 1.0 * item.getAmount();
                    EconomyResponse r = BetterElo.getEconomy().depositPlayer(player, amountToAdd);

                    if (r.transactionSuccess()) {
                        pluginLogger.log(PluginLogger.LogLevel.INFO, "Event.onItemPickup: Added $" + r.amount + " to " + player.getName() + "'s account. New balance: $" + r.balance,transactionID,player.getName(), player.getUniqueId().toString(),amountToAdd);
                        player.sendMessage(ChatColor.GREEN + lang.moneyAddedMessage + r.amount + "$");
                        event.getItem().remove();  // Usuwa item z ziemi
                        event.setCancelled(true);  // Zapobiega dodaniu itemu do ekwipunku gracza
                        //player.sendMessage("Added $" + r.amount + " to your account. New balance: $" + r.balance);
                    } else {
                        player.sendMessage("Transaction failed: " + r.errorMessage);
                        pluginLogger.log(PluginLogger.LogLevel.ERROR, "Event.onItemPickup: Transaction failed: " + r.errorMessage,transactionID);
                    }
                }
            }
        }
    }
    private static void launchFireworkEffect(Player player) {

        Location location = player.getLocation();
        player.getWorld().playSound(location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH,SoundCategory.AMBIENT, 1.0f, 1.0f);
    }
    private static void applyBoosterEffect(Player player,int power) {

        Vector velocity = player.getLocation().getDirection().multiply(power);
        player.setVelocity(velocity);
    }
    private static void applyZephyrEffect(Player player,int power) {

        Vector velocity = player.getLocation().getDirection().multiply(power);
        player.setVelocity(velocity);
    }
    public boolean hasElytraLore(ItemStack itemStack) {
        pluginLogger.log(PluginLogger.LogLevel.ELYTRA_CHECK,"Event.hasElytraLore called");
        if (itemStack == null || !itemStack.hasItemMeta()) {
            pluginLogger.log(PluginLogger.LogLevel.ELYTRA_CHECK,"Event.hasElytraLore itemmeta check failed");
            return false;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null || !itemMeta.hasLore()) {
            pluginLogger.log(PluginLogger.LogLevel.ELYTRA_CHECK,"Event.hasElytraLore lore check failed");
            return false;
        }

        for (String lore : itemMeta.getLore()) {
            pluginLogger.log(PluginLogger.LogLevel.ELYTRA_CHECK,"Event.hasElytraLore Elytra check triggered, checking lore and foramtting");
            if (lore != null && lore.contains("§6§lElytra effect")) {
               return true;
            }
        }

        return false;
    }
    public boolean hasZephyrLore(Player player) {
        pluginLogger.log(PluginLogger.LogLevel.ZEPHYR,"Event.hasZephyrLore called, player "+player.getName());
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack == null || !itemStack.hasItemMeta()) {
            pluginLogger.log(PluginLogger.LogLevel.ZEPHYR,"Event.hasZephyrLore itemmeta check failed");
            return false;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null || !itemMeta.hasLore()) {
            pluginLogger.log(PluginLogger.LogLevel.ZEPHYR,"Event.hasZephyrLore lore check failed");
            return false;
        }

        for (String lore : itemMeta.getLore()) {
            pluginLogger.log(PluginLogger.LogLevel.ZEPHYR,"Event.hasZephyrLore Zephyr check triggered, checking lore and foramtting");
            if (lore != null && lore.startsWith("§6§lZephyr")) {
                pluginLogger.log(PluginLogger.LogLevel.ZEPHYR,"Event.hasZephyrLore itemInHand: "+itemStack.displayName());
                if(player.getInventory().getChestplate()!=null){
                    ItemStack chestplate = player.getInventory().getChestplate();
                    try {
                        pluginLogger.log(PluginLogger.LogLevel.ZEPHYR, "Event.hasZephyrLore chestplate: " + chestplate.displayName());
                        if (chestplate.getType().toString().contains("ELYTRA") || hasElytraLore(chestplate)) {
                            pluginLogger.log(PluginLogger.LogLevel.ZEPHYR, "Event.hasZephyrLore player " + player.getName() + " is wearing Elytra! Aborting");
                            player.sendMessage(ChatColor.RED + lang.zephyrErrorMessage);
                            return false;
                        }
                    } catch (Exception e) {
                        pluginLogger.log(PluginLogger.LogLevel.ERROR, "Event.hasZephyrLore exception:  " + e.getMessage());
                    }
                }else{
                    pluginLogger.log(PluginLogger.LogLevel.ZEPHYR, "Event.hasZephyrLore player: "+player.getName()+" is not wearing chestplate.");
                }
                String[] parts = lore.split(" ");
                pluginLogger.log(PluginLogger.LogLevel.ZEPHYR,"Event.hasZephyrLore Zephyr power "+parts[1]);
                if (parts.length == 2){
                    int power = Integer.parseInt(parts[1]);
                    applyBoosterEffect(player,power);
                    pluginLogger.log(PluginLogger.LogLevel.ZEPHYR,"Event.hasZephyrLore adding player "+player.getName()+" to the lastZephyrUsage list");
                    lastZephyrUsage.put(player, System.currentTimeMillis());
                    // Stwórz nowe zadanie BukkitRunnable, które zatrzyma gracza po 0,75 sekundy
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // Ustawienie wektora prędkości na bardzo małą wartość w kierunku obecnym dla gracza, aby "wyzerować" ruch
                            player.setVelocity(new Vector(0, -0.0784000015258789, 0)); // Minimalna wartość, by gracze nie utknęli w powietrzu
                        }
                    }.runTaskLater(betterElo, 10L); // 15 ticków = 0,75 sekundy
                }
                return true;
            }
        }

        return false;
    }
    private boolean canUseZephyr(Player player) {
        pluginLogger.log(PluginLogger.LogLevel.ZEPHYR,"Event.canUseZephyr called");
        if (!lastZephyrUsage.containsKey(player)) {
            pluginLogger.log(PluginLogger.LogLevel.ZEPHYR,"Event.canUseZephyr PLAYER NOT LISTED");
            return true;
        }
        long lastUsage = lastZephyrUsage.get(player);
        long currentTime = System.currentTimeMillis();
        pluginLogger.log(PluginLogger.LogLevel.ZEPHYR,"Event.canUseZephyr lastUsage: "+lastUsage+" currentTime: "+currentTime);
        return (currentTime - lastUsage) >= configManager.zephyrCooldown;
    }
    public boolean hasAntywebLore(ItemStack itemStack) {
        pluginLogger.log(PluginLogger.LogLevel.ANTYWEB,"Event.hasAntywebLore called");
        if (itemStack == null || !itemStack.hasItemMeta()) {
            pluginLogger.log(PluginLogger.LogLevel.ANTYWEB,"Event.hasAntywebLore itemmeta check failed");
            return false;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null || !itemMeta.hasLore()) {
            pluginLogger.log(PluginLogger.LogLevel.ANTYWEB,"Event.hasAntywebLore lore check failed");
            return false;
        }

        for (String lore : itemMeta.getLore()) {
            pluginLogger.log(PluginLogger.LogLevel.ANTYWEB,"Event.hasAntywebLore Antyweb check triggered, checking lore");
            if (lore != null && lore.contains("Antyweb")) {
                pluginLogger.log(PluginLogger.LogLevel.ANTYWEB,"Event.hasAntywebLore Antyweb check triggered, checking formatting");
                // Sprawdź, czy lore zawiera słowo "Antyweb" i czy ma odpowiednie formatowanie
                String[] parts = lore.split(" ");
                if (parts.length == 2 && parts[0].equals(ChatColor.GOLD + "" + ChatColor.BOLD + "Antyweb")) {
                    try {
                        // Sprawdź, czy druga część to liczba całkowita
                        Integer.parseInt(parts[1]);
                        return true;
                    } catch (NumberFormatException ignored) {
                        pluginLogger.log(PluginLogger.LogLevel.ERROR,"Event.hasAntywebLore Antyweb value not INT!");
                    }
                }
            }
        }

        return false;
    }
    private boolean canUseFirework(Player player) {
        if (!lastFireworkUsage.containsKey(player)) {
            return true; // Gracz jeszcze nie używał fajerwerka
        }
        long lastUsage = lastFireworkUsage.get(player);
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastUsage) >= configManager.fireworkCooldown;
    }
    public Integer getAntywebRadius(ItemStack itemStack) {
        pluginLogger.log(PluginLogger.LogLevel.ANTYWEB,"Event.getAntywebRadius called");
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return null;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null || !itemMeta.hasLore()) {
            return null;
        }

        for (String lore : itemMeta.getLore()) {
            // Sprawdź, czy linia lore zawiera napis "Antyweb" i czy zawiera formatowanie
            pluginLogger.log(PluginLogger.LogLevel.ANTYWEB,"Event.getAntywebRadius Antyweb check triggered, checking lore");
            if (lore != null && lore.contains("Antyweb")) {
                pluginLogger.log(PluginLogger.LogLevel.ANTYWEB,"Event.getAntywebRadius Antyweb check triggered, checking formatting");
                // Sprawdź, czy lore zawiera słowo "Antyweb" i czy ma odpowiednie formatowanie
                String[] parts = lore.split(" ");
                if (parts.length == 2 && parts[0].equals(ChatColor.GOLD + "" + ChatColor.BOLD + "Antyweb")) {
                    // Wyodrębnij promień z linii lore
                    try {
                        return Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        // Jeśli nie można przekonwertować na liczbę, zwróć null
                        return null;
                    }
                }
            }
        }

        return null;
    }
    public boolean hasFlamethrowerLore(Player player) {
        pluginLogger.log(PluginLogger.LogLevel.FLAMETHROWER, "Event.hasFlamethrowerLore called");
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack == null || !itemStack.hasItemMeta()) {
            pluginLogger.log(PluginLogger.LogLevel.FLAMETHROWER, "Event.hasFlamethrowerLore itemmeta check failed");
            return false;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null || !itemMeta.hasLore()) {
            pluginLogger.log(PluginLogger.LogLevel.FLAMETHROWER, "Event.hasFlamethrowerLore lore check failed");
            return false;
        }

        for (String lore : itemMeta.getLore()) {
            pluginLogger.log(PluginLogger.LogLevel.FLAMETHROWER, "Event.hasFlamethrowerLore Flamethrower check triggered, checking lore");
            if (lore != null && lore.contains("Flamethrower")) {
                pluginLogger.log(PluginLogger.LogLevel.FLAMETHROWER, "Event.hasFlamethrowerLore Flamethrower check triggered, checking formatting");
                // Sprawdź, czy lore zawiera słowo "Flamethrower" i czy ma odpowiednie formatowanie
                String[] parts = lore.split(" ");
                if (parts.length == 2 && parts[0].equals(ChatColor.GOLD + "" + ChatColor.BOLD + "Flamethrower")) {
                    try {
                        // Sprawdź, czy druga część to format: X/Y
                        String[] range = parts[1].split("/");
                        if (range.length == 2) {
                            int diameter = Integer.parseInt(range[0]);
                            int rangeValue = Integer.parseInt(range[1]);

                            // Podpal graczy w okolicy
                            ignitePlayersInArea(player,diameter,rangeValue);
                            lastFlameUsage.put(player, System.currentTimeMillis());
                            return true;
                        } else {
                            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Event.hasFlamethrowerLore Lore value format incorrect!");
                        }
                    } catch (NumberFormatException ignored) {
                        pluginLogger.log(PluginLogger.LogLevel.ERROR, "Event.hasFlamethrowerLore Lore value not INT!");
                    }
                }
            }
        }

        return false;
    }
    public void ignitePlayersInArea(Player player, int diameter, int range) {
        pluginLogger.log(PluginLogger.LogLevel.FLAMETHROWER,"ignitePlayersInArea called player:"+player.getName()+" diameter:"+diameter+" range:"+range);
        // Pobierz blok, na który gracz aktualnie wskazuje
        Set<Material> transparent = new HashSet<>(Arrays.asList(Material.AIR, Material.WATER)); // Zdefiniuj przezroczyste bloki, które mają być ignorowane.
        Block clickedBlock = player.getTargetBlock(transparent,range);
        if (clickedBlock == null) {
            pluginLogger.log(PluginLogger.LogLevel.FLAMETHROWER, "No block in player's sight within range: " + range + ". IgnitePlayersInArea aborted for " + player.getName() + ".");
            return; // Jeśli gracz nie wskazuje na żaden blok, zakończ działanie funkcji
        }
        Location clickLocation = clickedBlock.getLocation();
        // Oblicz i zaloguj odległość od gracza rzucającego zaklęcie do klikniętego bloku
        double distanceFromCaster = clickLocation.distance(player.getLocation());
        pluginLogger.log(PluginLogger.LogLevel.FLAMETHROWER, "Event.ignitePlayersInArea called for player: " + player.getName() + " at click location: " + clickLocation.toString() + ", caster distance from click location: " + distanceFromCaster + ", with diameter: " + diameter + " and range: " + range);
        if(isPvPDeniedAtLocation(player,clickLocation)){
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo] "+ChatColor.DARK_RED+"Ypu cannot use that in non-pvp zones!");
            pluginLogger.log(PluginLogger.LogLevel.FLAMETHROWER, "PVP Denied");
            return;
        }
        // Iteruj przez wszystkich graczy online w tym samym świecie
        for (Player target : player.getWorld().getPlayers()) {
            pluginLogger.log(PluginLogger.LogLevel.FLAMETHROWER, "Checking player: " + target.getName() + " for potential ignition.");

            // Sprawdź, czy gracz znajduje się w obszarze podpalenia
            double distanceToTarget = target.getLocation().distance(clickLocation);
            if (distanceToTarget <= diameter && target!=player) {
                // Podpal gracza
                target.setFireTicks(20 * 5); // Podpal na 5 sekund
                //target.sendMessage(ChatColor.RED + "Zostałeś podpalony przez gracza " + player.getName() + "!");
                pluginLogger.log(PluginLogger.LogLevel.FLAMETHROWER, "Player " + target.getName() + " ignited within diameter. Distance from click location: " + distanceToTarget);
            } else {
                // Dodajemy logowanie dla sytuacji, gdy gracz jest poza zasięgiem podpalenia
                pluginLogger.log(PluginLogger.LogLevel.FLAMETHROWER, "Player " + target.getName() + " is outside the ignition diameter: " + diameter + ". Distance from click location: " + distanceToTarget);
            }
        }
    }




    private boolean canUseFlamethrower(Player player) {
        pluginLogger.log(PluginLogger.LogLevel.FLAMETHROWER, "Event.canUseFlamethrower called.  player: "+player.getName());
        if (!lastFlameUsage.containsKey(player)) {
            pluginLogger.log(PluginLogger.LogLevel.FLAMETHROWER, "Event.canUseFlamethrower  player "+player.getName()+" not on the list. return true");
            return true; // Gracz jeszcze nie używał fajerwerka
        }
        long lastUsage = lastFlameUsage.get(player);
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastUsage) >= configManager.fireworkCooldown;
    }

    public boolean isPvPDeniedAtLocation(Player player, Location location) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event.isPvPDeniedAtLocation called");
        try {
            com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(location.getWorld());

            RegionContainer container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(weWorld);

            if (regions == null) {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.isPvPDeniedAtLocation No regions in world.");
                return false;
            } else {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.isPvPDeniedAtLocation Found regions: " + regions);
            }

            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

            ApplicableRegionSet regionSet = regions.getApplicableRegions(BukkitAdapter.asBlockVector(location));

            StateFlag.State pvpState = regionSet.queryState(localPlayer, Flags.PVP);
            boolean isPvPDenied = pvpState == StateFlag.State.DENY;

            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.isPvPDeniedAtLocation PvP Denied at location: " + location + " is " + isPvPDenied);

            return isPvPDenied;
        } catch (Exception e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Event.isPvPDeniedAtLocation: "+e.toString());
            return false;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMobDeath(EntityDeathEvent event) {
        String transactionID = UUID.randomUUID().toString();
        //pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS,"CustomMobs.onMobDeath called");
        LivingEntity entity = event.getEntity();
        CustomMobs.CustomMob customMob;
        if (entity.hasMetadata("DeathHandled")) {
            pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.onMobDeath mobDeath already processed!",transactionID);
            return; // Zdarzenie śmierci zostało już obsłużone, więc nic nie rób
        }
        //CustomMobs.CustomMob customMob = entity;
        if (entity.hasMetadata("CustomMob")) {
            pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.onMobDeath CustomMob metadata check passed",transactionID);
            List<ItemStack> drops = event.getDrops();
            drops.clear(); // Usuwa standardowy drop, jeśli chcesz
            // Tutaj zakładamy, że niestandardowa nazwa moba jest kluczem do dropTable
            if (entity.hasMetadata("MobName")) {
                pluginLogger.log(PluginLogger.LogLevel.DROP, "Event.onMobDeath MobName check passed",transactionID);
                List<MetadataValue> values = entity.getMetadata("MobName");
                // Zakładając, że pierwsza wartość jest właściwą wartością dla twojego pluginu
                String mobName = values.get(0).asString();

                // Załadowanie dropTable dla tego moba
                //HashMap<Double, ItemStack> dropTable = fileRewardManager.loadCustomDrops(mobName);
                customMob =  betterElo.getCustomMobFromEntity(entity);
                if(customMob!=null)
                {


                    pluginLogger.log(PluginLogger.LogLevel.DROP, "Event.onMobDeath customMob.dropTable: "+customMob.dropTable,transactionID);
                    //HashMap<Double, ItemStack> dropTable = customMob.dropTable;
                    List<CustomMobsFileManager.DropItem> dropTable = customMob.dropTable;
                    pluginLogger.log(PluginLogger.LogLevel.DROP, "Event.onMobDeath dropTable: "+dropTable,transactionID);

                    // Iteracja przez dropTable i decydowanie, czy dodawać przedmiot
                    for (CustomMobsFileManager.DropItem dropItem : dropTable) {
                        double rolledCance = Math.random();
                        double dropChance = dropItem.getDropChance()/100;
                        if ( rolledCance< dropChance) { // entry.getKey() to szansa na drop
                            ItemStack item = dropItem.getItemStack();
                            ItemMeta meta = item.getItemMeta();
                            pluginLogger.log(PluginLogger.LogLevel.DROP, "Event.onMobDeath dropItem.isAvgDmgBonus(): "+dropItem.isAvgDmgBonus(),transactionID);
                            if (dropItem.isAvgDmgBonus()) {

                                int AvgDmgBonus = Utils.dropAverageDamage(transactionID);

                                List<String> lore = meta.getLore();
                                if (lore == null) {
                                    lore = new ArrayList<>();
                                }
                                lore.add(configManager.averageDamageLore+AvgDmgBonus+"%");
                                meta.setLore(lore);
                                pluginLogger.log(PluginLogger.LogLevel.DROP, "Event.onMobDeath item: "+item,transactionID);
                                item.setItemMeta(meta);
                                pluginLogger.log(PluginLogger.LogLevel.DROP, "Event.onMobDeath AvgDmgBonus: "+AvgDmgBonus+", hasAverageDamageAttribute(item):"+betterElo.hasAverageDamageAttribute(item),transactionID);
                                betterElo.addAverageDamageAttribute(item,AvgDmgBonus);
                                meta = item.getItemMeta();
                                pluginLogger.log(PluginLogger.LogLevel.DROP, "Event.onMobDeath AvgDmgBonus: "+AvgDmgBonus+", hasAverageDamageAttribute(item):"+betterElo.hasAverageDamageAttribute(item)+", getAverageDamageAttribute: "+betterElo.getAverageDamageAttribute(item),transactionID);

                            }


                            if(dropItem.hasmaxDamage()){
                                int maxDamage = betterElo.rollDamage(dropItem.getMaxDamage());
                                pluginLogger.log(PluginLogger.LogLevel.DROP, "Event.onMobDeath loadedMaxDamage: "+maxDamage,transactionID);
                                int minDamage = betterElo.rollDamage(maxDamage);
                                pluginLogger.log(PluginLogger.LogLevel.DROP, "Event.onMobDeath minDamage: "+minDamage+", maxDamage: "+maxDamage,transactionID);
                                List<String> lore = meta.getLore();
                                if (lore == null) {
                                    lore = new ArrayList<>();
                                }
                                lore.set(0,configManager.mobDamageLore+minDamage+"-"+maxDamage);
                                meta.setLore(lore);
                                item.setItemMeta(meta);
                                betterElo.addMobDamageAttributeNoLore(item,minDamage+"-"+maxDamage,transactionID);

                            }



                            pluginLogger.log(PluginLogger.LogLevel.DROP, "Event.onMobDeath item: "+item,transactionID);
                            drops.add(item);
                            pluginLogger.log(PluginLogger.LogLevel.DROP, "Event.onMobDeath drops.toArray()[0]: "+drops.toArray()[0],transactionID);

                            pluginLogger.log(PluginLogger.LogLevel.DROP, "Event.onMobDeath Added  item from dropTable to the drops. dropChance: "+dropChance+", rolledChance: "+rolledCance,transactionID);
                        }else{
                            pluginLogger.log(PluginLogger.LogLevel.DROP,"Event.onMobDeath Item from dropTable not added, chance failed. dropChance: "+dropChance+", rolledChance: "+rolledCance,transactionID);
                        }
                    }
                    pluginLogger.log(PluginLogger.LogLevel.INFO  , "Event.onMobDeath player: "+event.getEntity().getKiller().getName()+", mobName: "+mobName+", rolledDrop: "+Utils.formatDroppedItems(drops),transactionID);
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    try{
                        Player killer = event.getEntity().getKiller();
                        if(!Utils.isEloAllowed(killer,killer.getLocation())){
                            //pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: onPlayerDeath noElo zone!");
                            killer.sendMessage(ChatColor.RED+lang.noEloZoneMessage);
                            return;
                        }
                        String[] rankings = {"main", "daily", "weekly", "monthly"};
                        boolean eventEnabled = betterElo.isEventEnabled;

                        for (String ranking : rankings) {
                            handleRankingPointsFromMobKill(killer, customMob, ranking,transactionID);
                        }

                        if (eventEnabled) {
                            handleRankingPointsFromMobKill(killer, customMob,  "event",transactionID);
                        }
                    }catch (Exception e){
                        pluginLogger.log(PluginLogger.LogLevel.ERROR  , "Event.onMobDeath exception: "+e.getMessage(),transactionID);
                    }
                    });



                    pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS,"Event.onMobDeath customMob.mobName: "+customMob.mobName+", customMob.spawnerName: "+customMob.spawnerName,transactionID);
                    if(customMob.spawnerName!=null){

                        customMobs.decreaseMobCount(customMob.spawnerName);
                    }
                    betterElo.unregisterCustomMob(entity);
                }else {
                    pluginLogger.log(PluginLogger.LogLevel.WARNING,"Event.onMobDeath customMob object is null!",transactionID);
                }
            } else {
                customMob = null;
            }


            entity.setMetadata("DeathHandled", new FixedMetadataValue(plugin, true));
        } else {
            customMob = null;
        }


    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        String transactionID = UUID.randomUUID().toString();
        long startTime = System.nanoTime();
        Entity damagerEntity = event.getDamager();
        Entity victimEntity = event.getEntity();

        if (damagerEntity.hasMetadata("handledDamage")) {
            //pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onEntityDamageByEntity event already handled!",transactionID);
            return;
        }
        if (damagerEntity instanceof Player){
            damagerEntity.setMetadata("handledDamage", new FixedMetadataValue(plugin, true));

            new BukkitRunnable() {
                @Override
                public void run() {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            damagerEntity.removeMetadata("handledDamage", BetterElo.getInstance());
                        }
                    }.runTask(BetterElo.getInstance());
                }
            }.runTaskLaterAsynchronously(BetterElo.getInstance(), 1L);
        }
        if (damagerEntity instanceof Player && victimEntity instanceof Player && !event.isCancelled()) {
            Player damager = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            int averageDamageBonusPercent = betterElo.getAverageDamageAttribute(Utils.getPlayerEquippedItems((Player) event.getDamager()),transactionID);
            double totalDamage = event.getDamage() + event.getDamage() * ((double) averageDamageBonusPercent /100);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onEntityDamageByEntity event.getDamage(): "+event.getDamage()+", averageDamageBonusPercent: "+averageDamageBonusPercent+", totalDamage: "+totalDamage,transactionID);
            event.setDamage(totalDamage);
            updateLastHitTime(damager);
            updateLastHitTime(victim);
        }else if (victimEntity.hasMetadata("CustomMob") && damagerEntity instanceof Player){
            Player damager = (Player) event.getDamager();

            String key = damager.getUniqueId() + ":" + victimEntity.getUniqueId();
            long currentTime = System.currentTimeMillis();
            Long lastAttackTime = lastAttackTimes.get(key);

            String metadataKey = "last-attack-time-" + victimEntity.getUniqueId().toString();

            // Sprawdzenie, czy metadane istnieją i czy ostatni atak był mniej niż 10 ticków temu

            if (lastAttackTime != null && (currentTime - lastAttackTime) < 500) {
                event.setCancelled(true);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Player " + damager.getName() + " tried to hit too fast!",transactionID);
                return;
            }

            pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.onEntityDamageByEntity custom mob damage by player detected",transactionID);

            int[] damageRange = betterElo.getMobDamageAttribute(damager.getInventory().getItemInMainHand());
            int avgBonus = betterElo.getAverageDamageAttribute(Utils.getPlayerEquippedItems((Player) event.getDamager()),transactionID);
            pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.onEntityDamageByEntity damageRange: "+damageRange.toString()+", avgBonus: "+avgBonus,transactionID);
            customEntityDamageEvent(event,damageRange[0],damageRange[1],avgBonus,transactionID);
            removePlayerPlacedBlocksAsync(victimEntity,transactionID);

            lastAttackTimes.put(key, currentTime);

            Bukkit.getScheduler().runTaskLater(BetterElo.getInstance(), () -> {
                ((LivingEntity)event.getEntity()).setNoDamageTicks(0);
                //((Player) event.getEntity()).damage(1D);
            }, 1L);

        }else if (damagerEntity.hasMetadata("CustomMob") && victimEntity instanceof Player) {
            int customArmorBonus =betterElo.getMobDefenseAttribute(Utils.getPlayerEquippedItems((Player) victimEntity));
            pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.EntityDamageEvent getFinalDamage: "+event.getFinalDamage()+", customArmorBonus: "+customArmorBonus,transactionID);
            event.setDamage(event.getFinalDamage()*(1-(0.004*customArmorBonus)));

        }

        if (victimEntity instanceof LivingEntity) {
            LivingEntity entity = (LivingEntity) event.getEntity();
            if (entity.hasMetadata("CustomMob")) {
                CustomMobs.CustomMob customMob =  betterElo.getCustomMobFromEntity(entity);
                double eloPints = customMob.eloPoints;

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (!entity.isDead()) {
                                    //mobNameUtil.updateCustomMobName(entity,eloPints,transactionID);
                                    customMobs.updateCustomMobName(entity,eloPints,transactionID);
                                }
                            }
                        }.runTask(BetterElo.getInstance());
                    }
                }.runTaskLaterAsynchronously(BetterElo.getInstance(), 1L);

            }
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        double durationInMillis = duration / 1_000_000.0;
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onEntityDamageByEntity execution time: " + durationInMillis + " ms",transactionID);




    }
    public void customDamageHandling(Player damager, Player victim, double initialDamage) {
        int averageDamageBonusPercent = betterElo.getAverageDamageAttribute(Utils.getPlayerEquippedItems(damager),null);
        double totalDamage = initialDamage + initialDamage * averageDamageBonusPercent;
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "customDamageHandling initialDamage: " + initialDamage + ", averageDamageBonusPercent: " + averageDamageBonusPercent + ", totalDamage: " + totalDamage);
        victim.damage(totalDamage, damager);
        updateLastHitTime(damager);
        updateLastHitTime(victim);
    }
    public void customEntityDamageEvent(EntityDamageByEntityEvent event,int minDamage, int maxDamage, int averageDamageBonusPercent,String transactionID){
        long timer;
        double armor=1,defense=0;
        double averageDamage = (double) (minDamage + maxDamage) / 2; // Średnia wartość obrażeń
        int bonusDamage = (int) (averageDamage * (averageDamageBonusPercent / 100.0)); // Obliczenie bonusu
        double totalDamage = minDamage + random.nextInt(maxDamage - minDamage + 1) + bonusDamage; // Całkowite obrażenia
        pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.customEntityDamageEvent minDamage: "+minDamage+", maxDamage: "+maxDamage+", averageDamage: "+averageDamage+", averageDamageBonusPercent: "+averageDamageBonusPercent+", bonusDamage: "+bonusDamage,transactionID);
        CustomMobs.CustomMob customMob = null;
        Player player = (Player) event.getDamager();
        customMob =  betterElo.getCustomMobFromEntity(event.getEntity());
        if(customMob!=null)
        {
            defense = customMob.defense;
            armor = customMob.armor;
            pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.customEntityDamageEvent  from customMob object - defense: "+defense+", armor: "+armor,transactionID,player.getName(),player.getUniqueId().toString());

        }
        double defDmgReduction= (1-(0.01*defense));
        double finalDamage =event.getFinalDamage()+((totalDamage-armor)*defDmgReduction);
        if(finalDamage<=0)
            finalDamage=0;
        event.setDamage(finalDamage);
        pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.customEntityDamageEvent finalDamage: "+finalDamage+",  totalDamage: " + totalDamage+", bonusDamage: "+bonusDamage+", defDmgReduction(1-(0.01*defense)): "+defDmgReduction+", armor: "+armor,transactionID,player.getName(),player.getUniqueId().toString(),finalDamage);
    }
    public void removePlayerPlacedBlocksAsync(Entity entity,String transactionID) {
        // Asynchronicznie przygotowujesz dane
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<Block> blocksToRemove = new ArrayList<>();
            Block baseBlock = entity.getLocation().getBlock();

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 2; y++) {
                    for (int z = -1; z <= 1; z++) {
                        Block relBlock = baseBlock.getRelative(x, y, z);
                        if (relBlock.hasMetadata("placed_by_player")) {
                            blocksToRemove.add(relBlock);
                        }
                    }
                }
            }

            // Synchroniczne usuwanie bloków w głównym wątku
            Bukkit.getScheduler().runTask(plugin, () -> {
                for (Block block : blocksToRemove) {
                    block.setType(Material.AIR);
                    pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Removing player-placed block at " + block.getLocation(),transactionID);
                }
            });
        });
    }
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        // Sprawdza, czy gracz jest operatorem i czy trzyma shift
        if (player.isOp() && player.isSneaking()) {
            // Usuwa entity, jeżeli warunki są spełnione
            entity.remove();
            player.sendMessage("Usunięto entity!");
        }
    }
    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClick(InventoryClickEvent event) {
        String transactionID = UUID.randomUUID().toString();
        Player player = (Player) event.getWhoClicked();
        String playerName = player.getName();
        String playerUUID = player.getUniqueId().toString();
        if (player.hasMetadata("avgDmgRerolled")) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onInventoryClick avgDmgRerolled event already handled!",transactionID,playerName,playerUUID);
            return;
        }

        // Sprawdzamy, czy event to przeciąganie przedmiotu
        if (!event.isCancelled()) {
            ItemStack cursorItem = event.getCursor(); // przedmiot, który gracz trzyma myszką
            ItemStack destinationItem = event.getCurrentItem(); // przedmiot na miejscu docelowym w ekwipunku

            if (cursorItem != null && Utils.isEnchantItem(cursorItem)) {
                //pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onInventoryClick: Enchant item detected. cursorItem: " + cursorItem.getItemMeta().getLore() + ", hasMobDamageAttribute: " + betterElo.hasMobDamageAttribute(cursorItem) + ", hasAverageDamageAttribute: " + betterElo.hasAverageDamageAttribute(destinationItem) + ", hasMonDamage " + betterElo.hasMobDamageAttribute(destinationItem) + ", lore: " + destinationItem.getItemMeta().getLore(),transactionID,playerName,playerUUID);
                if (destinationItem != null && betterElo.hasMobDamageAttribute(destinationItem) && betterElo.hasAverageDamageAttribute(destinationItem)) {

                    pluginLogger.log(PluginLogger.LogLevel.REROLL, "Event.onInventoryClick: Mob damage and average damage bonus detected.",transactionID,playerName,playerUUID);
                        player.setMetadata("avgDmgRerolled", new FixedMetadataValue(plugin, true));
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                player.removeMetadata("avgDmgRerolled", plugin);
                            }
                        }, 1L);



                        int avgDmg = Utils.dropAverageDamage(transactionID);
                        ItemStack newDestination = destinationItem.clone();
                        pluginLogger.log(PluginLogger.LogLevel.REROLL, "Event.onInventoryClick: Rerolling average damage bonus. New bonus: " + avgDmg,transactionID,playerName,playerUUID,avgDmg);
                        utils.updateAverageDamage(newDestination, avgDmg, transactionID);
                        pluginLogger.log(PluginLogger.LogLevel.REROLL, "Event.onInventoryClick: Item updated with new average damage bonus. newItem avgbonus: " + betterElo.getAverageDamageAttribute(destinationItem),transactionID,playerName,playerUUID);

                    /*
                    event.setCursor(cursorItem);
                    if(cursorItem.getAmount() <=1) {
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onInventoryClick: Rerolling average damage bonus. cursorItem amount: " + cursorItem.getAmount(),transactionID,playerName,playerUUID);
                        event.setCursor(null);
                    }
                    else {
                        cursorItem.setAmount(cursorItem.getAmount() - 1);
                        event.setCursor(cursorItem);
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onInventoryClick: Rerolling average damage bonus. cursorItem amount: " + cursorItem.getAmount(),transactionID,playerName,playerUUID);
                    }

                     */

                    cursorItem.setAmount(cursorItem.getAmount() - 1);
                    event.setCursor(cursorItem.getAmount() > 0 ? cursorItem : null);
                    event.setCurrentItem(newDestination);
                    event.setCancelled(true);


                        // Informacja dla gracza
                        if (avgDmg >= 50) {
                            event.getWhoClicked().sendMessage(ChatColor.GREEN + configManager.newAvgBonusString + ChatColor.DARK_RED + "" + ChatColor.BOLD + avgDmg + "%");
                        } else if (avgDmg >= 40) {
                            event.getWhoClicked().sendMessage(ChatColor.GREEN + configManager.newAvgBonusString + ChatColor.RED + "" + ChatColor.BOLD + avgDmg + "%");
                        } else if (avgDmg >= 30) {
                            event.getWhoClicked().sendMessage(ChatColor.GREEN + configManager.newAvgBonusString + ChatColor.GOLD + "" + ChatColor.BOLD + avgDmg + "%");
                        } else if (avgDmg >= 20) {
                            event.getWhoClicked().sendMessage(ChatColor.GREEN + configManager.newAvgBonusString+ ChatColor.BOLD + avgDmg + "%");
                        }else {
                            event.getWhoClicked().sendMessage(ChatColor.GREEN + configManager.newAvgBonusString + avgDmg + "%");
                        }
                        return;

                }

            }
        }


        if(event.getCurrentItem()==null){
            return;
        }
        if(event.getCurrentItem().getType() == Material.GRAY_STAINED_GLASS_PANE || event.getCurrentItem().getType() == Material.GREEN_WOOL){
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onInventoryClick green wool or blank pane clicked, cancelling..",transactionID,playerName,playerUUID);
            event.setCancelled(true);

        }

        ItemStack currentItem = event.getCurrentItem();
        Inventory playerInventory = player.getInventory();
        ItemStack[] savedInventory = playerInventory.getContents();

        String title = event.getView().getTitle();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onInventoryClick called. title:"+title,transactionID,playerName,playerUUID);
        if (!Arrays.asList("Set Rewards", "Add Items", "Select Top", "AvgDmg bonus change").contains(title)) {
            return;
        }




        if (currentItem == null || currentItem.getType() == Material.AIR) {
            return;
        }
        Inventory inv;
        //String rewardTypeTemp = currentItem.getItemMeta().getDisplayName();
        switch (title) {
            case "Set Rewards":
                event.setCancelled(true);
                guiManager.periodType = currentItem.getItemMeta().getDisplayName();
                if (guiManager.periodType.equals("dropTable")) {
                    List<ItemStack> currentRewards = fileRewardManager.loadRewards();
                    inv = Bukkit.createInventory(null, 36, "Add Items");
                    currentRewards.forEach(inv::addItem);
                    guiManager.createItem(inv, Material.GREEN_WOOL, 35, "Save", "Save drop table");
                    player.openInventory(inv);
                    break;
                }
                guiManager.openSubGui(player);
                break;
            case "Select Top":
                event.setCancelled(true);
                guiManager.rewardType = currentItem.getItemMeta().getDisplayName();
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event.onInventoryClick: rewardType:" + guiManager.rewardType + " periodType:" + guiManager.periodType,transactionID,playerName,playerUUID);
                fileRewardManager.setRewardType(guiManager.periodType, guiManager.rewardType);
                List<ItemStack> currentRewards = fileRewardManager.loadRewards();
                inv = Bukkit.createInventory(null, 36, "Add Items");
                currentRewards.forEach(inv::addItem);
                guiManager.createItem(inv, Material.GREEN_WOOL, 35, "Save", "Save rewards");
                //createItem(inv, Material.ORANGE_WOOL, 34, "Reset", "Resetuj czas");
                //createItem(inv, Material.YELLOW_WOOL, 33, "Reedem", "Rozdaj nagrody");
                player.openInventory(inv);
                break;
            case "Add Items":
                //save button check
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onInventoryClick Add Items",transactionID,playerName,playerUUID);
                if (currentItem.getType() == Material.GREEN_WOOL && (event.getSlot() == 53)) {
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onInventoryClick Add Items - save called.",transactionID,playerName,playerUUID);
                    event.setCancelled(true);
                    Inventory inventory = event.getInventory();
                    List<ItemStack> itemsToSave = new ArrayList<>();
                    for (int i = 0; i < inventory.getSize(); i++) {
                        if (i == 53) { // Pomijamy slot przycisku "Save"
                            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onInventoryClick save button, skipping.",transactionID,playerName,playerUUID);
                            continue;
                        }
                        ItemStack item = inventory.getItem(i);
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onInventoryClick save: item: " + item,transactionID,playerName,playerUUID);
                        if (item != null && item.getType() != Material.AIR) {
                            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onInventoryClick no air save: item: " + item,transactionID,playerName,playerUUID);
                            itemsToSave.add(item);
                        }

                    }

                    String fileName = guiManager.periodType + "_" + guiManager.dropTable;

                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onInventoryClick guiManager.periodType=" + guiManager.periodType,transactionID,playerName,playerUUID);
                    if (guiManager.periodType.equals("dropTable")) {
                        fileName = guiManager.dropTable;
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onInventoryClick droptable: " + fileName,transactionID,playerName,playerUUID);
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onInventoryClick calling fileRewardManager.saveCustomDrops(" + fileName + ",itemsToSave)",transactionID,playerName,playerUUID);
                        fileRewardManager.saveCustomDrops(fileName, itemsToSave);
                    } else {
                        fileRewardManager.saveCustomDrops(fileName, itemsToSave);
                    }

                }
                if (currentItem.getType() == Material.GREEN_WOOL && event.getSlot() == 35) {
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onInventoryClick Add Items - save called.",transactionID,playerName,playerUUID);
                    event.setCancelled(true);
                    Inventory inventory = event.getInventory();
                    List<ItemStack> itemsToSave = new ArrayList<>();
                    for (int i = 0; i < inventory.getSize(); i++) {
                        if (i != 35) { // Pomijamy slot przycisku "Save"
                            ItemStack item = inventory.getItem(i);
                            if (item != null && item.getType() != Material.AIR) {
                                itemsToSave.add(item);
                            }
                        }
                    }

                    String fileName = guiManager.periodType + "_" + guiManager.rewardType;
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onInventoryClick calling fileRewardManager.saveRewards(" + fileName + ",itemsToSave)",transactionID,playerName,playerUUID);
                    fileRewardManager.saveRewards(fileName, itemsToSave);

                }
                break;
            case "AvgDmg bonus change":
                pluginLogger.log(PluginLogger.LogLevel.REROLL, "Event.onInventoryClick Average Damage bonus re-roll",transactionID,playerName,playerUUID);

                if (currentItem.getType() == Material.GREEN_WOOL && event.getSlot() == 5) {
                    playerInventory.setContents(savedInventory);
                    event.setCancelled(true);
                    pluginLogger.log(PluginLogger.LogLevel.REROLL, "Event.onInventoryClick Average Damage bonus re-roll clicked",transactionID,playerName,playerUUID);
                    Inventory inventory = event.getInventory();
                    ItemStack item0 = inventory.getItem(3);
                    if (item0 != null && item0.hasItemMeta()) {
                        pluginLogger.log(PluginLogger.LogLevel.REROLL, "Event.onInventoryClick reroll, betterElo.hasMobDamageAttribute(item0): "+betterElo.hasMobDamageAttribute(item0)+", betterElo.hasAverageDamageAttribute(item0): "+betterElo.hasAverageDamageAttribute(item0),transactionID,playerName,playerUUID);
                        ItemStack result = item0.clone();
                        ItemMeta resultMeta = result.getItemMeta();
                        List<String> lore = new ArrayList<>(resultMeta.getLore());
                        if (betterElo.hasMobDamageAttribute(item0) && betterElo.hasAverageDamageAttribute(item0)) {
                            if (Utils.checkAndRemoveEnchantItem(player)) {
                                int avgDmg = Utils.dropAverageDamage(transactionID);
                                betterElo.addAverageDamageAttribute(result, avgDmg);
                                resultMeta = result.getItemMeta();
                                pluginLogger.log(PluginLogger.LogLevel.REROLL, "Event.onInventoryClick reroll, player paid, re-rolling...",transactionID,playerName,playerUUID);
                                for (int i = 0; i < lore.size(); i++) {
                                    pluginLogger.log(PluginLogger.LogLevel.REROLL, "Event.onInventoryClick lore i="+i,transactionID,playerName,playerUUID);
                                    //if (destinationItem != null && betterElo.hasMobDamageAttribute(destinationItem) && betterElo.hasAverageDamageAttribute(destinationItem))
                                    if (lore.get(i).contains(configManager.averageDamageLore)) {
                                        pluginLogger.log(PluginLogger.LogLevel.REROLL, "Event.onInventoryClick lore i="+i+" contains Average Damage, setting avgDmg: "+avgDmg,transactionID,playerName,playerUUID);
                                        lore.set(i,configManager.averageDamageLore  + avgDmg + "%");
                                    }
                                }
                            }
                            player.setMetadata("avgDmgRerolled", new FixedMetadataValue(plugin, true));
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    player.removeMetadata("avgDmgRerolled", plugin);
                                }
                            }, 1L);


                            resultMeta.setLore(lore);
                            result.setItemMeta(resultMeta);
                            inventory.setItem(3, result);
                            pluginLogger.log(PluginLogger.LogLevel.REROLL, "Event.onInventoryClick result placed back in slot 3",transactionID,playerName,playerUUID);
                        }
                        pluginLogger.log(PluginLogger.LogLevel.REROLL, "Event.onInventoryClick reroll, player has no money for the re-roll.",transactionID,playerName,playerUUID);


                        ItemStack greenWoolItem = inventory.getItem(5);

                        if (greenWoolItem != null && greenWoolItem.hasItemMeta()) {
                            ItemMeta greenWoolMeta = greenWoolItem.getItemMeta();
                            List<String> greenWoolLore = greenWoolMeta.hasLore() ? new ArrayList<>(greenWoolMeta.getLore()) : new ArrayList<>();
                            String avgDmgLine = lore.stream().filter(line -> line.contains(configManager.averageDamageLore)).findFirst().orElse(configManager.averageDamageLore+"+0%");

                            // Ustalanie indeksów dla "current bonus:" i wartości
                            int bonusIndex = -1;
                            for (int i = 0; i < greenWoolLore.size(); i++) {
                                if (greenWoolLore.get(i).equals(configManager.currentBonusString)) {
                                    bonusIndex = i;
                                    break;
                                }
                            }

                            if (bonusIndex != -1 && bonusIndex + 1 < greenWoolLore.size()) {
                                // Aktualizujemy istniejącą wartość jeśli jest miejsce w lore
                                greenWoolLore.set(bonusIndex + 1, "<" + avgDmgLine + ">");
                            } else if (bonusIndex == -1) {
                                // Dodajemy nowe linie jeśli "current bonus:" nie istnieje
                                greenWoolLore.add(configManager.currentBonusString);
                                greenWoolLore.add("<" + avgDmgLine + ">");
                            } else {
                                // Jeśli "current bonus:" jest na końcu listy, dodajemy wartość
                                greenWoolLore.add("<" + avgDmgLine + ">");
                            }

                            greenWoolMeta.setLore(greenWoolLore);
                            greenWoolItem.setItemMeta(greenWoolMeta);
                            inventory.setItem(5, greenWoolItem);


                        }
                    }
                }
                break;
        }


    }


    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Inventory closedInventory = event.getInventory();

        // Check if the closed inventory is the same one we're interested in
        if (event.getView().getTitle().equalsIgnoreCase("AvgDmg bonus change")) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onInventoryClose: Checking items in closed GUI");

            ItemStack itemInSlot0 = closedInventory.getItem(3);
            if (itemInSlot0 != null) {
                ItemMeta meta = itemInSlot0.getItemMeta();
                //if (meta.getLore().stream().anyMatch(line -> line.contains("Average Damage"))) {
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onInventoryClose: Item with 'Average damage' found in slot 0");

                    // Optional: Directly give back the item to the player's inventory
                    if (player.getInventory().addItem(itemInSlot0).size() == 0) {
                        // Item successfully added to player's inventory
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onInventoryClose: Item returned to player inventory");
                        closedInventory.clear(3);  // Clear the slot after returning item
                    } else {
                        // Inventory full, drop item at player's location
                        player.getWorld().dropItem(player.getLocation(), itemInSlot0);
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onInventoryClose: Inventory full, item dropped at player's location");
                        closedInventory.clear(3);  // Clear the slot
                    }
                //}
            }
        }
    }
    @EventHandler
    public void onEntityMove(EntityMoveEvent event) {
        Entity entity = event.getEntity();
        CustomMobs.CustomMob customMob = null;
        customMob = betterElo.getCustomMobFromEntity(entity);

        if (customMob != null) {
            Location entityLocation = entity.getLocation();
            if (customMob.spawnerName!=null) {
                String spawnerLocationString = customMobsFileManager.getSpawnerLocation(customMob.spawnerName);
                Location spawnerLocation = customMobs.getLocationFromString(spawnerLocationString);
                int maxDistance = customMobsFileManager.getMaxDistance(customMob.spawnerName);
                if(maxDistance==0){
                    maxDistance=20;
                }
                if (spawnerLocation==null){
                    return;
                }
                if (entityLocation.distance(spawnerLocation) > maxDistance) {
                    // Teleportacja entity z powrotem do spawnerLocation
                    pluginLogger.log(PluginLogger.LogLevel.SPAWNERS, "Event.onEntityMove teleporting mob: "+customMob.mobName);
                    while (spawnerLocation.getBlock().getType() != Material.AIR) {
                        spawnerLocation.add(0, 1, 0); // Zwiększ y o 1
                        if (spawnerLocation.getBlockY() > spawnerLocation.getWorld().getMaxHeight()) {
                            // Jeśli przekraczamy maksymalną wysokość, przerwij pętlę, aby uniknąć pętli nieskończonej
                            System.out.println("Reached the top of the world without finding an AIR block.");
                            break;
                        }
                    }
                    spawnerLocation.add(0, 1, 0);
                    entity.teleport(spawnerLocation);
                }
            }




        }
    }





}

