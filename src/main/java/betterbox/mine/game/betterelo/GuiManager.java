package betterbox.mine.game.betterelo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
public class GuiManager{
    private final FileRewardManager fileRewardManager;
    private final BetterElo mainClass;
    private final PluginLogger pluginLogger;
    public String periodType = null;
    public String rewardType;
    public String dropTable;
    private final CustomMobsFileManager mobsFileManager;
    private CustomMobs customMobs;
    private JavaPlugin plugin;
    private Random random = new Random();

    private final DataManager dataManager;
    public GuiManager(FileRewardManager fileRewardManager, PluginLogger pluginLogger, BetterElo mainClass, DataManager dataManager, CustomMobsFileManager mobsFileManager, CustomMobs customMobs, JavaPlugin plugin) {
        this.fileRewardManager = fileRewardManager;
        this.dataManager = dataManager;
        this.pluginLogger = pluginLogger;
        this.mainClass = mainClass;
        this.mobsFileManager=mobsFileManager;
        this.customMobs = customMobs;
        this.plugin = plugin;
    }
    public void openSubGui(Player player) {
        Inventory subInv = Bukkit.createInventory(null, 9, "Select Top");
        createItem(subInv, Material.DIAMOND_SWORD, 1, "top1", "Top 1");
        createItem(subInv, Material.GOLDEN_SWORD, 3, "top2", "Top 2");
        createItem(subInv, Material.IRON_SWORD, 5, "top3", "Top 3");
        createItem(subInv, Material.WOODEN_SWORD, 7, "top4-10", "Top 4-10");
        player.openInventory(subInv);
    }
    public void openMainGui(Player player) {
        Inventory inv = Bukkit.createInventory(null, 18, "Set Rewards");
        createItem(inv, Material.APPLE, 1, "daily", "Daily Reward");
        createItem(inv, Material.BREAD, 3, "weekly", "Weekly Reward");
        createItem(inv, Material.DIAMOND, 5, "monthly", "Monthly Reward");
        createItem(inv, Material.EMERALD, 7, "event", "Event Reward");
        //createItem(inv, Material.EMERALD, 14, "dropTable", "Create new Drop Table");
        player.openInventory(inv);
    }
    public void openDroptableGuiOld(Player player, String dropTableName) {
        dropTable= dropTableName;
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.openDroptableGui called. dropTableName:"+dropTableName+", dropTable: "+dropTable);
        Inventory inv = Bukkit.createInventory(null, 9, "Set Rewards");
        createItem(inv, Material.EMERALD, 4, "dropTable", "Create new Drop Table");
        player.openInventory(inv);
    }
    public void createItem(Inventory inv, Material material, int slot, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(List.of(description));
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }
    public void openDroptableGui(Player player, String dropTableName) {
        dropTable = dropTableName;
        periodType = "dropTable";
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.openDroptableGui called. dropTableName:" + dropTableName + ", dropTable: " + dropTable);

        Inventory inv = Bukkit.createInventory(null, 54, "Add Items"); // Zwiększ rozmiar inwentarza, aby pomieścić więcej przedmiotów
        /*
        HashMap<Double, ItemStack> drops = mobsFileManager.loadCustomDrops(dropTableName); // Wczytaj przedmioty z tabeli dropów

        // Umieść przedmioty w oknie GUI
        drops.values().forEach(inv::addItem);

         */

        List<CustomMobsFileManager.DropItem> drops = mobsFileManager.loadCustomDropsv2(dropTableName); // Wczytaj przedmioty z tabeli dropów

        // Umieść przedmioty w oknie GUI
        drops.stream().map(CustomMobsFileManager.DropItem::getItemStack).forEach(inv::addItem);

        // Dodaj przycisk do zapisania tabeli dropów
        createItem(inv, Material.GREEN_WOOL, 53, "Save", "Save drop table");

        player.openInventory(inv);
    }


    private void handleReRollEvent(InventoryClickEvent event, Inventory inv) {
        if (event.getSlot() == 2) { // Zapobieganie interakcji z wynikowym slotem
            event.setCancelled(true);
            return;
        }

        // Asynchroniczne sprawdzenie receptury i aktualizacja slotu wynikowego
        Bukkit.getScheduler().runTaskLater(plugin, () -> updateResultSlot(inv), 1L);
    }

