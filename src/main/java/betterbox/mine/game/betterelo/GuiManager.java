package betterbox.mine.game.betterelo;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
public class GuiManager implements Listener {
    private final FileRewardManager fileRewardManager;
    private final BetterElo mainClass;
    private final PluginLogger pluginLogger;
    public String periodType = null;
    private String rewardType;
    private String dropTable;

    private final DataManager dataManager;
    public GuiManager(FileRewardManager fileRewardManager, PluginLogger pluginLogger, BetterElo mainClass, DataManager dataManager) {
        this.fileRewardManager = fileRewardManager;
        this.dataManager = dataManager;
        this.pluginLogger = pluginLogger;
        this.mainClass = mainClass;
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
    public void openDroptableGui(Player player, String dropTableName) {

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
    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClick(InventoryClickEvent event) {


        String title = event.getView().getTitle();
        if (!Arrays.asList("Set Rewards", "Add Items", "Select Top").contains(title)) {
            return;
        }
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.onInventoryClick called. title:"+title);

        Player player = (Player) event.getWhoClicked();
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null || currentItem.getType() == Material.AIR) {
            return;
        }
        //String rewardTypeTemp = currentItem.getItemMeta().getDisplayName();
        switch (title) {
            case "Set Rewards":
                event.setCancelled(true);
                periodType = currentItem.getItemMeta().getDisplayName();
                if(periodType.equals("dropTable")){
                    List<ItemStack> currentRewards = fileRewardManager.loadRewards();
                    Inventory inv = Bukkit.createInventory(null, 36, "Add Items");
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
                Inventory inv = Bukkit.createInventory(null, 36, "Add Items");
                currentRewards.forEach(inv::addItem);
                createItem(inv, Material.GREEN_WOOL, 35, "Save", "Save rewards");
                //createItem(inv, Material.ORANGE_WOOL, 34, "Reset", "Resetuj czas");
                //createItem(inv, Material.YELLOW_WOOL, 33, "Reedem", "Rozdaj nagrody");
                player.openInventory(inv);
                break;
            case "Add Items":
                //save button check
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.onInventoryClick Add Items");
                if (currentItem.getType() == Material.GREEN_WOOL && event.getSlot() == 35) {
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManager.onInventoryClick Add Items - save called.");
                    event.setCancelled(true);
                    Inventory inventory = event.getInventory();
                    List<ItemStack> itemsToSave = new ArrayList<>();
                    for (int i = 0; i < inventory.getSize(); i++) {
                        if (i != 35) { // Pomijamy slot przycisku "Save"
                            ItemStack item = inventory.getItem(i);
                            if (item != null && item.getType() != Material.AIR) {
                                itemsToSave.add(item);
                            }
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
        }
    }
}
