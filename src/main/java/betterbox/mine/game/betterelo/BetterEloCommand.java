package betterbox.mine.game.betterelo;

import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class BetterEloCommand implements CommandExecutor, TabCompleter {

    private final DataManager dataManager;
    private final JavaPlugin plugin;
    private final PluginLogger pluginLogger;
    private final GuiManager guiManager; // Dodajemy referencję do GuiManager
    private final BetterElo betterElo;
    private final ExtendedConfigManager configManager;
    private final Event event;
    private final CustomMobsFileManager customMobsFileManager;
    private final PlayerKillDatabase PKDB;
    private Hologram hologramMain;
    private Hologram hologramDaily;
    private Hologram hologramWeekly;
    private Hologram hologramMonthly;
    private Hologram hologramEvent;
    private CustomMobs customMobs;
    private BukkitTask eventHoloTask;
    private final Lang lang;

    public BetterEloCommand(JavaPlugin plugin, DataManager dataManager, GuiManager guiManager, PluginLogger pluginLogger, BetterElo betterElo, ExtendedConfigManager configManager, Event event, PlayerKillDatabase PKDB, CustomMobs customMobs, CustomMobsFileManager customMobsFileManager, Lang lang) {
        this.dataManager = dataManager;
        this.plugin = plugin;
        this.customMobs = customMobs;
        this.guiManager = guiManager; // Inicjalizujemy referencję do GuiManager
        this.pluginLogger = pluginLogger;
        this.betterElo = betterElo; // Inicjalizujemy referencję do BetterElo
        this.configManager = configManager;
        this.event = event;
        this.customMobsFileManager = customMobsFileManager;
        this.PKDB = PKDB;
        this.lang = lang;


    }



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String transactionID = UUID.randomUUID().toString();
        switch (args.length) {
            case 0:
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    int rank = dataManager.getPlayerRank(player.getUniqueId().toString());
                    double points = dataManager.getPoints(player.getUniqueId().toString(), "main");
                    points = (double) Math.round(points * 100) / 100;
                    sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + "Your rank: " + ChatColor.GREEN + rank);
                    sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + "Your points: " + ChatColor.GREEN + points);
                } else {
                    sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " This command can only be used by online players.");
                }
                break;
            case 1:
                switch (args[0].toLowerCase()) {
                    case "autobank":
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            if (sender.hasPermission("betterelo.autobank")) {
                                Utils.toggleMoneyPickup(player);
                                //player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + "Automatic money pickup " + (player.getMetadata("addMoneyOnPickup").get(0).asBoolean() ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled") + ".");
                            } else {
                                noPermission(sender);
                            }
                        }
                        break;
                    case "language":
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            if (sender.isOp()) {
                                lang.loadLangFile();
                            } else {
                                noPermission(sender);
                            }
                        }
                        break;
                    case "checkitem":
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            ItemStack itemInHand = player.getInventory().getItemInMainHand();
                            if (itemInHand != null) {
                                ItemMeta itemMeta = itemInHand.getItemMeta();
                                if (itemMeta != null) {
                                    int avgdmg = betterElo.getAverageDamageAttribute(itemInHand);
                                    int[] dmg = betterElo.getMobDamageAttribute(itemInHand);
                                    sender.sendMessage("avgdmg: " + avgdmg + " dmg: " + dmg[0] + "-" + dmg[1]);
                                }
                            }
                        }
                        break;
                    case "enchantitem":
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterEloCommand.onCommand enchantitem called, sender: " + sender);
                        if (!sender.isOp()) {
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " You don't have permission to use that command!");
                            break;
                        }
                        createEncahntItem(sender,transactionID);
                        break;
                    case "reroll":
                        if (sender.isOp() || sender.hasPermission("betterelo.reroll")) {
                            handleRerollCommand(sender);
                        } else {
                            noPermission(sender);
                        }
                        break;
                    case "killallmobs":
                        if (!sender.isOp()) {
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " You don't have permission to use that command!");
                            break;
                        }
                        betterElo.killAllCustomMobs();
                        break;
                    case "addelytra":
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            if (!player.isOp()) {
                                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " You don't have permission to use that command!");
                                break;
                            }
                            ItemStack itemInHand = player.getInventory().getItemInMainHand();
                            if (itemInHand.getType() != Material.AIR && itemInHand.getType().toString().contains("CHESTPLATE")) {
                                addElytraLore(player, itemInHand);
                                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Elytra effect added.");
                            }
                        } else {
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " This command can be used only by as a player.");
                        }
                        break;
                    case "stopevent":
                        if (sender.isOp()) {
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Event stopped,data removed.");
                            betterElo.stopEvent();
                        } else {
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " You don't have permission to use that command!");
                        }
                        break;
                    case "info":
                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Better Elo system for BetterBox and BetterServer.");
                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Author: " + plugin.getDescription().getAuthors());
                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Version: " + plugin.getDescription().getVersion());
                        break;
                    case "help":
                        handleHelpCommand(sender);
                        break;
                    case "top10":
                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Top 10 players in the ranking:");
                        for (int i = 1; i <= 10; i++) {
                            String playerName = dataManager.getPlayerAtPosition(i, dataManager.playerPoints);
                            double points = dataManager.getPointsAtPosition(i, dataManager.playerPoints);
                            points = (double) Math.round(points * 100) / 100;
                            if (playerName != null) {
                                sender.sendMessage(ChatColor.AQUA.toString() + i + ". " + ChatColor.GREEN + playerName + ChatColor.AQUA + " - Points: " + ChatColor.GREEN + points);
                            }
                        }
                        break;
                    case "daily":
                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Top 10 players in the daily ranking:");
                        for (int i = 1; i <= 10; i++) {
                            String playerName = dataManager.getPlayerAtPosition(i, dataManager.dailyPlayerPoints);
                            double points = dataManager.getPointsAtPosition(i, dataManager.dailyPlayerPoints);
                            points = (double) Math.round(points * 100) / 100;
                            if (playerName != null) {
                                sender.sendMessage(ChatColor.AQUA.toString() + i + ". " + ChatColor.GREEN + playerName + ChatColor.AQUA + " - Points: " + ChatColor.GREEN + points);
                            }
                        }
                        break;
                    case "weekly":
                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Top 10 players in the weekly ranking:");
                        for (int i = 1; i <= 10; i++) {
                            String playerName = dataManager.getPlayerAtPosition(i, dataManager.weeklyPlayerPoints);
                            double points = dataManager.getPointsAtPosition(i, dataManager.weeklyPlayerPoints);
                            points = (double) Math.round(points * 100) / 100;
                            if (playerName != null) {
                                sender.sendMessage(ChatColor.AQUA.toString() + i + ". " + ChatColor.GREEN + playerName + ChatColor.AQUA + " - Points: " + ChatColor.GREEN + points);
                            }
                        }
                        break;
                    case "monthly":
                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Top 10 players in the monthly ranking:");
                        for (int i = 1; i <= 10; i++) {
                            String playerName = dataManager.getPlayerAtPosition(i, dataManager.monthlyPayerPoints);
                            double points = dataManager.getPointsAtPosition(i, dataManager.monthlyPayerPoints);
                            points = (double) Math.round(points * 100) / 100;
                            if (playerName != null) {
                                sender.sendMessage(ChatColor.AQUA.toString() + i + ". " + ChatColor.GREEN + playerName + ChatColor.AQUA + " - Points: " + ChatColor.GREEN + points);
                            }
                        }
                        break;
                    case "claim":
                        if (!(sender instanceof Player)) {
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " This command can only be used by online players.");
                            return true;
                        }

                        Player player = (Player) sender;
                        String playerName = player.getName();

                        File rewardsFile = new File(plugin.getDataFolder(), "offlineRewards.yml");
                        if (!rewardsFile.exists()) return true;

                        FileConfiguration rewardsConfig = YamlConfiguration.loadConfiguration(rewardsFile);
                        List<ItemStack> rewardItems = (List<ItemStack>) rewardsConfig.getList(playerName);

                        if (rewardItems != null && !rewardItems.isEmpty()) {
                            for (ItemStack item : rewardItems) {
                                player.getInventory().addItem(item);
                                pluginLogger.log(PluginLogger.LogLevel.INFO, "BetterEloCommand: claim: player: " + player.getName() + " reward: " + item);
                            }
                            rewardsConfig.set(playerName, null); // Usuń przyznane nagrody z pliku
                            pluginLogger.log(PluginLogger.LogLevel.INFO, "BetterEloCommand: claim: player: " + player.getName() + " rewards claimed.");
                            try {
                                rewardsConfig.save(rewardsFile);
                            } catch (IOException e) {
                                pluginLogger.log(PluginLogger.LogLevel.ERROR, "BetterEloCommand: claim: Error while saving rewards configuration: " + e);
                            }
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Rewards claimed!");
                        } else {
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " No rewards assigned.");
                        }

                        break;
                    case "timeleft":
                        handleTimeLeft(sender);
                        break;
                    case "setrewards":
                        handleSetRewards(sender);
                        break;
                    case "reload":
                        handleReloadCommand(sender, transactionID);
                        break;
                    case "event":
                        if (sender.isOp()) {
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " to start the event use /be event <duration> <h/m>");
                        }
                        if (sender instanceof Player) {
                            if (betterElo.isEventEnabled) {
                                player = (Player) sender;
                                double points = dataManager.getPoints(player.getUniqueId().toString(), "event");
                                points = (double) Math.round(points * 100) / 100;
                                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + "Event active! Time left: " + formatTime(betterElo.getRemainingTimeForRewards("event")));
                                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + "Your rank: " + dataManager.getPlayerRank(player.getUniqueId().toString(), "event"));
                                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + "Your points: " + points);
                            } else {
                                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + "Event is not active!");
                            }
                        }
                        break;

                    default:
                        // /be <player_name> - Information about a specific player's rank and points
                        playerName = args[0];

                        // Pobierz obiekt OfflinePlayer dla danego gracza
                        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(playerName);

                        // Sprawdź, czy gracz istnieje w bazie danych pluginu
                        String playerUUID = targetPlayer.getUniqueId().toString();
                        if (!dataManager.playerExists(playerUUID, "main")) {
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " Player with the name " + ChatColor.GREEN + playerName + ChatColor.AQUA + " has never played on this server.");
                            return true;
                        }

                        // Sprawdź, czy gracz o podanej nazwie istnieje w rankingu
                        int rank = dataManager.getPlayerRank(playerUUID);
                        if (rank == -1) {
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " Player with the name " + ChatColor.GREEN + playerName + ChatColor.AQUA + " was not found in the ranking.");
                            return true;
                        }

                        double points = dataManager.getPoints(playerUUID, "main");
                        points = (double) Math.round(points * 100) / 100;
                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Player " + ChatColor.GREEN + playerName + ChatColor.AQUA + " is ranked " + ChatColor.GREEN + rank + ChatColor.AQUA + " in main ranking.");
                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Player's points: " + ChatColor.GREEN + points + ChatColor.AQUA + " in main ranking.");
                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Player " + ChatColor.GREEN + playerName + ChatColor.AQUA + " is ranked " + ChatColor.GREEN + dataManager.getPlayerRank(playerUUID, "daily") + ChatColor.AQUA + ".");
                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Player's points: " + ChatColor.GREEN + dataManager.getPoints(playerUUID, "daily") + ChatColor.AQUA + " in daily ranking.");
                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Player " + ChatColor.GREEN + playerName + ChatColor.AQUA + " is ranked " + ChatColor.GREEN + dataManager.getPlayerRank(playerUUID, "weekly") + ChatColor.AQUA + ".");
                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Player's points: " + ChatColor.GREEN + dataManager.getPoints(playerUUID, "weekly") + ChatColor.AQUA + " in weekly ranking.");
                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Player " + ChatColor.GREEN + playerName + ChatColor.AQUA + " is ranked " + ChatColor.GREEN + dataManager.getPlayerRank(playerUUID, "monthly") + ChatColor.AQUA + ".");
                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Player's points: " + ChatColor.GREEN + dataManager.getPoints(playerUUID, "monthly") + ChatColor.AQUA + " in monthly ranking.");
                        break;
                }
                break;
            case 2:
                switch (args[0].toLowerCase()) {
                    case "forcespawn":
                        if (sender.isOp() || sender.hasPermission("betterelo.forcespawn")) {
                            customMobs.spawnerForceSpawn(args[1]);
                        }
                        break;
                    case "droptable":
                        if (sender.isOp() || sender.hasPermission("betterelo.droptable")) {
                            handleCreateDropTable(sender, args[1]);
                        }
                        break;
                    case "antyweb":
                        if ((sender.hasPermission("betterelo.antyweb") || sender.isOp()) && sender instanceof Player) {
                            Player player = ((Player) sender).getPlayer();
                            assert player != null;
                            ItemStack itemInHand = player.getInventory().getItemInMainHand();
                            if (itemInHand.getType().isAir()) {
                                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + "You must hold an item in your hand to add Antyweb lore!");
                                return true;
                            }
                            int radius;
                            try {
                                // Spróbuj przekonwertować argument na liczbę
                                radius = Integer.parseInt(args[1]);
                            } catch (NumberFormatException e) {
                                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + "Invalid radius! Please provide a number.");
                                return true;
                            }
                            addAntywebLore(player, itemInHand, radius);
                        }
                        break;
                    case "zephyr":
                        if ((sender.hasPermission("betterelo.zephyr") || sender.isOp()) && sender instanceof Player) {
                            Player player = ((Player) sender).getPlayer();
                            assert player != null;
                            ItemStack itemInHand = player.getInventory().getItemInMainHand();
                            if (itemInHand.getType().isAir()) {
                                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + "You must hold an item in your hand to add Zephyr lore!");
                                return true;
                            }
                            int power;
                            try {
                                // Spróbuj przekonwertować argument na liczbę
                                power = Integer.parseInt(args[1]);
                            } catch (NumberFormatException e) {
                                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + "Invalid radius! Please provide a number.");
                                return true;
                            }
                            addZephyrLore(player, itemInHand, power);
                        }
                        break;
                    case "holo":
                        if (sender.isOp()) {
                            if (sender instanceof Player) {
                                Player player = (Player) sender;
                                Location where = player.getLocation();// The location where the hologram will be placed
                                HolographicDisplaysAPI api = HolographicDisplaysAPI.get(plugin); // The API instance for your plugin

                                if (args[1].equals("event")) {
                                    if (hologramEvent == null) {
                                        HoloTop(dataManager.eventPlayerPoints, where, api);
                                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Holo for Event created!");
                                    } else {
                                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " Holo for Event already exists!!");
                                    }
                                }
                            } else {
                                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + "Event is not active!");
                            }
                        }
                        break;
                    case "top":
                        try {
                            int position = Integer.parseInt(args[1]);
                            String playerName = dataManager.getPlayerAtPosition(position, dataManager.playerPoints);
                            double points = dataManager.getPointsAtPosition(position, dataManager.playerPoints);
                            points = (double) Math.round(points * 100) / 100;
                            if (playerName != null) {
                                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Player in position " + ChatColor.GREEN + position + ": " + ChatColor.GREEN + playerName);
                                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Player's points: " + ChatColor.GREEN + points);
                            } else {
                                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " No player in position " + ChatColor.GREEN + position + ChatColor.DARK_RED + " in the ranking.");
                            }
                        } catch (NumberFormatException e) {
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " Please enter a valid ranking position number.");
                        }
                        break;
                    case "ban":
                        if (sender.isOp()) {
                            handleBanCommand(sender, args[1]);
                            betterElo.notiyBannedPlayer(args[1]);
                        } else {
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + "You don't have permission to use that command!");
                        }
                        break;
                    case "firework":
                        if (sender.isOp()) {
                            try {
                                int power = Integer.parseInt(args[1]);
                                Player player = (Player) sender;
                                createInfiniteFireworkItem(player, power);
                                // Tutaj możesz wykonać logikę, jeśli args[1] jest liczbą całkowitą
                            } catch (NumberFormatException e) {
                                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + "Command usage /be firework <power> - where power is INT!");
                            }
                            return true;
                        } else {
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " You don't have permission to use that command!");
                        }
                        break;
                    default:
                        // Obsługa dla przypadków, gdy żadna z opcji nie pasuje do argumentu
                        // Możesz dodać tutaj odpowiednią logikę lub komunikat błędu
                        break;
                }

            case 3:
                if (sender.isOp()) {
                    switch (args[0]) {
                        case "spawnmob":
                            pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "BetterEloCommand.OnCommand calling handleCustomMobsCommands(sender)");
                            try {
                                handleCustomMobsCommands(sender, args[1], Integer.parseInt(args[2]));
                            } catch (Exception e) {
                                pluginLogger.log(PluginLogger.LogLevel.ERROR, "BetterEloCommand.OnCommand spawnmob command generated exception: " + e);
                            }
                            break;

                        case "startevent":
                            int eventDuration;
                            try {
                                eventDuration = Integer.parseInt(args[1]);
                            } catch (NumberFormatException e) {
                                pluginLogger.log(PluginLogger.LogLevel.ERROR, "Invalid event duration: " + args[1]);
                                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " Invalid event duration!");
                                return true;
                            }

                            String eventUnit = args[2];
                            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterEloCommand.onCommand.event called. Duration:" + args[1] + " timeUnit:" + args[2]);
                            betterElo.eventDuration = eventDuration;
                            betterElo.eventUnit = eventUnit;
                            betterElo.isEventEnabled = true;
                            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterElo: onEnable: Planowanie nagród event...");

                            long periodMillis = 0;
                            switch (eventUnit) {
                                case "h":
                                    periodMillis = TimeUnit.HOURS.toMillis(eventDuration);
                                    break;
                                case "m":
                                    periodMillis = TimeUnit.MINUTES.toMillis(eventDuration);
                                    break;
                                default:
                                    pluginLogger.log(PluginLogger.LogLevel.ERROR, "BetterEloCommand.onCommand.event: eventUnit: " + eventUnit);
                                    sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " Invalid time unit!");
                                    return true;
                            }

                            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterEloCommand.onCommand.event: scheduling event rewards periodMillis:" + periodMillis);
                            betterElo.scheduleRewards("event", periodMillis, false);
                            betterElo.rewardStates.put("event", true);
                            betterElo.loadRewards();
                            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterEloCommand.onCommand.event: calling betterElo.updateLastScheduledTime(event)");
                            betterElo.updateLastScheduledTime("event");
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Event started! Duration " + eventDuration + " " + eventUnit);
                            break;

                        case "setattribute":
                            Player player = (Player) sender;
                            ItemStack item = player.getInventory().getItemInMainHand();
                            if (item != null && !item.getType().equals("AIR")) {
                                switch (args[1]) {
                                    case "mobdefense":
                                        betterElo.addMobDefenseAttribute(item, Integer.parseInt(args[2]));
                                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " MobDefense attribute set with value " + args[2]);
                                        break;
                                    case "mobdamage":
                                        betterElo.addMobDamageAttribute(item, args[2], transactionID);
                                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " mobdamage attribute set with value " + args[2]);
                                        break;
                                    case "averagedamage":
                                        betterElo.addAverageDamageAttribute(item, Integer.parseInt(args[2]));
                                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " averagedamage attribute set with value " + args[2]);
                                        break;
                                }
                            }

                        default:
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " You don't have permission to use that command!");
                            break;
                    }
                } else {
                    sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " You don't have permission to use that command!");
                    return true;
                }
                break;
            case 4:
                if (args[0].equalsIgnoreCase("add")) {
                    handleAddPointsCommand(sender, args[1], Double.valueOf(args[2]), args[3]);
                } else if (args[0].equalsIgnoreCase("sub")) {
                    handleSubtractPointsCommand(sender, args[1], Double.valueOf(args[2]), args[3]);
                }
                break;

            case 6:
                if (args[0].equalsIgnoreCase("addspawner")) {
                    try {
                        handleAddSpawnerCommand(sender, args[1], args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
                    } catch (Exception e) {
                        pluginLogger.log(PluginLogger.LogLevel.ERROR, "BetterEloCommand.onCommand.addspawner exception " + e.getMessage());
                    }
                }


        }
        return true;
    }

    private void handleCustomMobsCommands(CommandSender sender, String mobName, int mobCount) {
        String transactionID = UUID.randomUUID().toString();
        pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "BetterEloCommand.handleCustomMobsCommands called, sender: " + sender.getName() + ", mobName: " + mobName + ", mobCount: " + mobCount);
        if (sender.isOp() && sender instanceof Player) {
            customMobs.spawnModifiedZombie((Player) sender, mobName, mobCount, transactionID);
        } else {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " You don't have permission to use that command!");

        }
    }

    private boolean handleAddPointsCommand(CommandSender sender, String player, Double points, String rankingType) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterEloCommand: handleAddPointsCommand called by " + sender.getName());
        if (sender.isOp()) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterEloCommand: handleReloadCommand: calling event.addPoints(getOfflinePlayerUUID(player), points, rankingType)");
            event.addPoints(getOfflinePlayerUUID(player), points, rankingType);
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " " + points + " points has been added to " + player + "'s account in " + rankingType);
            return true;
        } else {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " You don't have permission to use that command!");
            return false;
        }

    }

    private boolean handleSubtractPointsCommand(CommandSender sender, String player, Double points, String rankingType) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterEloCommand: handleSubtractPointsCommand called by " + sender.getName());
        if (sender.isOp()) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterEloCommand: handleReloadCommand: calling event.subtractPoints(getOfflinePlayerUUID(player), points, rankingType)");
            event.subtractPoints(getOfflinePlayerUUID(player), points, rankingType);
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " " + points + " points has been removed from " + player + "'s account in " + rankingType);
            return true;
        } else {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " You don't have permission to use that command!");
            return false;
        }

    }

    private boolean handleReloadCommand(CommandSender sender, String transactionID) {
        if (sender.hasPermission("betterelo.reload")) {
            configManager.ReloadConfig();
            pluginLogger.log(PluginLogger.LogLevel.INFO, "BetterElo config.yml reloaded!", transactionID);
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " BetterElo config reloaded!", transactionID);
            customMobsFileManager.loadSpawners();
            pluginLogger.log(PluginLogger.LogLevel.INFO, "BetterElo spawners.yml reloaded", transactionID);
            customMobs.loadCustomMobs(transactionID);
            //customMobsFileManager.loadCustomDrops();
            return true;
        } else {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterEloCommand: handleReloadCommand: sender " + sender + " dont have permission to use /br tl", transactionID);
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo] " + ChatColor.DARK_RED + "You don't have permission to use this command!");
            return false;
        }
    }

    private boolean handleBanCommand(CommandSender sender, String banName) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterEloCommand: handleBanCommand called with parameters " + banName);
        if (sender.hasPermission("betterelo.ban")) {
            /*
            if (!(sender instanceof Player)) {

                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo] " + ChatColor.DARK_RED +"You don't have permission to use this command!");
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterEloCommand: handleBanCommand: sender " + sender + " don't have permission to use /br tl");
                return false;
            }

             */

            Player player = (Player) sender;
            pluginLogger.log(PluginLogger.LogLevel.INFO, "Banning " + banName + ". Points in main ranking: " + dataManager.getPoints(getOfflinePlayerUUID(banName), "main") + ", points to redeem " + (dataManager.getPoints(getOfflinePlayerUUID(banName), "main") - 1000d));
            pluginLogger.log(PluginLogger.LogLevel.INFO, "Banning " + banName + ". Points in daily ranking: " + dataManager.getPoints(getOfflinePlayerUUID(banName), "daily") + ", points to redeem " + (dataManager.getPoints(getOfflinePlayerUUID(banName), "daily") - 1000d));
            pluginLogger.log(PluginLogger.LogLevel.INFO, "Banning " + banName + ". Points in weekly ranking: " + dataManager.getPoints(getOfflinePlayerUUID(banName), "weekly") + ", points to redeem " + (dataManager.getPoints(getOfflinePlayerUUID(banName), "weekly") - 1000d));
            pluginLogger.log(PluginLogger.LogLevel.INFO, "Banning " + banName + ". Points in monthly ranking: " + dataManager.getPoints(getOfflinePlayerUUID(banName), "monthly") + ", points to redeem " + (dataManager.getPoints(getOfflinePlayerUUID(banName), "monthly") - 1000d));
            // Pobierz listę interakcji dla gracza, którego chcesz zbanować
            HashMap<String, HashMap<String, Double>> interactionsMap = PKDB.getPlayerInteractions(banName);

            if (interactionsMap.isEmpty()) {
                sender.sendMessage("Brak interakcji do zbanowania dla gracza " + banName);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterEloCommand: handleBanCommand no interactions for " + banName);
                return false;
            }
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Banning player " + banName);
            try {
                double totalPointsDistributed = 0;
                for (Map.Entry<String, HashMap<String, Double>> entry : interactionsMap.entrySet()) {
                    String rankingType = entry.getKey();
                    HashMap<String, Double> interactions = entry.getValue();
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2, "BetterEloCommand: handleBanCommand: starting loop " + rankingType);

                    try {
                        for (Map.Entry<String, Double> interactionEntry : interactions.entrySet()) {

                            String otherPlayer = interactionEntry.getKey();
                            double totalPoints = interactionEntry.getValue();
                            pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3, "BetterEloCommand: handleBanCommand: starting loop2 " + otherPlayer + " " + totalPoints + " " + rankingType);
                            pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3, "BetterEloCommand: handleBanCommand: calling getOfflinePlayerUUID with parameters " + otherPlayer);
                            String UUID = getOfflinePlayerUUID(otherPlayer);

                            if (totalPoints > 0) {
                                // Gracz zasługuje na karę (odejmowanie punktów)
                                pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3, "BetterEloCommand: handleBanCommand: calling event.addPoints with parameters: " + getOfflinePlayerUUID(otherPlayer) + " " + Math.abs(totalPoints) + " " + rankingType);
                                event.addPoints(UUID, totalPoints, rankingType);
                                pluginLogger.log(PluginLogger.LogLevel.INFO, "BAN COMMAND - Player " + otherPlayer + " gained " + totalPoints + " in " + rankingType + " ranking after banning " + banName);
                                sender.sendMessage("Gracz " + otherPlayer + " otrzymał " + totalPoints + " punktów w trybie " + rankingType);
                            } else if (totalPoints < 0) {
                                // Gracz nie zasługuje na karę (dodawanie punktów)
                                pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3, "BetterEloCommand: handleBanCommand: calling event.subtractPoints with parameters: " + getOfflinePlayerUUID(otherPlayer) + " " + Math.abs(totalPoints) + " " + rankingType);
                                event.subtractPoints(UUID, Math.abs(totalPoints), rankingType);
                                pluginLogger.log(PluginLogger.LogLevel.INFO, "BAN COMMAND - Player " + otherPlayer + " lost " + totalPoints + " in " + rankingType + " ranking");
                                sender.sendMessage("Gracz " + otherPlayer + " stracił " + totalPoints + " punktów nagrody w trybie " + rankingType);
                            } else {
                                pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL3, "Gracz " + otherPlayer + " nie ma żadnych punktów do zmiany w trybie " + rankingType);
                                sender.sendMessage("Gracz " + otherPlayer + " nie ma żadnych punktów do zmiany w trybie " + rankingType);
                            }
                            totalPointsDistributed += totalPoints;

                        }
                    } catch (Exception e) {
                        pluginLogger.log(PluginLogger.LogLevel.ERROR, "BetterEloCommand: handleBanCommand loop2 exception " + e.getMessage());
                    }
                }
                pluginLogger.log(PluginLogger.LogLevel.INFO, "Distributed " + totalPointsDistributed + " points in all the rankings after banning " + banName);
            } catch (Exception e) {
                pluginLogger.log(PluginLogger.LogLevel.ERROR, "BetterEloCommand: handleBanCommand loop exception" + e.getMessage());
            }

            // Usuń rekordy gracza z wszystkich tabel
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterEloCommand: handleBanCommand: PKDB.deletePlayerRecords(banName)");

            PKDB.deletePlayerRecords(banName);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterEloCommand: handleBanCommand: setting 1000 points for " + banName + " in main");
            dataManager.setPoints(getOfflinePlayerUUID(banName), 1000d, "main");
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterEloCommand: handleBanCommand: setting 1000 points for " + banName + " in daily");
            dataManager.setPoints(getOfflinePlayerUUID(banName), 1000d, "daily");
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterEloCommand: handleBanCommand: setting 1000 points for " + banName + " in weekly");
            dataManager.setPoints(getOfflinePlayerUUID(banName), 1000d, "weekly");
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterEloCommand: handleBanCommand: setting 1000 points for " + banName + " in monthly");
            dataManager.setPoints(getOfflinePlayerUUID(banName), 1000d, "monthly");
            pluginLogger.log(PluginLogger.LogLevel.INFO, "Player " + banName + " banned, all rankings set to 1000points. Additional points redeemed to the victims");
            return true;
        } else {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterEloCommand: handleBanCommand: sender " + sender + " don't have permission to use /br tl");
            return false;
        }
    }

    public String getOfflinePlayerUUID(String playerName) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterEloCommand: getOfflinePlayerUUID called with parameter " + playerName);
        try {

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            String UUID = String.valueOf(offlinePlayer.getUniqueId());
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterEloCommand: getOfflinePlayerUUID UUID " + UUID);
            return UUID;
        } catch (Exception e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "BetterEloCommand: getOfflinePlayerUUID: " + e.getMessage());
            return null;
        }
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        StringBuilder formattedTime = new StringBuilder();

        if (days > 0) {
            formattedTime.append(days).append("d ");
        }
        if (hours % 24 > 0) {
            formattedTime.append(hours % 24).append("h ");
        }
        if (minutes % 60 > 0) {
            formattedTime.append(minutes % 60).append("m ");
        }
        if (seconds % 60 > 0 || formattedTime.length() == 0) {
            formattedTime.append(seconds % 60).append("s");
        }

        return formattedTime.toString().trim(); // Usunięcie ewentualnych spacji na końcu
    }

    private void HoloTop(Map playerPoints, Location location, HolographicDisplaysAPI api) {
        pluginLogger.log(PluginLogger.LogLevel.COMMAND, "BetterEloCommand.HoloTop called");
        if (eventHoloTask == null || eventHoloTask.isCancelled()) {
            eventHoloTask = new BukkitRunnable() {

                public void run() {
                    hologramEvent = api.createHologram(location);
                    hologramMain.getLines().insertText(0, ChatColor.GOLD + "" + ChatColor.BOLD + "EVENT RANKING");
                    hologramMain.getLines().insertText(1, ChatColor.GOLD + "" + ChatColor.BOLD + "TOP 1 - " + ChatColor.RED + "" + ChatColor.BOLD + dataManager.getPlayerAtPosition(1, playerPoints) + ChatColor.GOLD + "" + ChatColor.BOLD + "|| POINTS:" + ChatColor.BOLD + "" + ChatColor.RED + ((double) Math.round(dataManager.getPointsAtPosition(1, playerPoints) * 100) / 100));
                    hologramMain.getLines().insertText(2, ChatColor.GOLD + "" + ChatColor.BOLD + "TOP 2 - " + ChatColor.GREEN + "" + ChatColor.BOLD + dataManager.getPlayerAtPosition(2, playerPoints) + ChatColor.GOLD + "" + ChatColor.BOLD + "|| POINTS:" + ChatColor.BOLD + "" + ChatColor.GREEN + ((double) Math.round(dataManager.getPointsAtPosition(2, playerPoints) * 100) / 100));
                    hologramMain.getLines().insertText(3, ChatColor.GOLD + "" + ChatColor.BOLD + "TOP 3 - " + ChatColor.AQUA + "" + ChatColor.BOLD + dataManager.getPlayerAtPosition(3, playerPoints) + ChatColor.GOLD + "" + ChatColor.BOLD + "|| POINTS:" + ChatColor.BOLD + "" + ChatColor.AQUA + ((double) Math.round(dataManager.getPointsAtPosition(3, playerPoints) * 100) / 100));
                    hologramMain.getLines().insertText(4, ChatColor.WHITE + "" + ChatColor.BOLD + dataManager.getPlayerAtPosition(4, playerPoints) + ChatColor.GOLD + "" + ChatColor.BOLD + "|| POINTS:" + ChatColor.BOLD + "" + ChatColor.WHITE + ((double) Math.round(dataManager.getPointsAtPosition(4, playerPoints) * 100) / 100));
                    hologramMain.getLines().insertText(5, ChatColor.WHITE + "" + ChatColor.BOLD + dataManager.getPlayerAtPosition(5, playerPoints) + ChatColor.GOLD + "" + ChatColor.BOLD + "|| POINTS:" + ChatColor.BOLD + "" + ChatColor.WHITE + ((double) Math.round(dataManager.getPointsAtPosition(5, playerPoints) * 100) / 100));
                    hologramMain.getLines().insertText(6, ChatColor.WHITE + "" + ChatColor.BOLD + dataManager.getPlayerAtPosition(6, playerPoints) + ChatColor.GOLD + "" + ChatColor.BOLD + "|| POINTS:" + ChatColor.BOLD + "" + ChatColor.WHITE + ((double) Math.round(dataManager.getPointsAtPosition(6, playerPoints) * 100) / 100));
                    hologramMain.getLines().insertText(7, ChatColor.WHITE + "" + ChatColor.BOLD + dataManager.getPlayerAtPosition(7, playerPoints) + ChatColor.GOLD + "" + ChatColor.BOLD + "|| POINTS:" + ChatColor.BOLD + "" + ChatColor.WHITE + ((double) Math.round(dataManager.getPointsAtPosition(7, playerPoints) * 100) / 100));
                    hologramMain.getLines().insertText(8, ChatColor.WHITE + "" + ChatColor.BOLD + dataManager.getPlayerAtPosition(8, playerPoints) + ChatColor.GOLD + "" + ChatColor.BOLD + "|| POINTS:" + ChatColor.BOLD + "" + ChatColor.WHITE + ((double) Math.round(dataManager.getPointsAtPosition(8, playerPoints) * 100) / 100));
                    hologramMain.getLines().insertText(9, ChatColor.WHITE + "" + ChatColor.BOLD + dataManager.getPlayerAtPosition(9, playerPoints) + ChatColor.GOLD + "" + ChatColor.BOLD + "|| POINTS:" + ChatColor.BOLD + "" + ChatColor.WHITE + ((double) Math.round(dataManager.getPointsAtPosition(9, playerPoints) * 100) / 100));
                    hologramMain.getLines().insertText(10, ChatColor.WHITE + "" + ChatColor.BOLD + dataManager.getPlayerAtPosition(10, playerPoints) + ChatColor.GOLD + "" + ChatColor.BOLD + "|| POINTS:" + ChatColor.BOLD + "" + ChatColor.WHITE + ((double) Math.round(dataManager.getPointsAtPosition(10, playerPoints) * 100) / 100));
                    hologramEvent.delete();
                }
            }.runTaskTimer(plugin, 0L, 200L); // Pierwszy argument to odroczenie (po jakim czasie ma rozpocząć się pierwsze wykonanie zadania), a drugi to okres między kolejnymi wykonaniami w tickach (20 ticków = 1 sekunda)
        }
    }

    private void handleHelpCommand(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " List of player commands:");
        sender.sendMessage(ChatColor.AQUA + "/be " + ChatColor.GREEN + "- Returns your ranking info/Zwraca info o Twoim rankingu");
        sender.sendMessage(ChatColor.AQUA + "/be <player> " + ChatColor.GREEN + "- Returns ranking info about given player./Zwraca info rankingu danego gracza.");
        sender.sendMessage(ChatColor.AQUA + "/be info " + ChatColor.GREEN + "- Returns plugin info./Zwraca info o pluginie.");
        sender.sendMessage(ChatColor.AQUA + "/be top <n> " + ChatColor.GREEN + "- Returns ranking info about player at given position./Zwraca info o graczu na danym miejscu w rankingu");
        sender.sendMessage(ChatColor.AQUA + "/be top10 " + ChatColor.GREEN + "- Returns top10 players from ranking/Zwraca info o top10 graczy rankingu");
        sender.sendMessage(ChatColor.AQUA + "/be claim " + ChatColor.GREEN + "- Claim your rewards! Remember to empty your eq!!!/Odbierz swoje nagrody! Pamiętaj wyczyścić eq przed!!");
        sender.sendMessage(ChatColor.AQUA + "/be timeleft " + ChatColor.GREEN + "- Returns time left to giveaway/Zwraca pozostały czas do rozdania nagród");
        sender.sendMessage(ChatColor.AQUA + "/be daily " + ChatColor.GREEN + "- Returns top10 daily ranking/Zwraca info o top10 rankingu dziennego");
        sender.sendMessage(ChatColor.AQUA + "/be weekly " + ChatColor.GREEN + "- Returns top10 weekly ranking/Zwraca info o top10 rankingu tygodniowego");
        sender.sendMessage(ChatColor.AQUA + "/be monthly " + ChatColor.GREEN + "- Returns top10 monthly ranking/Zwraca info o top10 rankingu miesięcznego");
        sender.sendMessage(ChatColor.AQUA + "/be event " + ChatColor.GREEN + "- Returns event info/Zwraca info o evencie");
        sender.sendMessage(ChatColor.AQUA + "/be reroll " + ChatColor.GREEN + "- Open Re-Roll GUI for AvgDmg bonus items");
        if (sender.isOp()) {
            sender.sendMessage(ChatColor.AQUA + "/be setrewards " + ChatColor.GREEN + "- Opens a GUI for changing the rewards");
            sender.sendMessage(ChatColor.AQUA + "/be reload " + ChatColor.GREEN + "- Reloads the config file ");
            sender.sendMessage(ChatColor.AQUA + "/be ban <player> " + ChatColor.GREEN + "- resetting the player's rankings to 1000 and redeeming remaining poits to victims.");
            sender.sendMessage(ChatColor.AQUA + "/be add <player> <points> <rankingtype> " + ChatColor.GREEN + "- adding points to given player in specific ranking (main,daily,weekly,monthly)");
            sender.sendMessage(ChatColor.AQUA + "/be sub <player> <points> <rankingtype> " + ChatColor.GREEN + "- subtracting points from given player in specific ranking (main,daily,weekly,monthly)");
            sender.sendMessage(ChatColor.AQUA + "/be startevent <duration> <timeUnit> " + ChatColor.GREEN + "- setting up event duration and time unit <h/m> ");
            sender.sendMessage(ChatColor.AQUA + "/be stopevent " + ChatColor.GREEN + "- Stops current event (if active).");
            sender.sendMessage(ChatColor.AQUA + "/be antyweb <radius> " + ChatColor.GREEN + "- creates antyweb effect with given radius");
            sender.sendMessage(ChatColor.AQUA + "/be addspawner <spawnerName> <mobName> <cooldown(s)> <mobCountPerSpawn> <maxMobs>" + ChatColor.GREEN + "- creates custom mob spawner");
            sender.sendMessage(ChatColor.AQUA + "/be droptable <name> - opens a GUI to create new drop table");
            sender.sendMessage(ChatColor.AQUA + "/be spawnmob <mobname> <amount> - spawn given custom mob");
            sender.sendMessage(ChatColor.AQUA + "/be enchantitem - gives you 1x Enchant Item");
            sender.sendMessage(ChatColor.AQUA + "/be forcespawn <spawnerName> - forces a respawn of a given spawner");
            sender.sendMessage(ChatColor.AQUA + "/be setattribute mobdefense/mobdamage/avaragedamage x - adds custom attribute with x value to the item in hand");
        }
    }

    private void handleTimeLeft(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " This command can only be used by online players.");
            return;
        }

        Player player = (Player) sender;

        long dailyTimeLeft = betterElo.getRemainingTimeForRewards("daily");
        long weeklyTimeLeft = betterElo.getRemainingTimeForRewards("weekly");
        long monthlyTimeLeft = betterElo.getRemainingTimeForRewards("monthly");


        player.sendMessage(ChatColor.GREEN +lang.remainingTimeMessage+" | " +lang.dailyTranslation + formatTime(dailyTimeLeft)+" | "+lang.weeklyTranslation + formatTime(weeklyTimeLeft)+" | "+lang.monthlyTranslation + formatTime(monthlyTimeLeft));

        /*
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Remaining time for daily rewards: " + ChatColor.GREEN + formatTime(dailyTimeLeft));
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Remaining time for weekly rewards: " + ChatColor.GREEN + formatTime(weeklyTimeLeft));
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Remaining time for monthly rewards: " + ChatColor.GREEN + formatTime(monthlyTimeLeft));

         */
        if (betterElo.isEventEnabled) {
            long eventTimeLeft = betterElo.getRemainingTimeForRewards("event");
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Remaining time for event rewards: " + ChatColor.GREEN + formatTime(eventTimeLeft));

        }
    }

    private void handleSetRewards(CommandSender sender) {
        pluginLogger.log(PluginLogger.LogLevel.INFO, "BetterEloCommand: Player " + sender.getName() + " issued command /be setrewards");
        if (!sender.hasPermission("betterelo.setrewards")) {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "BetterEloCommand: Player " + sender.getName() + " was denied access to command /be setrewards");
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " You don't have permission to use that command!");
            return;
        }
        pluginLogger.log(PluginLogger.LogLevel.INFO, "BetterEloCommand: Player " + sender.getName() + " was granted access to command /be setrewards");
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " This command can only be used by online players.");
            return;
        }

        Player player = (Player) sender;
        guiManager.openMainGui(player); // Otwieramy główne menu GUI dla gracza
    }

    private void handleCreateDropTable(CommandSender sender, String dropTableName) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterEloCommand: Player " + sender.getName() + " issued command /be droptable " + dropTableName);
        if (!sender.hasPermission("betterelo.droptable") || !sender.isOp()) {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "BetterEloCommand: Player " + sender.getName() + " was denied access to command /be droptable");
            noPermission(sender);
            return;
        }
        pluginLogger.log(PluginLogger.LogLevel.INFO, "BetterEloCommand: Player " + sender.getName() + " was granted access to command /be droptable");
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " This command can only be used by online players.");
            return;
        }

        Player player = (Player) sender;
        guiManager.openDroptableGui(player, dropTableName); // Otwieramy główne menu GUI dla gracza
    }

    public void handleRerollCommand(CommandSender sender) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterEloCommand.handleRerollCommand called, sender: " + sender);
        if (!sender.hasPermission("betterelo.rerollgui") || !sender.isOp()) {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "BetterEloCommand: Player " + sender.getName() + " was denied access to command /be reroll which is fucked and should be fixed");
            noPermission(sender);
            return;
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            guiManager.openReRollGui(player);
        }
    }

    private void createEncahntItem(CommandSender sender,String transactionID) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterEloCommand.createEncahntItem called, sender: " + sender, transactionID);
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " This command can only be used by online players.");
            return;
        }
        Player player = (Player) sender;
        ItemStack enchantItem = null;
        try {
            enchantItem = Utils.getEnchantItem(transactionID);
            player.getInventory().addItem(enchantItem);
        } catch (Exception e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "BetterEloCommand.createEncahntItem exception " + e.getMessage(),transactionID,player.getName(),player.getUniqueId().toString());
        }

        player.getInventory().addItem(enchantItem);
        pluginLogger.log(PluginLogger.LogLevel.INFO, "BetterEloCommand.createEncahntItem: EnchantItem added to " + player.getName(),transactionID,player.getName(),player.getUniqueId().toString());
    }

    public void addAntywebLore(Player player, ItemStack itemStack, int radius) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return; // Przerwij, jeśli nie można pobrać metadanych przedmiotu
        }
        String antywebLore = ChatColor.GOLD + "" + ChatColor.BOLD + "Antyweb " + radius;
        List<String> lore = itemMeta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(antywebLore);
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Added Antyweb lore with radius " + radius);

    }

    public void addZephyrLore(Player player, ItemStack itemStack, int power) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return; // Przerwij, jeśli nie można pobrać metadanych przedmiotu
        }
        String zephyrLore = ChatColor.GOLD + "" + ChatColor.BOLD + "Zephyr " + power;
        List<String> lore = itemMeta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(zephyrLore);
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Added Zephyr lore with power " + power);

    }

    public void addFlamethrowerLore(Player player, ItemStack itemStack, int radius, int distance) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return; // Przerwij, jeśli nie można pobrać metadanych przedmiotu
        }
        String flamethrowerLore = ChatColor.GOLD + "" + ChatColor.BOLD + "Flamethrower " + radius + "/" + distance;
        List<String> lore = itemMeta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(flamethrowerLore);
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Added Antyweb lore with radius " + radius);

    }

    public void addElytraLore(Player player, ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) {
            return; // Przerwij, jeśli nie można pobrać metadanych przedmiotu
        }
        String antywebLore = ChatColor.GOLD + "" + ChatColor.BOLD + "Elytra effect";
        List<String> lore = itemMeta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(antywebLore);
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Added Elytra effect");

    }


    private static void createInfiniteFireworkItem(Player player, int power) {
        //ItemStack firework = createInfiniteFireworkItem();
        ItemStack firework = new ItemStack(Material.FIREWORK_ROCKET, 1);
        FireworkMeta meta = (FireworkMeta) firework.getItemMeta();
        meta.setDisplayName("Infinite Firework");
        meta.setLore(java.util.Arrays.asList("§6§lInfinite usage")); // Gold bold "Infinite usage";
        meta.setPower(power);
        firework.setItemMeta(meta);
        player.getInventory().addItem(firework);
    }

    public void handleAddSpawnerCommand(CommandSender sender, String spawnerName, String mobName, int spawnerCooldown, int mobCount, int maxMobs) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.isOp()) {
                Location targetLocation = player.getTargetBlock(null, 100).getLocation();
                customMobsFileManager.saveSpawner(targetLocation, spawnerName, mobName, spawnerCooldown, mobCount, maxMobs);
            } else {
                noPermission(sender);
            }
        } else {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "BetterEloCommand:handleAddSpawnerCommand this is only-player command!");
        }
    }

    private void noPermission(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + lang.noPermissionMessage);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterEloCommand.onTabComplete called");
        Player player = (Player) sender;
        List<String> tabCompleteList = new ArrayList<>();
        if (args.length == 1) {
            tabCompleteList.add("top");
            tabCompleteList.add("top10");
            tabCompleteList.add("claim");
            tabCompleteList.add("timeleft");
            tabCompleteList.add("daily");
            tabCompleteList.add("weekly");
            tabCompleteList.add("monthly");
            tabCompleteList.add("event");
            tabCompleteList.add("reroll");
            if (sender.isOp()) {
                tabCompleteList.add("setrewards");
                tabCompleteList.add("reload");
                tabCompleteList.add("ban");
                tabCompleteList.add("add");
                tabCompleteList.add("sub");
                tabCompleteList.add("startevent");
                tabCompleteList.add("stopevent");
                tabCompleteList.add("antyweb");
                tabCompleteList.add("addspawner");
                tabCompleteList.add("droptable");
                tabCompleteList.add("spawnmob");
                tabCompleteList.add("enchantitem");
                tabCompleteList.add("forcespawn");
                tabCompleteList.add("setattribute");
                tabCompleteList.add("language");
            }
            if (player.hasPermission("betterelo.autobank")) {
                tabCompleteList.add("autobank");
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("top")) {
                tabCompleteList.add("1");
                tabCompleteList.add("2");
                tabCompleteList.add("3");
                tabCompleteList.add("4");
                tabCompleteList.add("5");
                tabCompleteList.add("6");
                tabCompleteList.add("7");
                tabCompleteList.add("8");
                tabCompleteList.add("9");
                tabCompleteList.add("10");
            } else if ((args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("sub")) && sender.isOp()) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    tabCompleteList.add(onlinePlayer.getName());
                }
            } else if (args[0].equalsIgnoreCase("ban") && sender.isOp()) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    tabCompleteList.add(onlinePlayer.getName());
                }
            } else if (args[0].equalsIgnoreCase("startevent") && sender.isOp()) {
                tabCompleteList.add("1");
                tabCompleteList.add("2");
                tabCompleteList.add("3");
                tabCompleteList.add("4");

            }
        }
        return tabCompleteList;
    }
}

