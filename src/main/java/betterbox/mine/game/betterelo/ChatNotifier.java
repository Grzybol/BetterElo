package betterbox.mine.game.betterelo;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Bukkit;

public class ChatNotifier extends BukkitRunnable {

    private final BetterElo plugin;

    public ChatNotifier(BetterElo plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // Wiadomość, którą chcesz wysłać
        String message = " Remember to use /be claim to claim your rewards!";
        String prefix = ChatColor.GOLD+""+ChatColor.BOLD +"[BetterElo]";
        broadcastFormattedChatMessage(prefix + message);
        message = ChatColor.AQUA+" Use /shop  to get our Item-Shop link";
        broadcastFormattedChatMessage(prefix + message);
        message = ChatColor.AQUA+" Use /discord  to get our Discord link";
        broadcastFormattedChatMessage(prefix + message);
    }

    public void broadcastFormattedChatMessage(String message) {
        // Wysłanie wiadomości do wszystkich graczy na serwerze
        Bukkit.getServer().broadcastMessage(message);
    }
}
