package betterbox.mine.game.betterelo;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.text.DecimalFormat;

public class Event implements Listener {
    private final DataManager dataManager;
    private final JavaPlugin plugin;
    private final PluginLogger pluginLogger;

    public Event(DataManager dataManager, PluginLogger pluginLogger, JavaPlugin plugin) {
        this.dataManager = dataManager;
        this.pluginLogger = pluginLogger;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        String playerUUID = player.getUniqueId().toString();

        pluginLogger.log(PluginLogger.LogLevel.DEBUG,String.format("Event: onPlayerJoin: gracz %s wszedl na serwer", playerName));

        if (!dataManager.playerExists(playerUUID,"main")) {
            pluginLogger.log(PluginLogger.LogLevel.WARNING,"Event: onPlayerJoin: Player "+playerName+" does not exists in main database.");
            dataManager.setPoints(playerUUID, 1000, "main");

        } else {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: onPlayerJoin: "+playerName+" Already in main database.");
        }
        if (!dataManager.playerExists(playerUUID,"daily")) {
            pluginLogger.log(PluginLogger.LogLevel.WARNING,"Event: onPlayerJoin: Player "+playerName+" does not exists in daily database.");
            dataManager.setPoints(playerUUID, 1000, "daily");

        } else {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: onPlayerJoin: "+playerName+" Already in daily database.");
        }
        if (!dataManager.playerExists(playerUUID,"weekly")) {
            pluginLogger.log(PluginLogger.LogLevel.WARNING,"Event: onPlayerJoin: Player "+playerName+" does not exists in weekly database.");
            dataManager.setPoints(playerUUID, 1000, "weekly");

        } else {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: onPlayerJoin: "+playerName+" Already in weekly database.");
        }
        if (!dataManager.playerExists(playerUUID,"monthly")) {
            pluginLogger.log(PluginLogger.LogLevel.WARNING,"Event: onPlayerJoin: Player "+playerName+" does not exists in monthly database.");
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
            double victimElo = getElo(victim.getUniqueId().toString(),rankingType);
            double killerElo = getElo(killer.getUniqueId().toString(),rankingType);
            double maxElo = getMaxElo(rankingType);
            double minElo = dataManager.getMinElo(rankingType);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: KillEvent: rankingType: "+rankingType+" loaded variables: maxElo:" + maxElo + " minElo: "+minElo+" victimElo:" + victimElo + " victim name: "+victim.getName()+" killerElo:" + killerElo+" killer name: "+killer.getName());
            double basePoints = 100;
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: handleKillEvent calling calculatePointsEarned with parameters: basePoints " + basePoints+" killerElo "+killerElo+" victimElo "+victimElo+" maxElo "+maxElo+" minElo "+minElo);
            pointsEarned = calculatePointsEarned(basePoints, killerElo, victimElo, maxElo, minElo);
            pluginLogger.log(PluginLogger.LogLevel.INFO,"Event: KillEvent: pointsEarned: " + pointsEarned+"rankingType: "+rankingType);


            // Dodaj punkty graczowi, który zabił
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: handleKillEvent calling addPoints with parameters: " + killer.getUniqueId().toString()+" "+pointsEarned+" "+rankingType);
            addPoints(killer.getUniqueId().toString(), pointsEarned, rankingType);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: handleKillEvent calling subtractPoints with parameters: " + victim.getUniqueId().toString()+" "+pointsEarned+" "+rankingType);
            subtractPoints(victim.getUniqueId().toString(), pointsEarned, rankingType);
        }
        return pointsEarned;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        try
        {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: onPlayerDeath called with parameters: " + event);
            Player victim = event.getEntity();
            Player killer = victim.getKiller();
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: onPlayerDeath: victim: " + victim + " killer: " + killer);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: onPlayerDeath calling handleKillEvent with parameters: main " + victim+" "+killer);
            double pointsEarned = handleKillEvent("main", victim, killer);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: onPlayerDeath calling handleKillEvent with parameters: daily " + victim+" "+killer);
            handleKillEvent("daily", victim, killer);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: onPlayerDeath calling handleKillEvent with parameters: weekly " + victim+" "+killer);
            handleKillEvent("weekly", victim, killer);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: onPlayerDeath calling handleKillEvent with parameters: monthly " + victim+" "+killer);
            handleKillEvent("monthly", victim, killer);

            notifyPlayersAboutPoints(killer, victim, pointsEarned);
        }catch (Exception e){
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Event: onPlayerDeath exception  " + e+" "+e.getMessage());
        }
    }

    private void notifyPlayersAboutPoints(Player killer, Player victim, double pointsEarned) {
        DecimalFormat df = new DecimalFormat("#.##");

        // Notify the killer
        killer.sendActionBar(ChatColor.GREEN + "You have earned " + df.format(pointsEarned) + " points!");

        // Notify the victim
        victim.sendActionBar(ChatColor.RED + "You have lost " + df.format(pointsEarned) + " points!");
    }



    private double calculatePointsEarned(double base, double elo1, double elo2, double maxElo, double minElo) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: calculatePointsEarned called with parameters : base "+base+" elo1 "+elo1+" elo2 "+elo2+" maxElo "+maxElo+" minElo "+minElo);
        double eloDifference = elo1 - elo2;
        double normalizedDifference = eloDifference / (maxElo - minElo + 1);
        double points = base * (1 - normalizedDifference);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: calculatePointsEarned: eloDifference: "+eloDifference+" normalizedDifference: "+normalizedDifference+" points: "+points+" maxElo: "+maxElo+" minElo: "+minElo);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: calculatePointsEarned: PointsEarnedOut: "+(double)Math.round(points*100));
        pluginLogger.log(PluginLogger.LogLevel.INFO,"Event: calculatePointsEarned: PointsEarnedOut/100: "+(double)Math.round(points*100)/100);
        return (double)Math.round(points*100)/100;
    }

    private void updatePoints(String playerUUID, double points, String rankingType, boolean isAdding) {
        double currentPoints = dataManager.getPoints(playerUUID, rankingType);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,String.format("Event: %sPoints: currentPoints: %sBefore: %s",
                (isAdding ? "add" : "subtract"), rankingType, currentPoints));
        currentPoints = isAdding ? currentPoints + points : currentPoints - points;
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,String.format("Event: %sPoints: currentPoints: %sAfter: %s",
                (isAdding ? "add" : "subtract"), rankingType, currentPoints));
        dataManager.setPoints(playerUUID, Math.round(currentPoints * 100) / 100.0, rankingType);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,String.format("Event: %sPoints: currentPoints: %s ranking saved", (isAdding ? "add" : "subtract"), rankingType));
    }

    private void addPoints(String playerUUID, double points, String rankingType) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: addPoints: playerUUID: "+playerUUID+" rankingType: "+rankingType+" points: "+points);
        updatePoints(playerUUID, points, rankingType, true);
    }

    private void subtractPoints(String playerUUID, double points, String rankingType) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: subtractPoints: playerUUID: "+playerUUID+" rankingType: "+rankingType+" points: "+points);
        updatePoints(playerUUID, points, rankingType, false);
    }

    private double getElo(String playerUUID, String rankingType) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: getElo: playerUUID: "+playerUUID+" rankingType: "+rankingType);
        return dataManager.getPoints(playerUUID, rankingType);
    }

    private double getMaxElo(String rankingType) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Event: getMaxElo: rankingType: "+rankingType);
        return dataManager.getMaxElo(rankingType);
    }
}
