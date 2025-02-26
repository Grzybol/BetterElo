package betterbox.mine.game.betterelo;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Utils {
    static PluginLogger pluginLogger;
    static BetterElo betterElo;
    private static final Random random = new Random();
    private static ExtendedConfigManager conf;
    private static JavaPlugin plugin;
    private static Lang lang;
    public Utils(BetterElo betterElo, PluginLogger pluginLogger, ExtendedConfigManager conf, JavaPlugin plugin, Lang lang) {
        this.pluginLogger = pluginLogger;
        this.conf = conf;
        this.betterElo = betterElo;
        this.plugin = plugin;
        this.lang = lang;
        pluginLogger.log(PluginLogger.LogLevel.INFO, "Utils constructor called");

        updateLanguageStringsInConfig();


    }
    public enum ColorCode {
        BLACK("&0", ChatColor.BLACK),
        DARK_BLUE("&1", ChatColor.DARK_BLUE),
        DARK_GREEN("&2", ChatColor.DARK_GREEN),
        DARK_AQUA("&3", ChatColor.DARK_AQUA),
        DARK_RED("&4", ChatColor.DARK_RED),
        DARK_PURPLE("&5", ChatColor.DARK_PURPLE),
        GOLD("&6", ChatColor.GOLD),
        GRAY("&7", ChatColor.GRAY),
        DARK_GRAY("&8", ChatColor.DARK_GRAY),
        BLUE("&9", ChatColor.BLUE),
        GREEN("&a", ChatColor.GREEN),
        AQUA("&b", ChatColor.AQUA),
        RED("&c", ChatColor.RED),
        LIGHT_PURPLE("&d", ChatColor.LIGHT_PURPLE),
        YELLOW("&e", ChatColor.YELLOW),
        WHITE("&f", ChatColor.WHITE),
        OBFUSCATED("&k", ChatColor.MAGIC),
        BOLD("&l", ChatColor.BOLD),
        STRIKETHROUGH("&m", ChatColor.STRIKETHROUGH),
        UNDERLINE("&n", ChatColor.UNDERLINE),
        ITALIC("&o", ChatColor.ITALIC),
        RESET("&r", ChatColor.RESET);

        private final String code;
        private final ChatColor bukkitColor;

        ColorCode(String code, ChatColor bukkitColor) {
            this.code = code;
            this.bukkitColor = bukkitColor;
        }

        public String getCode() {
            return code;
        }

        public ChatColor getBukkitColor() {
            return bukkitColor;
        }

        public static String translateAlternateColorCodes(String message) {
            for (ColorCode color : values()) {
                message = message.replace(color.getCode(), color.getBukkitColor().toString());
            }
            return message;
        }
    }

    public static class FormatUtil {
        public static String applyFormatting(String input) {
            return ColorCode.translateAlternateColorCodes(input);
        }
    }
    public void updateLanguageStringsInConfig() {
        pluginLogger.log(PluginLogger.LogLevel.INFO, "Utils.updateLanguageStringsInConfig called");
        lang.enchantItemName = FormatUtil.applyFormatting(lang.enchantItemName);
        List<String> newEnchantItemLore = new ArrayList<>();
        for (String line : lang.enchantItemLore) {
            newEnchantItemLore.add(FormatUtil.applyFormatting(line));
        }
        lang.enchantItemLore = newEnchantItemLore;
        lang.enchantItemName = FormatUtil.applyFormatting(lang.enchantItemName);
        lang.currentBonusString = FormatUtil.applyFormatting(lang.currentBonusString);
        lang.mobDefenseLore = FormatUtil.applyFormatting(lang.mobDefenseLore);
        lang.averageDamageLore = FormatUtil.applyFormatting(lang.averageDamageLore);
        lang.mobDamageLore = FormatUtil.applyFormatting(lang.mobDamageLore);
    }
    public void updateAverageDamageOld(ItemStack item,int avgDmg,String transactionID) {
        if (item != null && item.hasItemMeta()) {
            pluginLogger.log(PluginLogger.LogLevel.REROLL, "updateAverageDamage called, betterElo.hasMobDamageAttribute(item): " + betterElo.hasMobDamageAttribute(item) + ", betterElo.hasAverageDamageAttribute(item): " + betterElo.hasAverageDamageAttribute(item),transactionID,avgDmg);

            if (betterElo.hasMobDamageAttribute(item) && betterElo.hasAverageDamageAttribute(item)) {
                ItemMeta itemMeta = item.getItemMeta();
                List<String> lore = new ArrayList<>(itemMeta.getLore());


                Iterator<String> iterator = lore.iterator();
                boolean updated = false;
                List<String> linesToRemove = new ArrayList<>();

                while (iterator.hasNext()) {
                    String line = iterator.next();
                    if (line.contains("Average Damage") || line.contains(lang.averageDamageLore)) {
                        if (!updated) {
                            // Update the first occurrence
                            pluginLogger.log(PluginLogger.LogLevel.REROLL, "Updating existing Average Damage line: " + line + " -> " + lang.averageDamageLore + avgDmg + "%",transactionID,avgDmg);
                            int index = lore.indexOf(line);
                            lore.set(index, lang.averageDamageLore + avgDmg + "%");
                            updated = true;
                        } else {
                            // Remove any additional occurrences
                            pluginLogger.log(PluginLogger.LogLevel.REROLL, "Removing extra Average Damage line: " + line,transactionID);
                            iterator.remove();
                        }
                    }
                }

                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Utils.updateAverageDamage lore: " + Arrays.toString(lore.toArray())+", lore again: "+lore,transactionID);

                itemMeta.setLore(lore);
                item.setItemMeta(itemMeta);
                betterElo.addAverageDamageAttribute(item, avgDmg);
            }
        }
    }
    public void updateAverageDamage(ItemStack item, int avgDmg, String transactionID) {
        if (item != null && item.hasItemMeta()) {
            pluginLogger.log(PluginLogger.LogLevel.REROLL, "updateAverageDamage called, betterElo.hasMobDamageAttribute(item): "
                    + betterElo.hasMobDamageAttribute(item)
                    + ", betterElo.hasAverageDamageAttribute(item): "
                    + betterElo.hasAverageDamageAttribute(item), transactionID, avgDmg);

            if (betterElo.hasMobDamageAttribute(item) && betterElo.hasAverageDamageAttribute(item)) {
                ItemMeta itemMeta = item.getItemMeta();
                List<String> lore = new ArrayList<>(itemMeta.getLore());

                boolean updated = false;
                List<Integer> indicesToRemove = new ArrayList<>(); // Store indices of duplicates

                for (int i = 0; i < lore.size(); i++) {
                    String line = lore.get(i);

                    if (line.contains("Average Damage") || line.contains(lang.averageDamageLore)) {
                        if (!updated) {
                            // Update first occurrence
                            pluginLogger.log(PluginLogger.LogLevel.REROLL, "Updating existing Average Damage line: "
                                    + line + " -> " + lang.averageDamageLore + avgDmg + "%", transactionID, avgDmg);
                            lore.set(i, lang.averageDamageLore + avgDmg + "%");
                            updated = true;
                        } else {
                            // Mark duplicates for removal
                            pluginLogger.log(PluginLogger.LogLevel.REROLL, "Marking extra Average Damage line for removal: " + line, transactionID);
                            indicesToRemove.add(i);
                        }
                    }
                }

                // Remove duplicate entries in reverse order to avoid index shifting issues
                for (int i = indicesToRemove.size() - 1; i >= 0; i--) {
                    lore.remove((int) indicesToRemove.get(i));
                }

                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Utils.updateAverageDamage final lore: " + lore, transactionID);

                itemMeta.setLore(lore);
                item.setItemMeta(itemMeta);
                betterElo.addAverageDamageAttributeNoLore(item, avgDmg);
            }
        }
    }

    public static int dropAverageDamage(String transactionID) {
        double rand = random.nextDouble();
        double x = Math.pow(-Math.log(rand), 0.44) * 18; // Adjusted exponential transformation and scale
        int bonus = (int) x;
        bonus = Math.min(bonus, 60);
        if(bonus==0){
            bonus=1;
        }
        pluginLogger.log(PluginLogger.LogLevel.REROLL, "Utils.dropAverageDamage bonus: " + bonus,transactionID,bonus);
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

    /*
    public static boolean checkAndRemoveEnchantItemOld(Player player) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.checkAndRemoveEnchantItem called, player : "+player);
        Inventory inventory = player.getInventory();
        ItemStack enchantItemStack = getEnchantItem();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.checkAndRemoveEnchantItem betterCoinStack: "+enchantItemStack);
        if (inventory.containsAtLeast(enchantItemStack, 1)) {
            inventory.removeItem(enchantItemStack);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.checkAndRemoveEnchantItem 1 Enchant Item found, removing : "+enchantItemStack);
            return true;
        }
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.checkAndRemoveEnchantItem 1 Enchant Item not found");
        return false;
    }
     */

    public static boolean checkAndRemoveEnchantItem(Player player) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.checkAndRemoveEnchantItem called, player : " + player);

        Inventory inventory = player.getInventory();

        for (ItemStack item : inventory.getContents()) {
            if (item != null && isEnchantItem(item)) {
                inventory.removeItem(new ItemStack(item.getType(), 1));
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.checkAndRemoveEnchantItem 1 Enchant Item found, removing : " + item);
                return true;
            }
        }

        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.checkAndRemoveEnchantItem 1 Enchant Item not found");
        return false;
    }

    public static ItemStack getEnchantItem(String transactionID) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Utils.getEnchantItem called",transactionID);
        Material material = Material.GHAST_TEAR;
        int amount = 1;

        ItemStack stack = new ItemStack(material, amount);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            try {
                meta.setDisplayName(lang.enchantItemName);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Utils.getEnchantItem meta.getDisplayName(): " + meta.getDisplayName(),   transactionID);
                //Component displayNameComponent = new Component("BetterCoin");
                //List<String> lore = ;
                meta.setLore(lang.enchantItemLore);
                // Dodajemy niestandardowy enchant, który nie wpływa na działanie itemu
                meta.addEnchant(Enchantment.LUCK, 1, true);

                // Ukrywamy wszystkie informacje o zaklęciach na itemie
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                stack.setItemMeta(meta);
                addEnchantItemAttribute(stack);
            }
            catch (Exception e){
                pluginLogger.log(PluginLogger.LogLevel.ERROR, "GuiManager.getEnchantItem: "+e.toString(),transactionID);
            }
        }
        return stack;
    }
    public static void addEnchantItemAttribute(ItemStack item){
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo.addEnchantItemAttribute called with item: "+item);
        if (item != null) {
            if(!item.hasItemMeta()){
                item.setItemMeta(Bukkit.getItemFactory().getItemMeta(item.getType()));
            }
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
            dataContainer.set(betterElo.enchanteItemKey, PersistentDataType.INTEGER, 1);
            item.setItemMeta(meta);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo.addEnchantItemAttribute value 1 was added to the item "+item);
        }else{
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "BetterElo.addEnchantItemAttribute null item!"+item);
        }
    }
    /*
    public static boolean isEnchantItemOld(ItemStack itemStack) {
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
     */

    public static boolean isEnchantItem(ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
            if (dataContainer.has(betterElo.enchanteItemKey, PersistentDataType.INTEGER)) {
                return true;
            }
        }
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
    public static void toggleMoneyPickup(Player player) {
        // Pobieramy bieżącą wartość. Jeśli brak metadanych, uznajemy, że wartość wynosi false
        boolean currentValue = false;
        if (!player.getMetadata("addMoneyOnPickup").isEmpty()) {
            currentValue = player.getMetadata("addMoneyOnPickup").get(0).asBoolean();
        }
        // Przełączanie wartości
        boolean newValue = !currentValue;
        player.setMetadata("addMoneyOnPickup", new FixedMetadataValue(plugin, newValue));
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA +"Automatic money pickup " +(newValue ? ChatColor.GREEN+"ENABLED" : ChatColor.RED+"DISABLED") + ".");
    }
    public static void enableMoneyPickup(Player player) {
        player.setMetadata("addMoneyOnPickup", new FixedMetadataValue(plugin, true));
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA +"Automatic money pickup "+ChatColor.GREEN+"ENABLED");
    }

}
