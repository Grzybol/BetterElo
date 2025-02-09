package betterbox.mine.game.betterelo;

import com.comphenix.protocol.*;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class MobNameUtil {

    private final Plugin plugin;
    private final ProtocolManager protocolManager;
    private final PluginLogger pluginLogger;

    public MobNameUtil(Plugin plugin,PluginLogger pluginLogger, ProtocolManager protocolManager) {
        this.plugin = plugin;
        this.protocolManager = protocolManager;
        this.pluginLogger=pluginLogger;
    }

    public void sendMultiLineName(LivingEntity mob, Player player, String transactionID, String... lines) {
        if (lines.length == 0) return;

        try {
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);

            String teamName = "mob_" + mob.getUniqueId().toString().substring(0, 10);

            packet.getStrings().write(0, teamName); // Nazwa teamu
            packet.getStrings().write(1, "never"); // Kolizja
            packet.getChatComponents().write(0, WrappedChatComponent.fromText(lines[0])); // Prefix
            if (lines.length > 1) {
                packet.getChatComponents().write(1, WrappedChatComponent.fromText(lines[1])); // Suffix
            }
            pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Sending multi-line name for mob: " + mob.getUniqueId().toString() + " to player: " + player.getName()+", packet: "+packet.toString(),transactionID);
            protocolManager.sendServerPacket(player, packet);
        } catch (Exception e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Error while sending multi-line name for mob: " + mob.getUniqueId().toString()+ " to player: " + player.getName()+", error: "+e.getMessage()+", error stack: "+e.getStackTrace(),transactionID);
            ;
        }
    }


    public void updateCustomMobName(LivingEntity mob,double eloPoints, String transactionID) {
        String line1 = CustomMobs.FormatUtil.applyFormatting(mob.getCustomName())+" HP: " + Math.round(mob.getHealth()) + "/" + Math.round(mob.getMaxHealth());
        String line2 = "Elo: " + Math.round(eloPoints);

        Location mobLocation = mob.getLocation();
        double maxDistance = 20.0; // Maksymalny zasięg w blokach

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(mobLocation.getWorld()) && player.getLocation().distanceSquared(mobLocation) <= maxDistance * maxDistance) {
                sendMultiLineName(mob, player, transactionID, line1, line2);
            }
        }
    }
}

