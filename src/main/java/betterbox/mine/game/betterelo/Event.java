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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.*;

public class  Event implements Listener {
    private final DataManager dataManager;
    private final JavaPlugin plugin;
    private final PluginLogger pluginLogger;
    private final BetterElo betterElo;
    private BetterRanksCheaters cheaters;
    private ExtendedConfigManager configManager;
    private HashMap<Player, Long> lastFireworkUsage = new HashMap<>();
    private HashMap<Player, Long> lastZephyrUsage = new HashMap<>();
    private HashMap<Player, Long> lastFlameUsage = new HashMap<>();
    private CustomMobs customMobs;
    private FileRewardManager fileRewardManager;
    private final Random random = new Random();
    //public final long cooldownMillis = 1500; // 1.5s

    public Event(DataManager dataManager, PluginLogger pluginLogger, JavaPlugin plugin, BetterRanksCheaters cheaters, ExtendedConfigManager configManager, BetterElo betterElo, CustomMobs customMobs, FileRewardManager fileRewardManager) {
        this.dataManager = dataManager;
        this.fileRewardManager = fileRewardManager;
        this.pluginLogger = pluginLogger;
        this.betterElo = betterElo;
        this.plugin = plugin;
        this.cheaters = cheaters;
        this.configManager = configManager;
        this.customMobs = customMobs;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        String playerUUID = player.getUniqueId().toString();

        pluginLogger.log(PluginLogger.LogLevel.DEBUG,String.format("Event: onPlayerJoin: gracz %s wszedl na serwer", playerName));

        if (!dataManager.playerExists(playerUUID,"main")) {
            pluginLogger.log(PluginLogger.LogLevel.INFO,"Player "+player.getName()+" doesn't exist in main database. Setting 1000 points");
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: onPlayerJoin: Player "+playerName+" does not exists in main database.");
            dataManager.setPoints(playerUUID, 1000, "main");

        } else {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: onPlayerJoin: "+playerName+" Already in main database.");
        }
        if (!dataManager.playerExists(playerUUID,"daily")) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: onPlayerJoin: Player "+playerName+" does not exists in daily database.");
            dataManager.setPoints(playerUUID, 1000, "daily");

        } else {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: onPlayerJoin: "+playerName+" Already in daily database.");
        }
        if (!dataManager.playerExists(playerUUID,"weekly")) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: onPlayerJoin: Player "+playerName+" does not exists in weekly database.");
            dataManager.setPoints(playerUUID, 1000, "weekly");

        } else {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: onPlayerJoin: "+playerName+" Already in weekly database.");
        }
        if (!dataManager.playerExists(playerUUID,"monthly")) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: onPlayerJoin: Player "+playerName+" does not exists in monthly database.");
            dataManager.setPoints(playerUUID, 1000, "monthly");

        } else {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: onPlayerJoin: "+playerName+" Already in monthly database.");
        }
    }

    private double handleKillEvent(String rankingType, Player victim, Player killer) {
        pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: handleKillEvent called with parameters: " + rankingType+" "+victim+" "+killer);
        double pointsEarned = 0;

        if (killer != null && !killer.equals(victim)) {
            // Gracz zabił innego gracza
            double victimElo = getElo(victim.getUniqueId().toString(), rankingType);
            double killerElo = getElo(killer.getUniqueId().toString(), rankingType);
            double maxElo = getMaxElo(rankingType);
            double minElo = dataManager.getMinElo(rankingType);
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: handleKillEvent: rankingType: " + rankingType + " loaded variables: maxElo:" + maxElo + " minElo: " + minElo + " victimElo:" + victimElo + " victim name: " + victim.getName() + " killerElo:" + killerElo + " killer name: " + killer.getName());
            double basePoints = 100;
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: handleKillEvent calling calculatePointsEarned with parameters: basePoints " + basePoints + " killerElo " + killerElo + " victimElo " + victimElo + " maxElo " + maxElo + " minElo " + minElo);
            pointsEarned = calculatePointsEarned(basePoints, killerElo, victimElo, maxElo, minElo);
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: handleKillEvent: pointsEarned: " + pointsEarned + "rankingType: " + rankingType);

            // Zapisz informacje do bazy danych
            PlayerKillDatabase playerKillDatabase = new PlayerKillDatabase(pluginLogger);
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: handleKillEvent calling saveKillData");
            playerKillDatabase.saveKillData(rankingType, victim.getName(), killer.getName(), pointsEarned, killerElo, victimElo);

            // Dodaj punkty graczowi, który zabił
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: handleKillEvent calling addPoints with parameters: " + killer.getUniqueId().toString() + " " + pointsEarned + " " + rankingType);
            addPoints(killer, pointsEarned, rankingType);
            pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: handleKillEvent calling subtractPoints with parameters: " + victim.getUniqueId().toString() + " " + pointsEarned + " " + rankingType);
            subtractPoints(victim, pointsEarned, rankingType);
        }
        return pointsEarned;
    }


    // Mapa do śledzenia ostatnich uderzeń
    private final Map<UUID, Long> lastHitTime = new HashMap<>();

    // Metoda do aktualizacji czasu ostatniego uderzenia
    public void updateLastHitTime(Player player) {
        pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: updateLastHitTime called");
        pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: updateLastHitTime saving "+player.getUniqueId()+" "+System.currentTimeMillis());
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

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        try {
            Player victim = event.getEntity();
            Player killer = victim.getKiller();
            if (!cheaters.getCheatersList().contains(victim.getName()) && !cheaters.getCheatersList().contains(killer.getName())) {
                pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: onPlayerDeath: victim: " + victim + " killer: " + killer);
                pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: onPlayerDeath calling deathDueToCombat(victim)");
                if (killer == null && deathDueToCombat(victim)) {
                    pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: onPlayerDeath killer is null");
                    pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: onPlayerDeath: deathDueToCombat(victim): " + deathDueToCombat(victim) + " victim: " + victim);
                    // Znajdź ostatniego gracza, który uderzył ofiarę
                    pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: onPlayerDeath calling getLastAttacker(victim)");
                    Player lastAttacker = getLastAttacker(victim);
                    pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: onPlayerDeath: lastAttacker " + lastAttacker);

                    if (lastAttacker == null) {
                        /*
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: onPlayerDeath: calling handleKillEvent" + lastAttacker);
                        // Oblicz punkty zdobyte/wtracone w wyniku "wirtualnej" walki
                        double pointsEarned = handleKillEvent("main", victim, lastAttacker);
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: onPlayerDeath calling handleKillEvent with parameters: daily " + victim + " " + lastAttacker);
                        handleKillEvent("daily", victim, lastAttacker);
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: onPlayerDeath calling handleKillEvent with parameters: weekly " + victim + " " + lastAttacker);
                        handleKillEvent("weekly", victim, lastAttacker);
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: onPlayerDeath calling handleKillEvent with parameters: monthly " + victim + " " + lastAttacker);
                        handleKillEvent("monthly", victim, lastAttacker);
                        if(betterElo.isEventEnabled){
                            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onPlayerDeath calling handleKillEvent with parameters: event " + victim + " " + lastAttacker);
                            handleKillEvent("event", victim, lastAttacker);
                        }



                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: onPlayerDeath calling notifyPlayersAboutPoints(lastAttacker, victim, pointsEarned)");
                        // Powiadom graczy o zmianie punktów
                        notifyPlayersAboutPoints(lastAttacker, victim, pointsEarned);

                         */
                        return;
                    }

                    //return;
                }
                if(!isEloAllowed(victim,victim.getLocation())){
                    pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: onPlayerDeath noElo zone!");
                    killer.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo] " + ChatColor.DARK_RED + "No elo reward in this zone!");
                    return;
                }
                String victimUUID = victim.getUniqueId().toString();
                String killerUUID = killer.getUniqueId().toString();
                if (dataManager.getPoints(killerUUID, "main") - dataManager.getPoints(victimUUID, "main") < 1000) {
                    pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: onPlayerDeath calling handleKillEvent with parameters: main " + victim + " " + killer);
                    double pointsEarned = handleKillEvent("main", victim, killer);
                    assert killer != null;
                    notifyPlayersAboutPoints(killer, victim, pointsEarned);
                } else {
                    killer.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo] " + ChatColor.DARK_RED + "Your Elo difference in the Main ranking is too big! No reward for this one.");
                }
                if (dataManager.getPoints(killerUUID, "daily") - dataManager.getPoints(victimUUID, "daily") < 1000) {
                    pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: onPlayerDeath calling handleKillEvent with parameters: daily " + victim + " " + killer);
                    handleKillEvent("daily", victim, killer);
                } else {
                    killer.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo] " + ChatColor.DARK_RED + "Your Elo difference in the Daily ranking is too big! No reward for this one.");
                }
                if (dataManager.getPoints(killerUUID, "weekly") - dataManager.getPoints(victimUUID, "weekly") < 1000) {
                    pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: onPlayerDeath calling handleKillEvent with parameters: weekly " + victim + " " + killer);
                    handleKillEvent("weekly", victim, killer);
                } else {
                    killer.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo] " + ChatColor.DARK_RED + "Your Elo difference in the Weekly ranking is too big! No reward for this one.");
                }
                if (dataManager.getPoints(killerUUID, "monthly") - dataManager.getPoints(victimUUID, "monthly") < 1000) {
                    pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: onPlayerDeath calling handleKillEvent with parameters: monthly " + victim + " " + killer);
                    handleKillEvent("monthly", victim, killer);
                } else {
                    killer.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo] " + ChatColor.DARK_RED + "Your Elo difference in the Monthly ranking is too big! No reward for this one.");
                }
                if(betterElo.isEventEnabled) {
                    if (dataManager.getPoints(killerUUID, "event") - dataManager.getPoints(victimUUID, "event") < 1000) {
                        pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: onPlayerDeath calling handleKillEvent with parameters: event " + victim + " " + killer);
                        handleKillEvent("event", victim, killer);
                    } else {
                        killer.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo] " + ChatColor.DARK_RED + "Your Elo difference in the Event ranking is too big! No reward for this one.");
                    }
                }


            } else {
                pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: handleKillEvent: returning 0 points because either  " + victim + " " + cheaters.getCheatersList().contains(victim.getName()) + " or " + killer + " " + cheaters.getCheatersList().contains(killer.getName()) + " has CHEATER rank in BetterRanks.");
            }
        } catch (Exception e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Event: onPlayerDeath exception  " + e + " " + e.getMessage());
        }
    });
}

    private void notifyPlayersAboutPoints(Player killer, Player victim, double pointsEarned) {
        pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: notifyPlayersAboutPoints called with parameters: "+killer+" "+victim+" "+pointsEarned);
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

        victim.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.RED +  "You have lost "+ChatColor.DARK_RED + "" + ChatColor.BOLD +df.format(pointsEarned)+" Elo");
    }
    private void notifyPlayerAboutPoints(Player player, double pointsEarned, Double blockReward) {
        pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: notifyPlayersAboutPoints called with parameters: "+player+" "+pointsEarned);
        DecimalFormat df = new DecimalFormat("#.##");

        Duration fadeIn = Duration.ofMillis(300);  // czas pojawiania się
        Duration stay = Duration.ofMillis(900);    // czas wyświetlania
        Duration fadeOut = Duration.ofMillis(300); // czas znikania
        Title.Times times = Title.Times.times(fadeIn, stay, fadeOut);
        Component killerTitleComponent = Component.text(ChatColor.GREEN +""+ChatColor.BOLD+ "+"+df.format((double)Math.round(pointsEarned*100)/100)+" Elo");
        Component killerSubtitleComponent = Component.text(ChatColor.GOLD +"Block multiplier: x"+blockReward);
        // Notify the killer
        Title killerTitle = Title.title(killerTitleComponent,killerSubtitleComponent,times);
        player.showTitle(killerTitle);
    }



    private double calculatePointsEarned(double base, double killerelo, double victimelo, double maxElo, double minElo) {
        pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT,"Event: calculatePointsEarned called with parameters : base "+base+" elo1 "+killerelo+" elo2 "+victimelo+" maxElo "+maxElo+" minElo "+minElo);
        double eloDifference = killerelo - victimelo;
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
        pluginLogger.log(PluginLogger.LogLevel.BLOCK_BREAK,"onBlockBreak called");
        Block block = event.getBlock();

        // Sprawdź, czy blok zniszczony przez gracza znajduje się na liście nagród
            String blockType = block.getType().toString();

            if (configManager.getBlockRewards().containsKey(blockType)) {
                for (MetadataValue meta : block.getMetadata("placed_by_player")) {
                    if (meta.asBoolean()) {
                        pluginLogger.log(PluginLogger.LogLevel.BLOCK_BREAK,"onBlockBreak: block was placed by a player");
                        return;
                    }
                }
                double blockReward = configManager.getBlockRewards().get(blockType);
                Player player = event.getPlayer();
                String uuid = player.getUniqueId().toString();
                double base = configManager.blockBase;

                double playerElo = dataManager.getPoints(uuid,"main");
                double pointsEarnedMain = calculatePointsEarnedFromBlock(base,playerElo,blockReward, dataManager.getMaxElo("main"), dataManager.getMinElo("main"));
                addPoints(uuid,pointsEarnedMain,"main");
                pluginLogger.log(PluginLogger.LogLevel.BLOCK_BREAK,"Event: onBlockBreak: player: "+player.getName()+", blockType: "+blockType+", pointsEarned: "+pointsEarnedMain+", ranking main");

                playerElo = dataManager.getPoints(uuid,"daily");
                double pointsEarned = calculatePointsEarnedFromBlock(base,playerElo,blockReward, dataManager.getMaxElo("daily"), dataManager.getMinElo("daily"));
                addPoints(uuid,pointsEarned,"daily");
                pluginLogger.log(PluginLogger.LogLevel.BLOCK_BREAK,"Event: onBlockBreak: player: "+player.getName()+", blockType: "+blockType+", pointsEarned: "+pointsEarned+", ranking daily");

                playerElo = dataManager.getPoints(uuid,"weekly");
                pointsEarned = calculatePointsEarnedFromBlock(base,playerElo,blockReward, dataManager.getMaxElo("weekly"), dataManager.getMinElo("weekly"));
                addPoints(uuid,pointsEarned,"weekly");
                pluginLogger.log(PluginLogger.LogLevel.BLOCK_BREAK,"Event: onBlockBreak: player: "+player.getName()+", blockType: "+blockType+", pointsEarned: "+pointsEarned+", ranking weekly");

                playerElo = dataManager.getPoints(uuid,"monthly");
                pointsEarned = calculatePointsEarnedFromBlock(base,playerElo,blockReward, dataManager.getMaxElo("monthly"), dataManager.getMinElo("monthly"));
                addPoints(uuid,pointsEarned,"monthly");
                pluginLogger.log(PluginLogger.LogLevel.BLOCK_BREAK,"Event: onBlockBreak: player: "+player.getName()+", blockType: "+blockType+", pointsEarned: "+pointsEarned+", ranking monthly");
                if(betterElo.isEventEnabled){
                    playerElo = dataManager.getPoints(uuid,"event");
                    pointsEarned = calculatePointsEarnedFromBlock(base,playerElo,blockReward, dataManager.getMaxElo("event"), dataManager.getMinElo("event"));
                    addPoints(uuid,pointsEarned,"event");
                    pluginLogger.log(PluginLogger.LogLevel.BLOCK_BREAK,"Event: onBlockBreak: player: "+player.getName()+", blockType: "+blockType+", pointsEarned: "+pointsEarned+", ranking event");
                }
                if(pointsEarnedMain>0.001) {
                    notifyPlayerAboutPoints(player, pointsEarnedMain, blockReward);
                }
            }
            else{
                pluginLogger.log(PluginLogger.LogLevel.BLOCK_BREAK,"Block "+blockType+" is not on the list");
            }

    }

    // Metoda do dodawania metadanych do bloku podczas stawiania przez gracza
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        pluginLogger.log(PluginLogger.LogLevel.BLOCK_PLACE,"Event.onBlockPlace: called");
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
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        pluginLogger.log(PluginLogger.LogLevel.PLAYER_INTERACT,"Event.onPlayerInteract called");
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        int removeradius = 0;


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
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo] " + ChatColor.AQUA + "Elo cost for removing webs: " + ChatColor.DARK_RED + ChatColor.BOLD + totalCost);
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
                            player.sendMessage((ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo] " + ChatColor.DARK_RED + "You cannot use Zephyr while wearing Elytra!"));
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
    public boolean isEloAllowed(Player player, Location location) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.isEloAllowed called");
        try {
            com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(location.getWorld());

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(weWorld);

            if (regions == null) {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.isEloAllowed No regions in world.");
                return true; // Jeśli nie ma regionów, domyślnie zezwalaj na Elo
            } else {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.isEloAllowed Found regions: " + regions);
            }

            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

            ApplicableRegionSet regionSet = regions.getApplicableRegions(BukkitAdapter.asBlockVector(location));

            // Zmieniamy sprawdzanie z flagi PVP na Twoją niestandardową flagę noElo
            StateFlag.State EloState = regionSet.queryState(localPlayer, BetterElo.IS_ELO_ALLOWED);
            boolean isEloAllowed = EloState != StateFlag.State.DENY; // Jeśli flaga noElo jest ustawiona na DENY, Elo nie jest dozwolone

            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.isEloAllowed Elo Allowed at location: " + location + " is " + isEloAllowed);

            return isEloAllowed;
        } catch (Exception e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Event.isEloAllowed: " + e.toString());
            return false;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMobDeath(EntityDeathEvent event) {
        //pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS,"CustomMobs.onMobDeath called");
        LivingEntity entity = event.getEntity();
        CustomMobs.CustomMob customMob = null;
        if (entity.hasMetadata("DeathHandled")) {
            pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.onMobDeath mobDeath already processed!");
            return; // Zdarzenie śmierci zostało już obsłużone, więc nic nie rób
        }
        //CustomMobs.CustomMob customMob = entity;
        if (entity.hasMetadata("CustomMob")) {
            pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.onMobDeath CustomMob metadata check passed");
            List<ItemStack> drops = event.getDrops();
            drops.clear(); // Usuwa standardowy drop, jeśli chcesz
            // Tutaj zakładamy, że niestandardowa nazwa moba jest kluczem do dropTable
            if (entity.hasMetadata("MobName")) {
                pluginLogger.log(PluginLogger.LogLevel.DROP, "Event.onMobDeath MobName check passed");
                List<MetadataValue> values = entity.getMetadata("MobName");
                // Zakładając, że pierwsza wartość jest właściwą wartością dla twojego pluginu
                String mobName = values.get(0).asString();

                // Załadowanie dropTable dla tego moba
                //HashMap<Double, ItemStack> dropTable = fileRewardManager.loadCustomDrops(mobName);
                customMob =  betterElo.getCustomMobFromEntity(entity);
                if(customMob!=null)
                {
                    pluginLogger.log(PluginLogger.LogLevel.DROP, "Event.onMobDeath customMob.dropTable: "+customMob.dropTable);
                    //HashMap<Double, ItemStack> dropTable = customMob.dropTable;
                    List<CustomMobsFileManager.DropItem> dropTable = customMob.dropTable;
                    pluginLogger.log(PluginLogger.LogLevel.DROP, "Event.onMobDeath dropTable: "+dropTable);

                    // Iteracja przez dropTable i decydowanie, czy dodawać przedmiot
                    for (CustomMobsFileManager.DropItem dropItem : dropTable) {
                        double rolledCance = Math.random();
                        double dropChance = dropItem.getDropChance()/100;
                        if ( rolledCance< dropChance) { // entry.getKey() to szansa na drop
                            ItemStack item = dropItem.getItemStack();
                            ItemMeta meta = item.getItemMeta();
                            if (meta.hasLore()) {
                                List<String> lore = meta.getLore();
                                for (int i = 0; i < lore.size(); i++) {
                                    // Sprawdź, czy linia zawiera szukany tekst
                                    pluginLogger.log(PluginLogger.LogLevel.DROP, "Event.onMobDeath dropItem.isAvgDmgBonus(): "+dropItem.isAvgDmgBonus());
                                    if (dropItem.isAvgDmgBonus()) {
                                        // Zastąp znalezioną linię nowym tekstem
                                        String AvgDmgBonus = CustomMobs.dropAverageDamage();
                                        lore.set(i, AvgDmgBonus);
                                        pluginLogger.log(PluginLogger.LogLevel.DROP, "Event.onMobDeath Added AvgDmgBonus: "+AvgDmgBonus);
                                        break; // Przerwij pętlę, jeśli chcesz zastąpić tylko pierwsze wystąpienie

                                    }
                                }
                                pluginLogger.log(PluginLogger.LogLevel.DROP, "Event.onMobDeath lore to save: "+lore);
                                // Zapisz zmodyfikowane lore z powrotem do metadanych przedmiotu
                                meta.setLore(lore);
                                item.setItemMeta(meta);
                            }
                            drops.add(item);
                            pluginLogger.log(PluginLogger.LogLevel.DROP, "Event.onMobDeath Added  item from dropTable to the drops. dropChance: "+dropChance+", rolledChance: "+rolledCance);
                        }else{
                            pluginLogger.log(PluginLogger.LogLevel.DROP,"Event.onMobDeath Item from dropTable not added, chance failed. dropChance: "+dropChance+", rolledChance: "+rolledCance);
                        }
                    }

                    pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS,"Event.onMobDeath customMob.mobName: "+customMob.mobName+", customMob.spawnerName: "+customMob.spawnerName);
                    if(customMob.spawnerName!=null){

                        customMobs.decreaseMobCount(customMob.spawnerName);
                    }
                }else {
                    pluginLogger.log(PluginLogger.LogLevel.WARNING,"Event.onMobDeath customMob object is null!");
                }
                if(customMob.dropEMKS) {
                    double EMKSchance = 0.00;
                    EMKSchance = customMob.EMKSchance/100;
                    double randomValue = Math.random();
                    pluginLogger.log(PluginLogger.LogLevel.DROP, "Event.onMobDeath EMKS drop chance: " + EMKSchance + ", randomValue: " + randomValue);
                    if (randomValue <= EMKSchance) {
                        drops.add(dropMobKillerSword());
                        pluginLogger.log(PluginLogger.LogLevel.DROP, "Event.onMobDeath EMKS sword added");
                    }
                }
            }


            entity.setMetadata("DeathHandled", new FixedMetadataValue(plugin, true));
        }


    }
    private ItemStack dropMobKillerSword(){
        int minDamage = 25 + (int) (Math.random() * 40); // Losuje wartość od 10 do 100
        int maxDamage = minDamage + (int) (Math.random() * (131 - minDamage)); // Losuje wartość od minDamage do 100

        // Dodaj swój niestandardowy drop
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = sword.getItemMeta();
        pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS,"Event.dropMobKillerSword sword created");
        if (meta != null) {
            pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS,"Event.dropMobKillerSword");
            meta.setDisplayName("§6§lEpic Mob Slayer Sword");
            List<String> lore = new ArrayList<>();
            lore.add("§6§lMob Damage " + minDamage + "-" + maxDamage);
            lore.add(CustomMobs.dropAverageDamage());
            meta.setLore(lore);
            meta.setUnbreakable(true);
            sword.setItemMeta(meta);
            return sword;
        }else{

            pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS,"Event.dropMobKillerSword meta null");
            return null;
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        //pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event.onEntityDamageByEntity onEntityDamageByEntity called");
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {

            Player damager = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            //pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: onEntityDamageByEntity: calling updateLastHitTime(damager) "+damager);

            // Aktualizacja czasu ostatniego uderzenia
            updateLastHitTime(damager);
            //pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event: onEntityDamageByEntity: calling updateLastHitTime(victim) "+victim);
            updateLastHitTime(victim);
        }
        LivingEntity entity = (LivingEntity) event.getEntity();
        if (entity.hasMetadata("CustomMob")){
            pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.onEntityDamageByEntity custom mob detected");
            customEntityDamageEvent(event);
            //CustomMobs.CustomMob customMob = (CustomMobs.CustomMob) entity;
            //Zombie zombie = (Zombie) entity;
            pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.EntityDamageEvent calling customMobs.updateZombieCustomName(zombie)");
            //customMobs.updateCustomMobName(zombie);
        }


    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamageUpdateCustomMobHealth(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            LivingEntity entity = (LivingEntity) event.getEntity();
            if (entity.hasMetadata("CustomMob")) {
                // Opóźnienie wykonania aktualizacji nazwy, aby zdążyć na aktualizację zdrowia
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // Sprawdzenie, czy mob nadal żyje przed aktualizacją nazwy
                    if (!entity.isDead()) {
                        pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.onEntityDamageByEntity custom mob detected. Updating name.");
                        customMobs.updateCustomMobName(entity);
                    }
                }, 1L); // Opóźnienie o 1 tick
            }
        }
    }

    /*
    @EventHandler
    public void EntityDamageEvent(EntityDamageEvent event) {
        pluginLogger.log(PluginLogger.LogLevel.KILL_EVENT, "Event.EntityDamageEvent  called");
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity  = (LivingEntity) event.getEntity();
            if (entity.hasMetadata("CustomZombie")) {
                pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.EntityDamageEvent custom mob detected");
                try {
                    //CustomMobs.CustomMob customMob = (CustomMobs.CustomMob) livingEntity;
                    //Zombie zombie = (Zombie) entity;
                    pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.EntityDamageEvent calling customMobs.updateZombieCustomName(zombie)");
                    customMobs.updateCustomMobName(livingEntity);
                }catch (Exception e){
                    pluginLogger.log(PluginLogger.LogLevel.ERROR, "Event.EntityDamageEvent exception "+e.getMessage());
                }
            }
        }
    }

     */
    public void customEntityDamageEvent(EntityDamageByEntityEvent event){
        pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.customEntityDamageEvent triggered");

        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            List<ItemStack> equippedItems = getPlayerEquippedItems(player);

            if (itemInHand.hasItemMeta()) {
                ItemMeta meta = itemInHand.getItemMeta();
                if (meta != null && meta.hasLore()) {
                    List<String> lore = meta.getLore();
                    if (lore != null) {
                        double averageDamageBonusPercent = 0;
                        int minDamage = 0;
                        int maxDamage = 0;

                        boolean validDamageLoreFound = false;
                        boolean validAverageDamage = false;

                        averageDamageBonusPercent = getTotalAvgDmgBonus(equippedItems);
                        for (String line : lore) {
                            if (line.startsWith("§6§lMob Damage") || line.startsWith("Mob Damage")) {
                                try {
                                    String[] parts = line.split("Mob Damage ")[1].split("-");
                                    minDamage = Integer.parseInt(parts[0]);
                                    maxDamage = Integer.parseInt(parts[1]);
                                    validDamageLoreFound = true;
                                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                                    pluginLogger.log(PluginLogger.LogLevel.ERROR, "Error parsing damage range from lore: " + line);
                                }
                            }

                        }

                            double averageDamage = (double) (minDamage + maxDamage) / 2; // Średnia wartość obrażeń
                            int bonusDamage = (int) (averageDamage * (averageDamageBonusPercent / 100.0)); // Obliczenie bonusu
                            double totalDamage = minDamage + random.nextInt(maxDamage - minDamage + 1) + bonusDamage; // Całkowite obrażenia
                            pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.customEntityDamageEvent minDamage: "+minDamage+", maxDamage: "+maxDamage+", averageDamage: "+averageDamage+", bonusAverageDamage: "+bonusDamage);
                            event.setDamage(totalDamage);
                            pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.customEntityDamageEvent totalDamage: " + totalDamage+", bonusDamage: "+bonusDamage);
                            return;

                    }
                }
            }
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo] " + ChatColor.DARK_RED + "Only items with "+ChatColor.GOLD + ChatColor.BOLD +"Mob Damage"+ ChatColor.DARK_RED +" effect can attack mobs!");

        }
        event.setCancelled(true);

        //pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Damage event cancelled due to no valid item lore");
    }
    public List<ItemStack> getPlayerEquippedItems(Player player) {
        EntityEquipment equipment = player.getEquipment();
        List<ItemStack> equippedItems = new ArrayList<>();

        if (equipment != null) {
            // Dodawanie przedmiotu trzymanego w głównej ręce
            if (equipment.getItemInMainHand() != null) {
                equippedItems.add(equipment.getItemInMainHand());
            }
            // Dodawanie przedmiotu trzymanego w pomocniczej ręce
            if (equipment.getItemInOffHand() != null) {
                equippedItems.add(equipment.getItemInOffHand());
            }
            // Dodawanie elementów zbroi
            for (ItemStack item : equipment.getArmorContents()) {
                if (item != null) {
                    equippedItems.add(item);
                }
            }

        }
        return equippedItems;
    }
    public double getTotalAvgDmgBonus(List<ItemStack> equippedItems){
        double averageDamageBonusPercent = 0;
        if(equippedItems==null){
            return averageDamageBonusPercent;
        }
            for (ItemStack item : equippedItems){
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasLore()) {
                    List<String> lore = meta.getLore();
                    for (String line : lore) {
                        if (line.startsWith("§6§lAverage Damage +")) {
                            try {
                                String percentString = line.replace("§6§lAverage Damage +", "").replace("%", "");
                                pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.TotalAvgDmgBonus percentString: " + percentString);
                                averageDamageBonusPercent += Integer.parseInt(percentString);
                                pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.TotalAvgDmgBonus added averageDamageBonusPercent: " + averageDamageBonusPercent);
                            } catch (NumberFormatException e) {
                                pluginLogger.log(PluginLogger.LogLevel.ERROR, "Error parsing average damage bonus from lore: " + line);
                            }
                        }

                    }
                }
            }
            pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.TotalAvgDmgBonus  total averageDamageBonusPercent: " + averageDamageBonusPercent);
            return averageDamageBonusPercent;
    }
    public boolean isValidAverageDamage (ArrayList<ItemStack> equippedItems){
        boolean isValidAverageDamage = false;
            for (ItemStack item : equippedItems){
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasLore()) {
                    List<String> lore = meta.getLore();
                    for (String line : lore) {
                        if (line.startsWith("§6§lAverage Damage +")) {
                            isValidAverageDamage = true;pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.TotalAvgDmgBonus  total isValidAverageDamage: " + isValidAverageDamage);
                            return isValidAverageDamage;
                        }

                    }
                }
            }
        pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.TotalAvgDmgBonus  total isValidAverageDamage: " + isValidAverageDamage);
        return isValidAverageDamage;
    }

}
