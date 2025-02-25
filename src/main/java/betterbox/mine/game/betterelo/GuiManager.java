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
    private static Utils utils;

    private final DataManager dataManager;
    public GuiManager(FileRewardManager fileRewardManager, PluginLogger pluginLogger, BetterElo mainClass, DataManager dataManager, CustomMobsFileManager mobsFileManager, CustomMobs customMobs, JavaPlugin plugin, Utils utils) {
        this.fileRewardManager = fileRewardManager;
        this.dataManager = dataManager;
        this.pluginLogger = pluginLogger;
        this.mainClass = mainClass;
        this.mobsFileManager=mobsFileManager;
        this.customMobs = customMobs;
        this.plugin = plugin;
        this.utils = utils;
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

        List<CustomMobsFileManager.DropItem> drops = mobsFileManager.loadCustomDropsv2_old(dropTableName); // Wczytaj przedmioty z tabeli dropów

        // Umieść przedmioty w oknie GUI
        drops.stream().map(CustomMobsFileManager.DropItem::getItemStack).forEach(inv::addItem);

        // Dodaj przycisk do zapisania tabeli dropów
        createItem(inv, Material.GREEN_WOOL, 53, "Save", "Save drop table");

        player.openInventory(inv);
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
        ItemStack betterCoinStack = Utils.getBetterCoinStack();
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




}
