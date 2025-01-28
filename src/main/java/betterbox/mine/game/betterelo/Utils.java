package betterbox.mine.game.betterelo;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Utils {
    static PluginLogger pluginLogger;
    static BetterElo betterElo;
    private static final Random random = new Random();
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
    public static int dropAverageDamage() {
        double rand = random.nextDouble();
        double x = Math.pow(-Math.log(rand), 0.44) * 18; // Adjusted exponential transformation and scale
        int bonus = (int) x;
        bonus = Math.min(bonus, 60);
        if(bonus==0){
            bonus=1;
        }
        return bonus;
    }
    public static List<ItemStack> getPlayerEquippedItems(Player player) {
        EntityEquipment equipment = player.getEquipment();
        List<ItemStack> equippedItems = new ArrayList<>();

        if (equipment != null) {
            // Dodawanie przedmiotu trzymanego w głównej ręce
            if (equipment.getItemInMainHand() != null) {
                equippedItems.add(equipment.getItemInMainHand());
            }
            // Dodawanie przedmiotu trzymanego w pomocniczej ręce
            if (equipment.getItemInOffHand() != null) {
                equippedItems.add(equipment.getItemInOffHand());
            }
            // Dodawanie elementów zbroi
            for (ItemStack item : equipment.getArmorContents()) {
                if (item != null) {
                    equippedItems.add(item);
                }
            }

        }
        return equippedItems;
    }
    public static double getTotalAvgDmgBonus(List<ItemStack> equippedItems) {
        double totalAverageDamageBonus = 0;
        if (equippedItems == null) {
            return totalAverageDamageBonus;
        }
        for (ItemStack item : equippedItems) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
                if (dataContainer.has(betterElo.averageDamageKey, PersistentDataType.INTEGER)) {
                    int damageBonus = dataContainer.get(betterElo.averageDamageKey, PersistentDataType.INTEGER);
                    pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.TotalAvgDmgBonus read damageBonus: " + damageBonus);
                    totalAverageDamageBonus += damageBonus;
                }
            }
        }
        pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.TotalAvgDmgBonus total calculated averageDamageBonus: " + totalAverageDamageBonus);
        return totalAverageDamageBonus;
    }
    static ItemStack getBetterCoinStack() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.getBetterCoinStack called");
        Material material = Material.HONEYCOMB;
        int amount = 64;

        ItemStack stack = new ItemStack(material, amount);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD+"BetterCoin");
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.getBetterCoinStack meta.getDisplayName(): "+meta.getDisplayName());
            //Component displayNameComponent = new Component("BetterCoin");
            List<String> lore = List.of(ChatColor.YELLOW+ "Valuable currency you can use to buy items.");
            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }
    public static boolean checkAndRemoveEnchantItem(Player player) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.checkAndRemoveEnchantItem called, player : "+player);
        Inventory inventory = player.getInventory();
        ItemStack enchantItemStack = getEnchantItem();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.checkAndRemoveEnchantItem betterCoinStack: "+enchantItemStack);
        // Sprawdź, czy gracz ma co najmniej 64 BetterCoin w ekwipunku
        if (inventory.containsAtLeast(enchantItemStack, 1)) {
            // Usuń 64 sztuki BetterCoin z ekwipunku gracza
            inventory.removeItem(enchantItemStack);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.checkAndRemoveEnchantItem 1 Enchant Item found, removing : "+enchantItemStack);
            return true;
        }
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.checkAndRemoveEnchantItem 1 Enchant Item not found");
        return false;
    }
    public static ItemStack getEnchantItem(){
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.getEnchantItem called");
        Material material = Material.GHAST_TEAR;
        int amount = 1;

        ItemStack stack = new ItemStack(material, amount);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_PURPLE+""+ ChatColor.BOLD+"Enchant Item");
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.getEnchantItem meta.getDisplayName(): "+meta.getDisplayName());
            //Component displayNameComponent = new Component("BetterCoin");
            List<String> lore = List.of(ChatColor.GRAY+ "Removes current the Average Damage bonus",ChatColor.GRAY+ " from the item and adds new one.");
            meta.setLore(lore);
            // Dodajemy niestandardowy enchant, który nie wpływa na działanie itemu
            meta.addEnchant(Enchantment.LUCK, 1, true);

            // Ukrywamy wszystkie informacje o zaklęciach na itemie
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            stack.setItemMeta(meta);
        }
        return stack;
    }
    public static boolean isEnchantItem(ItemStack itemStack) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.isEnchantItem called, itemStack: " + itemStack);

        if (itemStack == null) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.isEnchantItem itemStack is null");
            return false;
        }

        ItemStack enchantItemStack = Utils.getEnchantItem();  // Zakładam, że metoda getEnchantItem zwraca referencyjny ItemStack
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.isEnchantItem enchantItemStack: " + enchantItemStack);

        // Sprawdź, czy przekazany itemStack ma ten sam typ i enchanty co enchantItemStack
        if (itemStack.getType() == enchantItemStack.getType() && itemStack.getEnchantments().equals(enchantItemStack.getEnchantments())) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.isEnchantItem item matches the criteria");
            return true;
        }

        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.isEnchantItem item does not match the criteria");
        return false;
    }
    public static String formatDroppedItems(List<ItemStack> drops) {
        StringBuilder sb = new StringBuilder();
        for (ItemStack itemStack : drops) {
            String itemName;
            // Sprawdź, czy przedmiot ma niestandardową nazwę
            if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
                itemName = itemStack.getItemMeta().getDisplayName();
            } else {
                // Użyj domyślnej nazwy materiału
                itemName = itemStack.getType().toString();
            }
            int quantity = itemStack.getAmount();
            sb.append(itemName).append(":").append(quantity).append(", ");
        }
        // Usuń ostatni przecinek i spację, jeśli istnieją
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }
    public static boolean isEloAllowed(Player player, Location location) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.isEloAllowed called");
        try {
            com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(location.getWorld());

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(weWorld);

            if (regions == null) {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.isEloAllowed No regions in world.");
                return true; // Jeśli nie ma regionów, domyślnie zezwalaj na Elo
            } else {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.isEloAllowed Found regions: " + regions);
            }

            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

            ApplicableRegionSet regionSet = regions.getApplicableRegions(BukkitAdapter.asBlockVector(location));

            // Zmieniamy sprawdzanie z flagi PVP na Twoją niestandardową flagę noElo
            StateFlag.State EloState = regionSet.queryState(localPlayer, BetterElo.IS_ELO_ALLOWED);
            boolean isEloAllowed = EloState != StateFlag.State.DENY; // Jeśli flaga noElo jest ustawiona na DENY, Elo nie jest dozwolone

            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.isEloAllowed Elo Allowed at location: " + location + " is " + isEloAllowed);

            return isEloAllowed;
        } catch (Exception e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Event.isEloAllowed: " + e.toString());
            return false;
        }
    }

}
