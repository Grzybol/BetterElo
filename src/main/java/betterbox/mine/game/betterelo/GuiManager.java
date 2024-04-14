package betterbox.mine.game.betterelo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
public class GuiManager implements Listener {
    private final FileRewardManager fileRewardManager;
    private final BetterElo mainClass;
    private final PluginLogger pluginLogger;
    public String periodType = null;
    private String rewardType;
    private String dropTable;
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
    private void createItem(Inventory inv, Material material, int slot, String name, String description) {
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
    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClick(InventoryClickEvent event) {
        if(event.getCurrentItem().getType() == Material.GRAY_STAINED_GLASS_PANE || event.getCurrentItem().getType() == Material.GREEN_WOOL){
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.onInventoryClick green wool or blank pane clicked, cancelling..");
            event.setCancelled(true);

        }
        Player player = (Player) event.getWhoClicked();
        ItemStack currentItem = event.getCurrentItem();
        Inventory playerInventory = player.getInventory();
        ItemStack[] savedInventory = playerInventory.getContents();

        String title = event.getView().getTitle();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.onInventoryClick called. title:"+title);
        if (!Arrays.asList("Set Rewards", "Add Items", "Select Top", "AvgDmg bonus change").contains(title)) {
            return;
        }




        if (currentItem == null || currentItem.getType() == Material.AIR) {
            return;
        }
        Inventory inv;
        //String rewardTypeTemp = currentItem.getItemMeta().getDisplayName();
        switch (title) {
            case "Set Rewards":
                event.setCancelled(true);
                periodType = currentItem.getItemMeta().getDisplayName();
                if(periodType.equals("dropTable")){
                    List<ItemStack> currentRewards = fileRewardManager.loadRewards();
                    inv = Bukkit.createInventory(null, 36, "Add Items");
                    currentRewards.forEach(inv::addItem);
                    createItem(inv, Material.GREEN_WOOL, 35, "Save", "Save drop table");
                    player.openInventory(inv);
                    break;
                }
                openSubGui(player);
                break;
            case "Select Top":
                event.setCancelled(true);
                rewardType = currentItem.getItemMeta().getDisplayName();
                pluginLogger.log("GuiManager.onInventoryClick: rewardType:" + rewardType + " periodType:" + periodType);
                fileRewardManager.setRewardType(periodType, rewardType);
                List<ItemStack> currentRewards = fileRewardManager.loadRewards();
                inv = Bukkit.createInventory(null, 36, "Add Items");
                currentRewards.forEach(inv::addItem);
                createItem(inv, Material.GREEN_WOOL, 35, "Save", "Save rewards");
                //createItem(inv, Material.ORANGE_WOOL, 34, "Reset", "Resetuj czas");
                //createItem(inv, Material.YELLOW_WOOL, 33, "Reedem", "Rozdaj nagrody");
                player.openInventory(inv);
                break;
            case "Add Items":
                //save button check
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.onInventoryClick Add Items");
                if (currentItem.getType() == Material.GREEN_WOOL && (event.getSlot() == 35 || event.getSlot() == 53)) {
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.onInventoryClick Add Items - save called.");
                    event.setCancelled(true);
                    Inventory inventory = event.getInventory();
                    List<ItemStack> itemsToSave = new ArrayList<>();
                    for (int i = 0; i < inventory.getSize(); i++) {
                        if (i == 35 || i == 53) { // Pomijamy slot przycisku "Save"
                            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.onInventoryClick save button, skipping.");
                            continue;
                        }
                        ItemStack item = inventory.getItem(i);
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.onInventoryClick save: item: "+item);
                        if (item != null && item.getType() != Material.AIR) {
                            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.onInventoryClick no air save: item: "+item);
                                itemsToSave.add(item);
                            }

                    }

                    String fileName=periodType+"_"+rewardType;
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.onInventoryClick calling fileRewardManager.saveRewards("+fileName+",itemsToSave)");
                    if(periodType.equals("dropTable")){
                        fileName=dropTable;
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.onInventoryClick droptable: "+fileName);
                        fileRewardManager.saveCustomDrops(fileName, itemsToSave);
                    }else{
                        fileRewardManager.saveRewards(fileName, itemsToSave);
                    }

                }
                break;
            case "AvgDmg bonus change":
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.onInventoryClick Average Damage bonus re-roll");

                if (currentItem.getType() == Material.GREEN_WOOL && event.getSlot() == 5){
                    playerInventory.setContents(savedInventory);
                    event.setCancelled(true);
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.onInventoryClick Average Damage bonus re-roll clicked");
                    Inventory inventory = event.getInventory();
                    ItemStack item0 = inventory.getItem(3);
                    if (item0 != null && item0.hasItemMeta()) {
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.onInventoryClick reroll, item0: "+item0+", item0.hasItemMeta(): "+item0.hasItemMeta());
                        ItemMeta meta0 = item0.getItemMeta();
                        boolean slot0Condition = meta0.getLore().stream().anyMatch(line -> line.contains("Average Damage"));
                        ItemMeta meta  = item0.getItemMeta();
                        //List<String> lore = meta.getLore();
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.onInventoryClick reroll, slot0Condition: "+slot0Condition);

                        if (slot0Condition) {
                            ItemStack result = item0.clone();
                            ItemMeta resultMeta = result.getItemMeta();
                            List<String> lore = new ArrayList<>(resultMeta.getLore());
                            for (int i = 0; i < lore.size(); i++) {
                                if (lore.get(i).contains("Average Damage")) {
                                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.onInventoryClick reroll, Average Damage lore line found i: " + i);
                                    if(checkAndRemoveBetterCoins(player)) {
                                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.onInventoryClick reroll, player paid, re-rolling..." );
                                        lore.set(i, customMobs.dropAverageDamage());
                                        break;
                                    }
                                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.onInventoryClick reroll, player has no money for the re-roll." );
                                }
                            }
                            resultMeta.setLore(lore);
                            result.setItemMeta(resultMeta);
                            inventory.setItem(3, result);
                            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.onInventoryClick result placed back in slot 3");

                        }
                    }
                }
                break;
            }

    }
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Inventory closedInventory = event.getInventory();

        // Check if the closed inventory is the same one we're interested in
        if (event.getView().getTitle().equalsIgnoreCase("AvgDmg bonus change")) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.onInventoryClose: Checking items in closed GUI");

            ItemStack itemInSlot0 = closedInventory.getItem(3);
            if (itemInSlot0 != null && itemInSlot0.hasItemMeta()) {
                ItemMeta meta = itemInSlot0.getItemMeta();
                if (meta.getLore().stream().anyMatch(line -> line.contains("Average Damage"))) {
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.onInventoryClose: Item with 'Average damage' found in slot 0");

                    // Optional: Directly give back the item to the player's inventory
                    if (player.getInventory().addItem(itemInSlot0).size() == 0) {
                        // Item successfully added to player's inventory
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.onInventoryClose: Item returned to player inventory");
                        closedInventory.clear(3);  // Clear the slot after returning item
                    } else {
                        // Inventory full, drop item at player's location
                        player.getWorld().dropItem(player.getLocation(), itemInSlot0);
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.onInventoryClose: Inventory full, item dropped at player's location");
                        closedInventory.clear(3);  // Clear the slot
                    }
                }
            }
        }
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
                for (int i = 0; i < lore.size(); i++) {
                    if (lore.get(i).contains("Average Damage")) {
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
        createItem(inv, Material.GREEN_WOOL, 5, "Re-Roll Average Damage bonus", "Cost: 64x BetterCoin");
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
