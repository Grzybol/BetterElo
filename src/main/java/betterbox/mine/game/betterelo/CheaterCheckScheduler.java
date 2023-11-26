package betterbox.mine.game.betterelo;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public class CheaterCheckScheduler {
        private final BetterRanksCheaters betterRanksCheaters;
        private final BukkitScheduler scheduler;
        private final PluginLogger pluginLogger;
        private final Plugin plugin; // Dodaj zmienną plugin

        public CheaterCheckScheduler(Plugin plugin, BetterRanksCheaters betterRanksCheaters, BukkitScheduler scheduler, PluginLogger pluginLogger) {

            this.plugin = plugin; // Zapisz zmienną plugin
            this.betterRanksCheaters = betterRanksCheaters;
            this.scheduler = scheduler;
            this.pluginLogger = pluginLogger;
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "CheaterCheckScheduler called");
        }

        public void startScheduler() {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2, "startScheduler called");
            int delay = 0; // Opóźnienie początkowe (0 ticków)
            int period = 100; // Okres w tickach (20 ticków to 1 sekunda)

            scheduler.scheduleSyncRepeatingTask(plugin, () -> {
                betterRanksCheaters.CheckCheatersFromBetterRanks();
            }, delay, period);
        }
}

