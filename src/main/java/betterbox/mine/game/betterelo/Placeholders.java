package betterbox.mine.game.betterelo;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Placeholders extends PlaceholderExpansion {

    private final DataManager dataManager;
    private final BetterElo betterElo;

    public Placeholders(DataManager dataManager, BetterElo betterElo) {
        this.dataManager = dataManager;
        this.betterElo = betterElo;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "be";
    }

    @Override
    public @NotNull String getAuthor() {
        return betterElo.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return betterElo.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    public String formatTime(long milliseconds) {
        if (milliseconds < 0) {
            return "Time is up!";
        }

        long totalSeconds = milliseconds / 1000;
        long days = totalSeconds / (24 * 3600);
        long hours = (totalSeconds % (24 * 3600)) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder timeString = new StringBuilder();

        if (days > 0) {
            timeString.append(days).append(" ").append("days").append(" | ");
        }
        if (hours > 0) {
            timeString.append(hours).append(" ").append("hours").append(" | ");
        }
        if (minutes > 0) {
            timeString.append(minutes).append(" ").append("minutes").append(" | ");
        }
        if (seconds > 0 || timeString.length() == 0) {
            timeString.append(seconds).append(" ").append("seconds");
        }

        return timeString.toString().trim();
    }

    private String processRankingPlaceholder(String identifier, String prefix, Map<String, Double> pointsMap) {
        if (identifier.startsWith(prefix + "points_top")) {
            int position = Integer.parseInt(identifier.replace(prefix + "points_top", ""));
            double points = dataManager.getPointsAtPosition(position, pointsMap);
            return String.valueOf(points);
        } else if (identifier.startsWith(prefix + "player_top")) {
            int position = Integer.parseInt(identifier.replace(prefix + "player_top", ""));
            String playerName = dataManager.getPlayerAtPosition(position, pointsMap);
            return playerName != null ? playerName : "No Player";
        }
        return null;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player != null) {
            switch (identifier) {
                case "player":
                    return String.valueOf(dataManager.getPoints(player.getUniqueId().toString(), "main"));
                case "rank":
                    return String.valueOf(dataManager.getPlayerRank(player.getUniqueId().toString()));
                case "daily_tl":
                    return formatTime(betterElo.getRemainingTimeForRewards("daily"));
                case "weekly_tl":
                    return formatTime(betterElo.getRemainingTimeForRewards("weekly"));
                case "monthly_tl":
                    return formatTime(betterElo.getRemainingTimeForRewards("monthly"));
                default:
                    String result;
                    if ((result = processRankingPlaceholder(identifier, "", dataManager.playerPoints)) != null) {
                        return result;
                    } else if ((result = processRankingPlaceholder(identifier, "daily_", dataManager.dailyPlayerPoints)) != null) {
                        return result;
                    } else if ((result = processRankingPlaceholder(identifier, "weekly_", dataManager.weeklyPlayerPoints)) != null) {
                        return result;
                    } else if ((result = processRankingPlaceholder(identifier, "monthly_", dataManager.monthlyPayerPoints)) != null) {
                        return result;
                    }
            }
        } else {
            OfflinePlayer offlinePlayer = dataManager.getOfflinePlayer(identifier);

            if (offlinePlayer != null) {
                switch (identifier) {
                    case "killed":
                        return String.valueOf(dataManager.getPoints(offlinePlayer.getUniqueId().toString(), "main"));
                    case "killer":
                        return String.valueOf(dataManager.getPoints(offlinePlayer.getUniqueId().toString(), "main"));
                }
            }
        }

        return null;
    }
}
