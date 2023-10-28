package betterbox.mine.game.betterelo;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
public class GuiManager implements Listener {
    private final FileRewardManager fileRewardManager;
    private final BetterElo mainClass;
    private final PluginLogger pluginLogger;
    public String periodType = null;
    private final DataManager dataManager;
    public GuiManager(FileRewardManager fileRewardManager, PluginLogger pluginLogger, BetterElo mainClass, DataManager dataManager) {
        this.fileRewardManager = fileRewardManager;
        this.dataManager = dataManager;
        this.pluginLogger = pluginLogger;
        this.mainClass = mainClass;
    }
    public void openSubGui(Player player, String rewardType) {
        Inventory subInv = Bukkit.createInventory(null, 9, "Select Top");
        createItem(subInv, Material.DIAMOND_SWORD, 1, "top1", "Top 1");
        createItem(subInv, Material.GOLDEN_SWORD, 3, "top2", "Top 2");
        createItem(subInv, Material.IRON_SWORD, 5, "top3", "Top 3");
        createItem(subInv, Material.WOODEN_SWORD, 7, "top4-10", "Top 4-10");
        player.openInventory(subInv);
    }
    public void openMainGui(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Set Rewards");
        createItem(inv, Material.APPLE, 1, "daily", "Daily Reward");
        createItem(inv, Material.BREAD, 3, "weekly", "Weekly Reward");
        createItem(inv, Material.DIAMOND, 5, "monthly", "Monthly Reward");
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
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!Arrays.asList("Set Rewards", "Add Items", "Select Top").contains(title)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null || currentItem.getType() == Material.AIR) {
            return;
        }
        String rewardType = currentItem.getItemMeta().getDisplayName();
        pluginLogger.log("GuiManager: onInventoryClick: rewardType: "+rewardType+" periodType: "+periodType);
        if (title.equals("Set Rewards")) {
                event.setCancelled(true);
                periodType = currentItem.getItemMeta().getDisplayName();
                openSubGui(player, rewardType);
        } else if (title.equals("Select Top")) {
            event.setCancelled(true);
            rewardType = currentItem.getItemMeta().getDisplayName();
            pluginLogger.log("GuiManager: onInventoryClick: rewardType: "+rewardType+" periodType: "+periodType);
            fileRewardManager.setRewardType(periodType, rewardType);
            List<ItemStack> currentRewards = fileRewardManager.loadRewards();
            Inventory inv = Bukkit.createInventory(null, 36, "Add Items");
            currentRewards.forEach(inv::addItem);
            createItem(inv, Material.GREEN_WOOL, 35, "Save", "Zapisz przedmioty");
            createItem(inv, Material.ORANGE_WOOL, 34, "Reset", "Resetuj czas");
            createItem(inv, Material.YELLOW_WOOL, 33, "Reedem", "Rozdaj nagrody");
            player.openInventory(inv);
        } else if (title.equals("Add Items")) {
            if (List.of("Save", "Reset", "Reedem").contains(rewardType)) {
                event.setCancelled(true);
                // Kod do obsługi przycisków funkcji
                Inventory inv = event.getInventory();
                if ("Save".equals(rewardType)) {
                    List<ItemStack> nonAirItems = Arrays.stream(inv.getContents())
                            .filter(item -> item != null && item.getType() != Material.AIR)
                            .filter(item -> !List.of("Save", "Reset", "Reedem").contains(item.getItemMeta().getDisplayName()))
                            .collect(Collectors.toList());
                    if (nonAirItems.isEmpty()) {
                        player.sendMessage("Brak przedmiotów do zapisania!");
                        return;
                    }
                    Inventory tempInventory = Bukkit.createInventory(null, 9, "Temporary Inventory");
                    nonAirItems.forEach(tempInventory::addItem);
                    fileRewardManager.saveRewards(tempInventory);
                    player.sendMessage("Przedmioty zostały zapisane!");
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG,"GuiManager: onInventoryClick: Save: rewardType: "+rewardType);
                    player.closeInventory();
                } else if ("Reset".equals(rewardType)) {

                        switch (periodType) {
                            case "daily":
                                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"GuiManager: onInventoryClick: Reset: "+periodType+" starting dataManager.updateLastScheduledTime");
                                mainClass.updateLastScheduledTime(periodType);
                                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"GuiManager: onInventoryClick: Reset: "+periodType+" dataManager.updateLastScheduledTime done, clearing points map");
                                dataManager.dailyPlayerPoints.clear();
                                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"GuiManager: onInventoryClick: Reset: clearing done "+periodType+" starting dataManager.saveDataToFileWeekly");
                                dataManager.saveDataToFileDaily();
                                player.sendMessage("Zresetowano nagrode : " + periodType);
                                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"GuiManager: onInventoryClick: Reset: done");
                                break;
                            case "weekly":
                                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"GuiManager: onInventoryClick: Reset: "+periodType+" starting dataManager.updateLastScheduledTime");
                                mainClass.updateLastScheduledTime(periodType);
                                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"GuiManager: onInventoryClick: Reset: "+periodType+" dataManager.updateLastScheduledTime done, clearing points map");
                                dataManager.weeklyPlayerPoints.clear();
                                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"GuiManager: onInventoryClick: Reset:Reset: clearing done "+periodType+"  starting dataManager.saveDataToFileWeekly");
                                dataManager.saveDataToFileWeekly();
                                player.sendMessage("Zresetowano nagrode : " + periodType);
                                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"GuiManager: onInventoryClick: Reset: done");
                                break;
                            case "monthly":
                                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"GuiManager: onInventoryClick: Reset: "+periodType+" starting dataManager.updateLastScheduledTime");
                                mainClass.updateLastScheduledTime(periodType);
                                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"GuiManager: onInventoryClick: Reset: "+periodType+" dataManager.updateLastScheduledTime done, clearing points map");
                                dataManager.monthlyPayerPoints.clear();
                                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"GuiManager: onInventoryClick: Reset:Reset: clearing done "+periodType+"  starting dataManager.saveDataToFileWeekly");
                                dataManager.saveDataToFileMonthly();
                                player.sendMessage("Zresetowano nagrode : " + periodType);
                                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"GuiManager: onInventoryClick: Reset: done");

                                break;
                            default:
                                pluginLogger.log(PluginLogger.LogLevel.WARNING,"GuiManager: onInventoryClick: Reset: Wrong reward type:"+periodType);
                                player.sendMessage("Nieznany typ nagrody: " + periodType);
                                return;
                        }
                        player.sendMessage(fileRewardManager.getRewardType() + " last scheduled time has been reset!");
                }
                else if ("Reedem".equals(rewardType)) {
                    mainClass.rewardTopPlayers(periodType);
                    pluginLogger.log("GuiManager: onInventoryClick: Reedem: periodType: "+periodType);
                    player.sendMessage("Nagrody zostały przyznane!");
                }
            } // Jeśli nie jest to przycisk funkcji, pozwól graczowi przesuwać przedmioty
        }
    }
}
