package betterbox.mine.game.betterelo;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.entity.Player;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldedit.util.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public class WorldGuardUtils {
    private final PluginLogger pluginLogger;

    public WorldGuardUtils(PluginLogger pluginLogger) {
        this.pluginLogger = pluginLogger;
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "WorldGuardUtils: called.");
    }

    public boolean isPlayerInExcludedRegion(Player player, List<String> excludedRegions) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "WorldGuardUtils: isPlayerInExcludedRegion called with parameters " + player.getName() + " " + Arrays.toString(excludedRegions.toArray()));
        Location playerLocation = BukkitAdapter.adapt(player.getLocation());

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(player.getWorld()));
        BlockVector3 vector = BlockVector3.at(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ());

        if (regionManager == null) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "WorldGuardUtils: isPlayerInExcludedRegion: Player " + player.getName() + " is not standing in any region");
            return false;
        }

        ApplicableRegionSet regions = regionManager.getApplicableRegions(vector);

        for (ProtectedRegion region : regions) {
            String regionName = region.getId();
            if (excludedRegions.contains(regionName)) {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL4, "WorldGuardUtils: isPlayerInExcludedRegion: Player " + player.getName() + " is standing in an excluded region " + regionName);
                return false; // Gracz jest w danym regionie, który jest wyłączony
            }
        }
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "WorldGuardUtils: isPlayerInExcludedRegion: Player " + player.getName() + " is not standing in any excluded region.");
        return true; // Gracz nie jest w żadnym z wyłączonych regionów
    }
}
