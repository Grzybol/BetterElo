package betterbox.mine.game.betterelo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
        String prefix = "[BetterElo]";
        broadcastFormattedChatMessage(prefix, message);
        message = " Use /shop  to get our Item-Shop link";
        broadcastFormattedChatMessage(prefix, message);
        message = " Use /discord  to get our Discord link";
        broadcastFormattedChatMessage(prefix, message);
    }
    public void broadcastFormattedChatMessage(String prefix, String message) {
        Component componentPart1 = Component.text(prefix)
                .color(NamedTextColor.GOLD) // Ustawienie pierwszego koloru
                .decorate(TextDecoration.BOLD); // Pierwsze formatowanie

        Component componentPart2 = Component.text(message)
                .color(NamedTextColor.AQUA); // Ustawienie drugiego koloru

        // Łączenie obu komponentów w jeden
        Component finalComponent = componentPart1.append(componentPart2);

        // Wysłanie komponentu do wszystkich graczy na serwerze
        Bukkit.getServer().broadcast(finalComponent);;
    }
}