    private void updateResultSlot(Inventory inv) {
        ItemStack item0 = inv.getItem(0);
        ItemStack item1 = inv.getItem(1);
        if (item0 != null && item1 != null && item0.hasItemMeta() && item1.hasItemMeta()) {
            ItemMeta meta0 = item0.getItemMeta();
            ItemMeta meta1 = item1.getItemMeta();
            boolean slot0Condition = meta0.getLore().stream().anyMatch(line -> line.contains("Average Damage"));
            boolean slot1Condition = meta1.getDisplayName().equals("test");
            if (slot0Condition && slot1Condition) {
                ItemStack result = item0.clone();
                ItemMeta resultMeta = result.getItemMeta();
                List<String> lore = new ArrayList<>(resultMeta.getLore());
                boolean mobDamage = false;
                for (int i = 0; i < lore.size(); i++) {
                    if(lore.get(i).contains("Mob Damage"))
                        mobDamage=true;
                    if (lore.get(i).contains("Average Damage")&& mobDamage) {
                        lore.set(i, customMobs.dropAverageDamage());
                        break;
                    }
                }
                resultMeta.setLore(lore);
                result.setItemMeta(resultMeta);
                inv.setItem(2, result);
            } else {
                inv.setItem(2, new ItemStack(Material.AIR));
            }
        }
    }


    public void openCustomSmithingTable(Player player) {
        Inventory smithingTable = Bukkit.createInventory(player, InventoryType.SMITHING, "Re-roll Average Damage bonus");

        // Ustawienie początkowych przedmiotów w GUI (przykładowo, można ustawić puste sloty lub zablokowane sloty)
        ItemStack item1 = new ItemStack(Material.AIR);  // Pierwszy slot na materiał
        ItemStack item2 = new ItemStack(Material.AIR);  // Drugi slot na narzędzie
        smithingTable.setItem(0, item1);  // Pierwszy slot
        smithingTable.setItem(1, item2);  // Drugi slot

        // Otworzenie ekwipunku dla gracza
        player.openInventory(smithingTable);
    }

    public void openReRollGui(Player player){
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.openReRollGui called, player: "+player.getName());
        Inventory inv = Bukkit.createInventory(null, 9, "AvgDmg bonus change");
        Material blank = Material.GRAY_STAINED_GLASS_PANE;
        createItem(inv, blank, 0, "", "");
        createItem(inv, blank, 1, "", "");
        createItem(inv, blank, 2, "", "");
        createItem(inv, blank, 4, "", "");
        createItem(inv, Material.GREEN_WOOL, 5, "Re-Roll Average Damage bonus", "Cost: 1x "+ChatColor.DARK_PURPLE+""+ChatColor.BOLD+"Enchant Item");
        createItem(inv, blank, 6, "", "");
        createItem(inv, blank, 7, "", "");
        createItem(inv, blank, 8, "", "");
        //createItem(inv, blank, 0, "", "");
        //createItem(inv, Material.EMERALD, 14, "dropTable", "Create new Drop Table");
        player.openInventory(inv);
    }
    public boolean checkAndRemoveBetterCoins(Player player) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.checkAndRemoveBetterCoins called, player : "+player);
        Inventory inventory = player.getInventory();
        ItemStack betterCoinStack = getBetterCoinStack();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.checkAndRemoveBetterCoins betterCoinStack: "+betterCoinStack);
        // Sprawdź, czy gracz ma co najmniej 64 BetterCoin w ekwipunku
        if (inventory.containsAtLeast(betterCoinStack, 64)) {
            // Usuń 64 sztuki BetterCoin z ekwipunku gracza
            inventory.removeItem(betterCoinStack);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.checkAndRemoveBetterCoins 64 BetterCoin found, removing : "+betterCoinStack);
            return true;
        }
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.checkAndRemoveBetterCoins 64 BetterCoin not found");
        return false;
    }
    public boolean checkAndRemoveEnchantItem(Player player) {
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
    public ItemStack getEnchantItem(){
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

    private ItemStack getBetterCoinStack() {
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
}
