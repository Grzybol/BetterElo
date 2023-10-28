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
import java.util.List;


public class BetterEloCommand implements CommandExecutor {

    private final DataManager dataManager;
    private final JavaPlugin plugin;
    private final PluginLogger pluginLogger;
    private final GuiManager guiManager; // Dodajemy referencję do GuiManager
    private BetterElo betterElo;
    public String state;


    public BetterEloCommand(JavaPlugin plugin, DataManager dataManager, GuiManager guiManager, PluginLogger pluginLogger, BetterElo betterElo) {
        this.dataManager = dataManager;
        this.plugin = plugin;

        this.guiManager = guiManager; // Inicjalizujemy referencję do GuiManager
        this.pluginLogger = pluginLogger;
        this.betterElo = betterElo; // Inicjalizujemy referencję do BetterElo


    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            // /be - Information about the player's rank and points
            if (sender instanceof Player) {
                Player player = (Player) sender;
                int rank = dataManager.getPlayerRank(player.getUniqueId().toString());
                double points = dataManager.getPoints(player.getUniqueId().toString(),"main");
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.AQUA +"Your rank: " +ChatColor.GREEN+ rank);
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.AQUA +"Your points: " +ChatColor.GREEN+ points);
            } else {
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.DARK_RED +" This command can only be used by online players.");
            }
        } else if (args[0].equalsIgnoreCase("info")) {
            // /be info - Plugin information
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.AQUA +" Better Elo system for BetterBox.");
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.AQUA +" Author: grzybol");
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.AQUA +" Version: 2.0.7");
        }else if (args[0].equalsIgnoreCase("help")) {
            // /be info - Plugin information
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.AQUA +" List of player commands:");
            sender.sendMessage(ChatColor.AQUA + "/be "+ChatColor.GREEN+"- Returns your ranking info/Zwraca info o Twoim rankingu");
            sender.sendMessage(ChatColor.AQUA + "/be <player> "+ChatColor.GREEN+"- Returns ranking info about given player./Zwraca info rankingu danego gracza.");
            sender.sendMessage(ChatColor.AQUA + "/be info "+ChatColor.GREEN+"- Returns plugin info./Zwraca info o pluginie.");
            sender.sendMessage(ChatColor.AQUA + "/be top <n> "+ChatColor.GREEN+"- Returns ranking info abot player at given postion./Zwraca info o graczu na danym miejscu w rankingu");
            sender.sendMessage(ChatColor.AQUA + "/be top10 "+ChatColor.GREEN+"- Returns top10 players from ranking/Zwraca info o top10 graczy rankingu");
            sender.sendMessage(ChatColor.AQUA + "/be claim "+ChatColor.GREEN+"- Claim your rewards! Remeber to empty your eq!!!/Odbierz swoje nagrody! Pameitaj wyczyścić eq przed!!");
            sender.sendMessage(ChatColor.AQUA + "/be timeleft "+ChatColor.GREEN+"- Returns time left to giveaway/Zwraca pozostały czas do rozdania nagród");
            sender.sendMessage(ChatColor.AQUA + "/be daily "+ChatColor.GREEN+"- Returns top10 daily ranking/Zwraca info o top10 rankingu dziennego");
            sender.sendMessage(ChatColor.AQUA + "/be weekly "+ChatColor.GREEN+"- Returns top10 weekly ranking/Zwraca info o top10 rankingu tygodniowego");
            sender.sendMessage(ChatColor.AQUA + "/be monthly "+ChatColor.GREEN+"- Returns top10 monthly ranking/Zwraca info o top10 rankingu miesięcznego");
        } else if (args.length == 1 && args[0].equalsIgnoreCase("top10")) {
            // /be top10 - Displays the top 10 players and their points
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.AQUA +" Top 10 players in the ranking:");
            for (int i = 1; i <= 10; i++) {
                String playerName = dataManager.getPlayerAtPosition(i,dataManager.playerPoints);
                double points = dataManager.getPointsAtPosition(i,dataManager.playerPoints);
                if (playerName != null) {
                    sender.sendMessage(ChatColor.AQUA.toString() + i + ". " + ChatColor.GREEN +playerName + ChatColor.AQUA+" - Points: " + ChatColor.GREEN +points);
                }

            }
        }else if (args.length == 1 && args[0].equalsIgnoreCase("daily")) {
            // /be top10 - Displays the top 10 players and their points
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.AQUA +" Top 10 players in the daily ranking:");
            for (int i = 1; i <= 10; i++) {
                String playerName = dataManager.getPlayerAtPosition(i,dataManager.dailyPlayerPoints);
                double points = dataManager.getPointsAtPosition(i,dataManager.dailyPlayerPoints);
                if (playerName != null) {
                    sender.sendMessage(ChatColor.AQUA.toString() + i + ". " + ChatColor.GREEN +playerName + ChatColor.AQUA+" - Points: " + ChatColor.GREEN +points);
                }
            }
        }else if (args.length == 1 && args[0].equalsIgnoreCase("weekly")) {
            // /be top10 - Displays the top 10 players and their points
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.AQUA +" Top 10 players in the weekly ranking:");
            for (int i = 1; i <= 10; i++) {
                String playerName = dataManager.getPlayerAtPosition(i,dataManager.weeklyPlayerPoints);
                double points = dataManager.getPointsAtPosition(i,dataManager.weeklyPlayerPoints);
                if (playerName != null) {
                    sender.sendMessage(ChatColor.AQUA.toString() + i + ". " + ChatColor.GREEN +playerName + ChatColor.AQUA+" - Points: " + ChatColor.GREEN +points);
                }
            }
        }else if (args.length == 1 && args[0].equalsIgnoreCase("monthly")) {
            // /be top10 - Displays the top 10 players and their points
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.AQUA +" Top 10 players in the monthly ranking:");
            for (int i = 1; i <= 10; i++) {
                String playerName = dataManager.getPlayerAtPosition(i,dataManager.monthlyPayerPoints);
                double points = dataManager.getPointsAtPosition(i,dataManager.monthlyPayerPoints);
                if (playerName != null) {
                    sender.sendMessage(ChatColor.AQUA.toString() + i + ". " + ChatColor.GREEN +playerName + ChatColor.AQUA+" - Points: " + ChatColor.GREEN +points);
                }
            }
        }else if (args.length == 1 && args[0].equalsIgnoreCase("claim")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.AQUA +" This command can only be used by online players.");
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
                    sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.AQUA +" Rewards calimed!");
                    pluginLogger.log(PluginLogger.LogLevel.INFO,"BetterEloCommand: claim: player: "+player.getName()+" reward: "+item);
                }
                rewardsConfig.set(playerName, null); // Usuń przyznane nagrody z pliku
                pluginLogger.log(PluginLogger.LogLevel.INFO,"BetterEloCommand: claim: player: "+player.getName()+" rewards claimed.");
                try {
                    rewardsConfig.save(rewardsFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.DARK_RED +" No rewards assigned.");
            pluginLogger.log(PluginLogger.LogLevel.INFO,"BetterEloCommand: claim: player: "+player.getName()+" No rewards assigned.");
        }else if (args.length == 1 && args[0].equalsIgnoreCase("timeleft")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.AQUA +" This command can only be used by online players.");
                return true;
            }

            Player player = (Player) sender;

                long dailyTimeLeft = betterElo.getRemainingTimeForRewards("daily");
                long weeklyTimeLeft = betterElo.getRemainingTimeForRewards("weekly");
                long monthlyTimeLeft = betterElo.getRemainingTimeForRewards("monthly");

                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.AQUA +" Remaining time for daily rewards: "+ChatColor.GREEN+ formatTime(dailyTimeLeft));
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.AQUA +" Remaining time for weekly rewards: "+ChatColor.GREEN+ formatTime(weeklyTimeLeft));
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.AQUA +" Remaining time for monthly rewards: " +ChatColor.GREEN+  formatTime(monthlyTimeLeft));

        }else if (args.length == 1 && args[0].equalsIgnoreCase("setrewards")) {
            pluginLogger.log(PluginLogger.LogLevel.INFO,"BetterEloCommand: Player "+sender.getName()+" issued command /be setrewards");
            if (!sender.hasPermission("betterelo.setrewards")) {
                pluginLogger.log(PluginLogger.LogLevel.WARNING,"BetterEloCommand: Player "+sender.getName()+" was denied access to command /be setrewards");
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.DARK_RED +" You don't have permission to use that command!");
                return true;
            }
            pluginLogger.log(PluginLogger.LogLevel.INFO,"BetterEloCommand: Player "+sender.getName()+" was granted access to command /be setrewards");
            // /be setrewards - Otwiera interfejs GUI do ustawiania nagród
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.DARK_RED +" This command can only be used by online players.");
                return true;
            }

            Player player = (Player) sender;
            guiManager.openMainGui(player); // Otwieramy główne menu GUI dla gracza
        }else if (args.length == 1) {
            // /be <player_name> - Information about a specific player's rank and points
            String playerName = args[0];

            // Pobierz obiekt OfflinePlayer dla danego gracza
            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(playerName);

            // Sprawdź, czy gracz istnieje w bazie danych pluginu
            String playerUUID = targetPlayer.getUniqueId().toString();
            if (!dataManager.playerExists(playerUUID,"main")) {
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.DARK_RED +" Player with the name " +ChatColor.GREEN+ playerName +ChatColor.AQUA + " has never played on this server.");
                return true;
            }

            // Sprawdź, czy gracz o podanej nazwie istnieje w rankingu
            int rank = dataManager.getPlayerRank(playerUUID);
            if (rank == -1) {
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.DARK_RED +" Player with the name " +ChatColor.GREEN+ playerName +ChatColor.AQUA + " was not found in the ranking.");
                return true;
            }

            double points = dataManager.getPoints(playerUUID,"main");
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.AQUA +" Player " +ChatColor.GREEN+ playerName +ChatColor.AQUA + " is ranked " +ChatColor.GREEN+ rank +ChatColor.AQUA + ".");
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.AQUA +" Player's points: " +ChatColor.GREEN+ points);
        }
        else if (args.length == 2 && args[0].equalsIgnoreCase("top")) {
            // /be top <n> - Displays the nickname and points of the player in position n in the ranking
            try {
                int position = Integer.parseInt(args[1]);
                String playerName = dataManager.getPlayerAtPosition(position,dataManager.playerPoints);
                double points = dataManager.getPointsAtPosition(position,dataManager.playerPoints);
                if (playerName != null) {
                    sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.AQUA +" Player in position " +ChatColor.GREEN+ position + ": " +ChatColor.GREEN+ playerName);
                    sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.AQUA +" Player's points: " +ChatColor.GREEN+ points);
                } else {
                    sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.DARK_RED +" No player in position " +ChatColor.GREEN+ position +ChatColor.DARK_RED+ " in the ranking.");
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterElo]"+ChatColor.DARK_RED +" Please enter a valid ranking position number.");
            }
            // ... [reszta kodu]

        }
        return true;
    }
    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        return String.format("%d days, %d hours, %d minutes, %d seconds", days, hours % 24, minutes % 60, seconds % 60);
    }
}
