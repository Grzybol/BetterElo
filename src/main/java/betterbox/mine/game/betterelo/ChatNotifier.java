package betterbox.mine.game.betterelo;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

public class ChatNotifier extends BukkitRunnable {

    private final BetterElo plugin;

    public ChatNotifier(BetterElo plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // Wiadomość, którą chcesz wysłać
        String message = ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Remember to use /be claim to claim your rewards!";

        // Wysyłanie wiadomości na czat
        plugin.getServer().broadcastMessage(message);
    }
}
