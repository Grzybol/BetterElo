package betterbox.mine.game.betterelo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class  Event implements Listener {
    private final DataManager dataManager;
    private final JavaPlugin plugin;
    private final PluginLogger pluginLogger;
    private final BetterElo betterElo;
    private BetterRanksCheaters cheaters;
    private ExtendedConfigManager configManager;

    public Event(DataManager dataManager, PluginLogger pluginLogger, JavaPlugin plugin, BetterRanksCheaters cheaters, ExtendedConfigManager configManager, BetterElo betterElo) {
        this.dataManager = dataManager;
        this.pluginLogger = pluginLogger;
        this.betterElo = betterElo;
        this.plugin = plugin;
        this.cheaters = cheaters;
        this.configManager = configManager;
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
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: handleKillEvent called with parameters: " + rankingType+" "+victim+" "+killer);
        double pointsEarned = 0;

        if (killer != null && !killer.equals(victim)) {
            // Gracz zabił innego gracza
            double victimElo = getElo(victim.getUniqueId().toString(), rankingType);
            double killerElo = getElo(killer.getUniqueId().toString(), rankingType);
            double maxElo = getMaxElo(rankingType);
            double minElo = dataManager.getMinElo(rankingType);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: handleKillEvent: rankingType: " + rankingType + " loaded variables: maxElo:" + maxElo + " minElo: " + minElo + " victimElo:" + victimElo + " victim name: " + victim.getName() + " killerElo:" + killerElo + " killer name: " + killer.getName());
            double basePoints = 100;
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: handleKillEvent calling calculatePointsEarned with parameters: basePoints " + basePoints + " killerElo " + killerElo + " victimElo " + victimElo + " maxElo " + maxElo + " minElo " + minElo);
            pointsEarned = calculatePointsEarned(basePoints, killerElo, victimElo, maxElo, minElo);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: handleKillEvent: pointsEarned: " + pointsEarned + "rankingType: " + rankingType);

            // Zapisz informacje do bazy danych
            PlayerKillDatabase playerKillDatabase = new PlayerKillDatabase(pluginLogger);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: handleKillEvent calling saveKillData");
            playerKillDatabase.saveKillData(rankingType, victim.getName(), killer.getName(), pointsEarned, killerElo, victimElo);

            // Dodaj punkty graczowi, który zabił
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: handleKillEvent calling addPoints with parameters: " + killer.getUniqueId().toString() + " " + pointsEarned + " " + rankingType);
            addPoints(killer, pointsEarned, rankingType);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: handleKillEvent calling subtractPoints with parameters: " + victim.getUniqueId().toString() + " " + pointsEarned + " " + rankingType);
            subtractPoints(victim, pointsEarned, rankingType);
        }
        return pointsEarned;
    }


    // Mapa do śledzenia ostatnich uderzeń
    private final Map<UUID, Long> lastHitTime = new HashMap<>();

    // Metoda do aktualizacji czasu ostatniego uderzenia
    public void updateLastHitTime(Player player) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: updateLastHitTime called");
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: updateLastHitTime saving "+player.getUniqueId()+" "+System.currentTimeMillis());
        lastHitTime.put(player.getUniqueId(), System.currentTimeMillis());
    }

    // Sprawdź, czy śmierć nastąpiła w wyniku walki
    private boolean deathDueToCombat(Player player) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: deathDueToCombat called");
        Long lastHit = lastHitTime.get(player.getUniqueId());
        return lastHit != null && (System.currentTimeMillis() - lastHit <= 10000); // 10 sekund
    }
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: onEntityDamageByEntity called");
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {

            Player damager = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: onEntityDamageByEntity: calling updateLastHitTime(damager) "+damager);

            // Aktualizacja czasu ostatniego uderzenia
            updateLastHitTime(damager);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: onEntityDamageByEntity: calling updateLastHitTime(victim) "+victim);
            updateLastHitTime(victim);
        }
    }
    // Metoda do znalezienia ostatniego napastnika
    private Player getLastAttacker(Player victim) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: getLastAttacker called with parameters: " + victim);
        try {
            Player lastAttacker = null;
            long lastAttackTime = 0;
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: getLastAttacker: checking online players");
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
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: getLastAttacker: lastAttacker " + lastAttacker);


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
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: onPlayerDeath: victim: " + victim + " killer: " + killer);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: onPlayerDeath calling deathDueToCombat(victim)");
                if (killer == null && deathDueToCombat(victim)) {
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: onPlayerDeath killer is null");
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: onPlayerDeath: deathDueToCombat(victim): " + deathDueToCombat(victim) + " victim: " + victim);
                    // Znajdź ostatniego gracza, który uderzył ofiarę
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: onPlayerDeath calling getLastAttacker(victim)");
                    Player lastAttacker = getLastAttacker(victim);
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: onPlayerDeath: lastAttacker " + lastAttacker);

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
                String victimUUID = victim.getUniqueId().toString();
                String killerUUID = killer.getUniqueId().toString();
                if (dataManager.getPoints(killerUUID, "main") - dataManager.getPoints(victimUUID, "main") < 1000) {
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: onPlayerDeath calling handleKillEvent with parameters: main " + victim + " " + killer);
                    double pointsEarned = handleKillEvent("main", victim, killer);
                    assert killer != null;
                    notifyPlayersAboutPoints(killer, victim, pointsEarned);
                } else {
                    killer.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo] " + ChatColor.DARK_RED + "Your Elo difference in the Main ranking is too big! No reward for this one.");
                }
                if (dataManager.getPoints(killerUUID, "daily") - dataManager.getPoints(victimUUID, "daily") < 1000) {
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: onPlayerDeath calling handleKillEvent with parameters: daily " + victim + " " + killer);
                    handleKillEvent("daily", victim, killer);
                } else {
                    killer.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo] " + ChatColor.DARK_RED + "Your Elo difference in the Daily ranking is too big! No reward for this one.");
                }
                if (dataManager.getPoints(killerUUID, "weekly") - dataManager.getPoints(victimUUID, "weekly") < 1000) {
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: onPlayerDeath calling handleKillEvent with parameters: weekly " + victim + " " + killer);
                    handleKillEvent("weekly", victim, killer);
                } else {
                    killer.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo] " + ChatColor.DARK_RED + "Your Elo difference in the Weekly ranking is too big! No reward for this one.");
                }
                if (dataManager.getPoints(killerUUID, "monthly") - dataManager.getPoints(victimUUID, "monthly") < 1000) {
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: onPlayerDeath calling handleKillEvent with parameters: monthly " + victim + " " + killer);
                    handleKillEvent("monthly", victim, killer);
                } else {
                    killer.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo] " + ChatColor.DARK_RED + "Your Elo difference in the Monthly ranking is too big! No reward for this one.");
                }
                if(betterElo.isEventEnabled) {
                    if (dataManager.getPoints(killerUUID, "event") - dataManager.getPoints(victimUUID, "event") < 1000) {
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: onPlayerDeath calling handleKillEvent with parameters: event " + victim + " " + killer);
                        handleKillEvent("event", victim, killer);
                    } else {
                        killer.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo] " + ChatColor.DARK_RED + "Your Elo difference in the Event ranking is too big! No reward for this one.");
                    }
                }


            } else {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: handleKillEvent: returning 0 points because either  " + victim + " " + cheaters.getCheatersList().contains(victim.getName()) + " or " + killer + " " + cheaters.getCheatersList().contains(killer.getName()) + " has CHEATER rank in BetterRanks.");
            }
        } catch (Exception e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Event: onPlayerDeath exception  " + e + " " + e.getMessage());
        }
    });
}


    private void notifyPlayersAboutPoints(Player killer, Player victim, double pointsEarned) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: notifyPlayersAboutPoints called with parameters: "+killer+" "+victim+" "+pointsEarned);
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
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: notifyPlayersAboutPoints called with parameters: "+player+" "+pointsEarned);
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
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: calculatePointsEarned called with parameters : base "+base+" elo1 "+killerelo+" elo2 "+victimelo+" maxElo "+maxElo+" minElo "+minElo);
        double eloDifference = killerelo - victimelo;
        double normalizedDifference = eloDifference / (maxElo - minElo + 1);
        double points = base * (1 - normalizedDifference);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: calculatePointsEarned: eloDifference: "+eloDifference+" normalizedDifference: "+normalizedDifference+" points: "+points+" maxElo: "+maxElo+" minElo: "+minElo);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: calculatePointsEarned: PointsEarnedOut: "+(double)Math.round(points*100));
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: calculatePointsEarned: PointsEarnedOut/100: "+(double)Math.round(points*100)/100);
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
        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL4,"onBlockBreak called");
        Block block = event.getBlock();

        // Sprawdź, czy blok zniszczony przez gracza znajduje się na liście nagród
            String blockType = block.getType().toString();

            if (configManager.getBlockRewards().containsKey(blockType)) {
                for (MetadataValue meta : block.getMetadata("placed_by_player")) {
                    if (meta.asBoolean()) {
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL4,"onBlockBreak: block was placed by a player");
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
                pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL4,"Event: onBlockBreak: player: "+player.getName()+", blockType: "+blockType+", pointsEarned: "+pointsEarnedMain+", ranking main");

                playerElo = dataManager.getPoints(uuid,"daily");
                double pointsEarned = calculatePointsEarnedFromBlock(base,playerElo,blockReward, dataManager.getMaxElo("daily"), dataManager.getMinElo("daily"));
                addPoints(uuid,pointsEarned,"daily");
                pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL4,"Event: onBlockBreak: player: "+player.getName()+", blockType: "+blockType+", pointsEarned: "+pointsEarned+", ranking daily");

                playerElo = dataManager.getPoints(uuid,"weekly");
                pointsEarned = calculatePointsEarnedFromBlock(base,playerElo,blockReward, dataManager.getMaxElo("weekly"), dataManager.getMinElo("weekly"));
                addPoints(uuid,pointsEarned,"weekly");
                pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL4,"Event: onBlockBreak: player: "+player.getName()+", blockType: "+blockType+", pointsEarned: "+pointsEarned+", ranking weekly");

                playerElo = dataManager.getPoints(uuid,"monthly");
                pointsEarned = calculatePointsEarnedFromBlock(base,playerElo,blockReward, dataManager.getMaxElo("monthly"), dataManager.getMinElo("monthly"));
                addPoints(uuid,pointsEarned,"monthly");
                pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL4,"Event: onBlockBreak: player: "+player.getName()+", blockType: "+blockType+", pointsEarned: "+pointsEarned+", ranking monthly");
                if(betterElo.isEventEnabled){
                    playerElo = dataManager.getPoints(uuid,"event");
                    pointsEarned = calculatePointsEarnedFromBlock(base,playerElo,blockReward, dataManager.getMaxElo("event"), dataManager.getMinElo("event"));
                    addPoints(uuid,pointsEarned,"event");
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL4,"Event: onBlockBreak: player: "+player.getName()+", blockType: "+blockType+", pointsEarned: "+pointsEarned+", ranking event");
                }
                if(pointsEarnedMain>0.001) {
                    notifyPlayerAboutPoints(player, pointsEarnedMain, blockReward);
                }
            }
            else{
                pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL4,"Block "+blockType+" is not on the list");
            }

    }

    // Metoda do dodawania metadanych do bloku podczas stawiania przez gracza
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL4,"Event.onBlockPlace: called");
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
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3,"Event.onPlayerInteract called");
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        int removeradius = 0;

        // Sprawdź, czy gracz trzyma odpowiedni przedmiot i nacisnął prawy przycisk myszy
        if (hasAntywebLore(itemInHand) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3,"Event.onPlayerInteract antywebcheck passed");
            removeradius = getAntywebRadius(itemInHand);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3,"Event.onPlayerInteract removeradius "+removeradius);
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

        }
        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3,"Event.onPlayerInteract checking if its infinite firework");
        ItemStack item = event.getItem();



        if (item != null && item.getType() == Material.FIREWORK_ROCKET) {
            Location location = player.getLocation();
            Location blockBelowLocation = location.clone().subtract(0, 1, 0);
            boolean isNotOnGround = blockBelowLocation.getBlock().isPassable();

            ItemStack chestplate = player.getInventory().getChestplate();
            if (chestplate == null || !chestplate.getType().toString().contains("ELYTRA")) {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3,"Event.onPlayerInteract player is not wearing Elytra!");
                return;

            }
            pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3,"Event.onPlayerInteract firework item check passed");
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasLore()) {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3,"Event.onPlayerInteract firework has lore check passed");
                if (meta.getLore().contains("§6§lInfinite usage")) {// Gold bold "Infinite usage"
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3, "Event.onPlayerInteract firework has formatted lore check passed");
                    if(isNotOnGround) {
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3, "Event.onPlayerInteract isNotOnGround check passed");
                        event.setCancelled(true); // Cancel the event so the firework isn't consumed
                        FireworkMeta fireworkMeta = (FireworkMeta) item.getItemMeta();
                        int power = fireworkMeta.getPower();
                        launchFireworkEffect(event.getPlayer());
                        applyBoosterEffect(event.getPlayer(), power);
                    }
                    else {
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3, "Event.onPlayerInteract isNotOnGround check failed");
                        event.setCancelled(true);
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
        // Zastosowanie efektu przyspieszenia (booster effect) dla gracza
        Vector velocity = player.getLocation().getDirection().multiply(power); // Przykładowa prędkość (można dostosować)
        player.setVelocity(velocity);
    }
    public boolean hasAntywebLore(ItemStack itemStack) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3,"Event.hasAntywebLore called");
        if (itemStack == null || !itemStack.hasItemMeta()) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3,"Event.hasAntywebLore itemmeta check failed");
            return false;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null || !itemMeta.hasLore()) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3,"Event.hasAntywebLore lore check failed");
            return false;
        }

        for (String lore : itemMeta.getLore()) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3,"Event.hasAntywebLore Antyweb check triggered, checking lore");
            if (lore != null && lore.contains("Antyweb")) {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3,"Event.hasAntywebLore Antyweb check triggered, checking formatting");
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
    public Integer getAntywebRadius(ItemStack itemStack) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3,"Event.getAntywebRadius called");
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return null;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null || !itemMeta.hasLore()) {
            return null;
        }

        for (String lore : itemMeta.getLore()) {
            // Sprawdź, czy linia lore zawiera napis "Antyweb" i czy zawiera formatowanie
            pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3,"Event.getAntywebRadius Antyweb check triggered, checking lore");
            if (lore != null && lore.contains("Antyweb")) {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3,"Event.getAntywebRadius Antyweb check triggered, checking formatting");
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
}
