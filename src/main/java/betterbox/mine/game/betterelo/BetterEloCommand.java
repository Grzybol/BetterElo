package betterbox.mine.game.betterelo;

import org.bukkit.OfflinePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class BetterEloCommand implements CommandExecutor {

    private final DataManager dataManager;
    private final JavaPlugin plugin;
    private final PluginLogger pluginLogger;
    private final GuiManager guiManager; // Dodajemy referencję do GuiManager
    private final BetterElo betterElo;
    private final ExtendedConfigManager configManager;
    private final Event event;
    private final PlayerKillDatabase PKDB;

    public BetterEloCommand(JavaPlugin plugin, DataManager dataManager, GuiManager guiManager, PluginLogger pluginLogger, BetterElo betterElo, ExtendedConfigManager configManager,Event event, PlayerKillDatabase PKDB) {
        this.dataManager = dataManager;
        this.plugin = plugin;

        this.guiManager = guiManager; // Inicjalizujemy referencję do GuiManager
        this.pluginLogger = pluginLogger;
        this.betterElo = betterElo; // Inicjalizujemy referencję do BetterElo
        this.configManager = configManager;
        this.event = event;
        this.PKDB = PKDB;


    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (args.length) {
            case 0:
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    int rank = dataManager.getPlayerRank(player.getUniqueId().toString());
                    double points = dataManager.getPoints(player.getUniqueId().toString(), "main");
                    sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + "Your rank: " + ChatColor.GREEN + rank);
                    sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + "Your points: " + ChatColor.GREEN + points);
                } else {
                    sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " This command can only be used by online players.");
                }
                break;
            case 1:
                switch (args[0].toLowerCase()) {
                    case "info":
                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Better Elo system for BetterBox.");
                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Author: "+plugin.getDescription().getAuthors());
                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Version: "+plugin.getDescription().getVersion());
                        break;
                    case "help":
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
                        break;
                    case "top10":
                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Top 10 players in the ranking:");
                        for (int i = 1; i <= 10; i++) {
                            String playerName = dataManager.getPlayerAtPosition(i, dataManager.playerPoints);
                            double points = dataManager.getPointsAtPosition(i, dataManager.playerPoints);
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
                                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Rewards claimed!");
                                pluginLogger.log(PluginLogger.LogLevel.INFO, "BetterEloCommand: claim: player: " + player.getName() + " reward: " + item);
                            }
                            rewardsConfig.set(playerName, null); // Usuń przyznane nagrody z pliku
                            pluginLogger.log(PluginLogger.LogLevel.INFO, "BetterEloCommand: claim: player: " + player.getName() + " rewards claimed.");
                            try {
                                rewardsConfig.save(rewardsFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                                pluginLogger.log(PluginLogger.LogLevel.ERROR, "BetterEloCommand: claim: Error while saving rewards configuration: " + e);
                            }
                        }
                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " No rewards assigned.");
                        pluginLogger.log(PluginLogger.LogLevel.INFO, "BetterEloCommand: claim: player: " + player.getName() + " No rewards assigned.");
                        break;
                    case "timeleft":
                        if (!(sender instanceof Player)) {
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " This command can only be used by online players.");
                            return true;
                        }

                        player = (Player) sender;

                        long dailyTimeLeft = betterElo.getRemainingTimeForRewards("daily");
                        long weeklyTimeLeft = betterElo.getRemainingTimeForRewards("weekly");
                        long monthlyTimeLeft = betterElo.getRemainingTimeForRewards("monthly");

                        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Remaining time for daily rewards: " + ChatColor.GREEN + formatTime(dailyTimeLeft));
                        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Remaining time for weekly rewards: " + ChatColor.GREEN + formatTime(weeklyTimeLeft));
                        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Remaining time for monthly rewards: " + ChatColor.GREEN + formatTime(monthlyTimeLeft));
                        break;
                    case "setrewards":
                        pluginLogger.log(PluginLogger.LogLevel.INFO, "BetterEloCommand: Player " + sender.getName() + " issued command /be setrewards");
                        if (!sender.hasPermission("betterelo.setrewards")) {
                            pluginLogger.log(PluginLogger.LogLevel.WARNING, "BetterEloCommand: Player " + sender.getName() + " was denied access to command /be setrewards");
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " You don't have permission to use that command!");
                            return true;
                        }
                        pluginLogger.log(PluginLogger.LogLevel.INFO, "BetterEloCommand: Player " + sender.getName() + " was granted access to command /be setrewards");
                        if (!(sender instanceof Player)) {
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " This command can only be used by online players.");
                            return true;
                        }

                        player = (Player) sender;
                        guiManager.openMainGui(player); // Otwieramy główne menu GUI dla gracza
                        break;
                    case "reload":
                        return handleReloadCommand(sender);

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
                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Player " + ChatColor.GREEN + playerName + ChatColor.AQUA + " is ranked " + ChatColor.GREEN + rank + ChatColor.AQUA + ".");
                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Player's points: " + ChatColor.GREEN + points);
                        break;
                }
                break;
            case 2:
                if (args[0].equalsIgnoreCase("top")) {
                    // /be top <n> - Displays the nickname and points of the player in position n in the ranking
                    try {
                        int position = Integer.parseInt(args[1]);
                        String playerName = dataManager.getPlayerAtPosition(position, dataManager.playerPoints);
                        double points = dataManager.getPointsAtPosition(position, dataManager.playerPoints);
                        if (playerName != null) {
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Player in position " + ChatColor.GREEN + position + ": " + ChatColor.GREEN + playerName);
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Player's points: " + ChatColor.GREEN + points);
                        } else {
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " No player in position " + ChatColor.GREEN + position + ChatColor.DARK_RED + " in the ranking.");
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.DARK_RED + " Please enter a valid ranking position number.");
                    }
                }
                if(args[0].equalsIgnoreCase("ban")){

                    handleBanCommand(sender,args[1]);
                }
                break;
        }
        return true;
    }
    private boolean handleReloadCommand(CommandSender sender){
        if(sender.hasPermission("betterelo.reload")){
            configManager.ReloadConfig();
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " BetterRanks config reloaded!");
            return true;
        }else {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterEloCommand: handleReloadCommand: sender " + sender + " dont have permission to use /br tl");
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo] " + ChatColor.DARK_RED +"You don't have permission to use this command!");
            return false;
        }
    }
    private boolean handleBanCommand(CommandSender sender, String banName) {
        if (sender.hasPermission("betterelo.ban")) {
            if (!(sender instanceof Player)) {

                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo] " + ChatColor.DARK_RED +"You don't have permission to use this command!");
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterEloCommand: handleBanCommand: sender " + sender + " don't have permission to use /br tl");
                return false;
            }

            Player player = (Player) sender;

            // Pobierz listę interakcji dla gracza, którego chcesz zbanować
            HashMap<String, HashMap<String, Double>> interactionsMap = PKDB.getPlayerInteractions(banName);

            if (interactionsMap.isEmpty()) {
                sender.sendMessage("Brak interakcji do zbanowania dla gracza " + banName);
                return false;
            }
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]" + ChatColor.AQUA + " Banning player " + banName);
            for (Map.Entry<String, HashMap<String, Double>> entry : interactionsMap.entrySet()) {
                String rankingType = entry.getKey();
                HashMap<String, Double> interactions = entry.getValue();

                for (Map.Entry<String, Double> interactionEntry : interactions.entrySet()) {
                    String otherPlayer = interactionEntry.getKey();
                    double totalPoints = interactionEntry.getValue();

                    if (totalPoints > 0) {
                        // Gracz zasługuje na karę (odejmowanie punktów)
                        event.subtractPoints(otherPlayer, totalPoints, rankingType);
                        sender.sendMessage("Gracz " + otherPlayer + " stracił " + totalPoints + " punktów w trybie " + rankingType);
                    } else if (totalPoints < 0) {
                        // Gracz nie zasługuje na karę (dodawanie punktów)
                        event.addPoints(otherPlayer, Math.abs(totalPoints), rankingType);
                        sender.sendMessage("Gracz " + otherPlayer + " otrzymał " + Math.abs(totalPoints) + " punktów nagrody w trybie " + rankingType);
                    } else {
                        sender.sendMessage("Gracz " + otherPlayer + " nie ma żadnych punktów do zmiany w trybie " + rankingType);
                    }
                }
            }

            // Usuń rekordy gracza z wszystkich tabel
            PKDB.deletePlayerRecords(banName);
            return true;
        } else {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterEloCommand: handleBanCommand: sender " + sender + " don't have permission to use /br tl");
            return false;
        }
    }





    public String getOfflinePlayerUUID(String playerName) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        if (offlinePlayer.hasPlayedBefore()) {
            String UUID = String.valueOf(offlinePlayer.getUniqueId());
            return UUID;
        } else {
            // Gracz o podanej nazwie jeszcze nie grał
            return null;
        }
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        return String.format("%d days, %d hours, %d minutes, %d seconds", days, hours % 24, minutes % 60, seconds % 60);
    }
}
