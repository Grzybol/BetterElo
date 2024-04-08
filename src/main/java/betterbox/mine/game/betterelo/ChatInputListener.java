package betterbox.mine.game.betterelo;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ChatInputListener implements Listener {
    private final Plugin plugin;
    private static final Map<Player, BlockingQueue<String>> playerInputs = new HashMap<>();

    public ChatInputListener(Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (playerInputs.containsKey(player)) {
            String message = event.getMessage();
            try {
                playerInputs.get(player).put(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            event.setCancelled(true); // Anulowanie wyświetlania wiadomości gracza w czacie
        }
    }

    public static void addPlayerWaitingForInput(Player player) {
        playerInputs.put(player, new LinkedBlockingQueue<>());
    }

    public static void removePlayerWaitingForInput(Player player) {
        playerInputs.remove(player);
    }

    public String waitForInput(Player player) {
        try {
            return playerInputs.get(player).take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}

