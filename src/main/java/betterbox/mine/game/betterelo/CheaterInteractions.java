package betterbox.mine.game.betterelo;

public class CheaterInteractions {
    private String rankingType;
    private String playerName;
    private String victimName;
    private double totalPoints;

    public CheaterInteractions(String rankingType, String playerName, String victimName, double totalPoints) {
        this.rankingType = rankingType;
        this.playerName = playerName;
        this.victimName = victimName;
        this.totalPoints = totalPoints;
    }

    public String getRankingType() {
        return rankingType;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getVictimName() {
        return victimName;
    }

    public double getTotalPoints() {
        return totalPoints;
    }
}
