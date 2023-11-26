package betterbox.mine.game.betterelo;

import java.sql.*;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerKillDatabase {
    private static final String DATABASE_URL = "jdbc:sqlite:plugins/BetterElo/player_kill_database.db";
    private PluginLogger pluginLogger;

    public PlayerKillDatabase(PluginLogger pluginLogger) {
        this.pluginLogger = pluginLogger;
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"PlayerKillDatabase called");
        createDatabaseIfNeeded();
    }

    private void createDatabaseIfNeeded() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"PlayerKillDatabase: createDatabaseIfNeeded called");
        File databaseFile = new File("plugins/BetterElo/player_kill_database.db");
        if (!databaseFile.exists()) {
            createNewDatabase();
        }
    }
    public void saveKillData(String rankingType, String victimName, String killerName, double pointsEarned) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "PlayerKillDatabase: saveKillData called with parameters: " + rankingType+" "+victimName+" "+killerName);
        try {
            Connection connection = DriverManager.getConnection(DATABASE_URL);

            // Wstawienie danych do odpowiedniej tabeli
            String insertSQL = "INSERT INTO " + rankingType + " (killername, victimname, pointearned) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
            preparedStatement.setString(1, killerName);
            preparedStatement.setString(2, victimName);
            preparedStatement.setDouble(3, pointsEarned);

            preparedStatement.executeUpdate();

            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void deletePlayerRecords(String playerName) {
        try {
            Connection connection = DriverManager.getConnection(DATABASE_URL);

            // Usuwanie rekordów z tabeli "daily"
            deleteRecordsFromTable(connection, "daily", playerName);

            // Usuwanie rekordów z tabeli "weekly"
            deleteRecordsFromTable(connection, "weekly", playerName);

            // Usuwanie rekordów z tabeli "monthly"
            deleteRecordsFromTable(connection, "monthly", playerName);

            // Usuwanie rekordów z tabeli "main"
            deleteRecordsFromTable(connection, "main", playerName);

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteRecordsFromTable(Connection connection, String tableName, String playerName) throws SQLException {
        String deleteSQL = "DELETE FROM " + tableName + " WHERE killername = ? OR victimname = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL);
        preparedStatement.setString(1, playerName);
        preparedStatement.setString(2, playerName);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
    public Map<String, List<PlayerInteraction>> getPlayerInteractions(String playerName) {
        Map<String, List<PlayerInteraction>> interactionsMap = new HashMap<>();

        // Tabele do przeszukania
        String[] tables = {"daily", "weekly", "monthly", "main"};

        for (String table : tables) {
            List<PlayerInteraction> playerInteractions = new ArrayList<>();

            try {
                Connection connection = DriverManager.getConnection(DATABASE_URL);

                // Zapytanie SQL do zliczania sumy zabójstw gracza X na graczu Y
                String queryKillsXonY = "SELECT victimname, SUM(pointearned) AS total_points " +
                        "FROM " + table + " " +
                        "WHERE killername = ? AND victimname = ?";

                PreparedStatement preparedStatementKillsXonY = connection.prepareStatement(queryKillsXonY);
                preparedStatementKillsXonY.setString(1, playerName);
                preparedStatementKillsXonY.setString(2, playerName);

                ResultSet resultSetKillsXonY = preparedStatementKillsXonY.executeQuery();

                while (resultSetKillsXonY.next()) {
                    String victimName = resultSetKillsXonY.getString("victimname");
                    double totalPoints = resultSetKillsXonY.getDouble("total_points");

                    playerInteractions.add(new PlayerInteraction(victimName, totalPoints));
                }

                resultSetKillsXonY.close();
                preparedStatementKillsXonY.close();

                // Zapytanie SQL do zliczania sumy zabójstw gracza Y na graczu X
                String queryKillsYonX = "SELECT killername, SUM(pointearned) AS total_points " +
                        "FROM " + table + " " +
                        "WHERE killername = ? AND victimname = ?";

                PreparedStatement preparedStatementKillsYonX = connection.prepareStatement(queryKillsYonX);
                preparedStatementKillsYonX.setString(1, playerName);
                preparedStatementKillsYonX.setString(2, playerName);

                ResultSet resultSetKillsYonX = preparedStatementKillsYonX.executeQuery();

                while (resultSetKillsYonX.next()) {
                    String killerName = resultSetKillsYonX.getString("killername");
                    double totalPoints = resultSetKillsYonX.getDouble("total_points");

                    playerInteractions.add(new PlayerInteraction(killerName, totalPoints));
                }

                resultSetKillsYonX.close();
                preparedStatementKillsYonX.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            interactionsMap.put(table, playerInteractions);
        }

        return interactionsMap;
    }
    public void clearTable(String tableName) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "PlayerKillDatabase: clearTable: called with parameters "+tableName);
        try {
            Connection connection = DriverManager.getConnection(DATABASE_URL);

            // Zapytanie SQL do usunięcia wszystkich rekordów z tabeli
            String query = "DELETE FROM " + tableName;

            Statement statement = connection.createStatement();
            statement.executeUpdate(query);

            statement.close();
            connection.close();
        } catch (SQLException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "PlayerKillDatabase: clearTable: "+ e.getMessage());
        }
    }
    public class PlayerInteraction {
        private String playerName;
        private double totalPoints;

        public PlayerInteraction(String playerName, double totalPoints) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2, "PlayerKillDatabase: PlayerInteraction: called with parameters "+playerName+" "+totalPoints);
            this.playerName = playerName;
            this.totalPoints = totalPoints;
        }

        public String getPlayerName() {
            return playerName;
        }

        public double getTotalPoints() {
            return totalPoints;
        }
    }





    private void createNewDatabase() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"PlayerKillDatabase: createNewDatabase called");
        try {
            Connection connection = DriverManager.getConnection(DATABASE_URL);

            // Tworzenie tabeli "daily"
            createTable(connection, "daily");

            // Tworzenie tabeli "weekly"
            createTable(connection, "weekly");

            // Tworzenie tabeli "monthly"
            createTable(connection, "monthly");

            // Tworzenie tabeli "main"
            createTable(connection, "main");

            // Zamykanie połączenia
            connection.close();
        } catch (SQLException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR,"PlayerKillDatabase: createNewDatabase: "+e.getMessage());
        }
    }

    private void createTable(Connection connection, String tableName) throws SQLException {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"PlayerKillDatabase: createTable called with paraemeters "+connection.toString()+" "+tableName);
        Statement statement = connection.createStatement();

        // Tworzenie tabeli z odpowiednimi kolumnami
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "killername TEXT NOT NULL," +
                "victimname TEXT NOT NULL," +
                "pointearned DOUBLE NOT NULL" +
                ");";

        statement.execute(createTableSQL);
        statement.close();
    }
}
