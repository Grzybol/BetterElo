package betterbox.mine.game.betterelo;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    PluginLogger pluginLogger;
    BetterElo betterElo;
    public Utils(BetterElo plugin,PluginLogger pluginLogger) {
        this.pluginLogger = pluginLogger;
        this.betterElo = plugin;
        pluginLogger.log(PluginLogger.LogLevel.INFO, "Utils constructor called");

    }
    public void updateAverageDamage(ItemStack item,int avgDmg) {
        if (item != null && item.hasItemMeta()) {
            pluginLogger.log(PluginLogger.LogLevel.REROLL, "updateAverageDamage called, betterElo.hasMobDamageAttribute(item): " + betterElo.hasMobDamageAttribute(item) + ", betterElo.hasAverageDamageAttribute(item): " + betterElo.hasAverageDamageAttribute(item));

            if (betterElo.hasMobDamageAttribute(item) && betterElo.hasAverageDamageAttribute(item)) {
                ItemMeta itemMeta = item.getItemMeta();
                List<String> lore = new ArrayList<>(itemMeta.getLore());


                pluginLogger.log(PluginLogger.LogLevel.REROLL, "updateAverageDamage, re-rolling average damage...");

                for (int i = 0; i < lore.size(); i++) {
                    pluginLogger.log(PluginLogger.LogLevel.REROLL, "updateAverageDamage lore i=" + i);
                    if (lore.get(i).contains("Average Damage")) {
                        pluginLogger.log(PluginLogger.LogLevel.REROLL, "updateAverageDamage lore i=" + i + " contains Average Damage, setting avgDmg: " + avgDmg);
                        lore.set(i, "§6§lAverage Damage +" + avgDmg + "%");
                    }
                }

                itemMeta.setLore(lore);
                item.setItemMeta(itemMeta);
                betterElo.addAverageDamageAttribute(item, avgDmg);
            }
        }
    }
}
