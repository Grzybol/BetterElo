package betterbox.mine.game.betterelo;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Bukkit;

public class ChatNotifier extends BukkitRunnable {

    private final BetterElo plugin;
    private final Lang lang;
    private final Utils utils;

    public ChatNotifier(BetterElo plugin,Lang lang,Utils utils) {
        this.plugin = plugin;
        this.lang = lang;
        this.utils = utils;
    }

    @Override
    public void run() {
        // Wiadomość, którą chcesz wysłać
        for (String line : lang.chatNotifierMessagesList) {
            //String prefix = ChatColor.GOLD+""+ChatColor.BOLD +"[BetterElo]";
            broadcastFormattedChatMessage(Utils.FormatUtil.applyFormatting(lang.prefix)  + Utils.FormatUtil.applyFormatting(line));
        }
        /*
        String message = " Remember to use /be claim to claim your rewards!";
        String prefix = ChatColor.GOLD+""+ChatColor.BOLD +"[BetterElo]";
        broadcastFormattedChatMessage(prefix + message);
        message = ChatColor.AQUA+" Use /shop  to get our Item-Shop link";
        broadcastFormattedChatMessage(prefix + message);
        message = ChatColor.AQUA+" Use /discord  to get our Discord link";
        broadcastFormattedChatMessage(prefix + message);

         */
    }

    public void broadcastFormattedChatMessage(String message) {
        // Wysłanie wiadomości do wszystkich graczy na serwerze
        Bukkit.getServer().broadcastMessage(message);
    }
}
