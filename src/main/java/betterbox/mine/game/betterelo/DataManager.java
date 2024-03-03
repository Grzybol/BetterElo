package betterbox.mine.game.betterelo;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.*;
import java.util.*;
public class DataManager {
    private final JavaPlugin plugin;
    private final File dataFolder;
    private final File databaseFile;
    private final File dailyDatabaseFile;
    private final File weeklyDatabaseFile;
    private final File monthlyDatabaseFile;
    private final File eventDatabasefile;
    public final Map<String, Double> playerPoints = new HashMap<>();
    public final Map<String, Double> dailyPlayerPoints = new HashMap<>();
    public final Map<String, Double> weeklyPlayerPoints = new HashMap<>();
    public final Map<String, Double> monthlyPayerPoints = new HashMap<>();
    public final Map<String, Double> eventPlayerPoints = new HashMap<>();
    private final PluginLogger pluginLogger; // Dodajemy referencję do PluginLogger
    private final Map<String, Map<String, Double>> allPlayerPoints = new HashMap<>();
    public DataManager(JavaPlugin plugin, PluginLogger pluginLogger) {
        this.plugin = plugin;
        this.pluginLogger = pluginLogger; // Inicjalizujemy PluginLogger
        this.dataFolder = plugin.getDataFolder();
        this.databaseFile = new File(dataFolder, "database.txt");
        this.dailyDatabaseFile = new File(dataFolder, "daily_database.txt");
        this.weeklyDatabaseFile = new File(dataFolder, "weekly_database.txt");
        this.monthlyDatabaseFile = new File(dataFolder, "monthly_database.txt");
        this.eventDatabasefile = new File(dataFolder, "event_database.txt");
        allPlayerPoints.put("main", playerPoints);
        allPlayerPoints.put("daily", dailyPlayerPoints);
        allPlayerPoints.put("weekly", weeklyPlayerPoints);
        allPlayerPoints.put("monthly", monthlyPayerPoints);
        allPlayerPoints.put("event", eventPlayerPoints);
    }
    public void setPoints(String playerUUID, double points, String ranking_type) {
        if (ranking_type.equals("main")) playerPoints.put(playerUUID, points);
        if (ranking_type.equals("daily")) dailyPlayerPoints.put(playerUUID, points);
        if (ranking_type.equals("weekly")) weeklyPlayerPoints.put(playerUUID, points);
        if (ranking_type.equals("monthly")) monthlyPayerPoints.put(playerUUID, points);
        if (ranking_type.equals("event")) eventPlayerPoints.put(playerUUID, points);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: setPoints: zapisywanie do bazy..");
        saveDataToFile(); // Zapisz zmienione dane do pliku
        saveDataToFileDaily(); // Zapisz zmienione dane do pliku
        saveDataToFileWeekly(); // Zapisz zmienione dane do pliku
        saveDataToFileMonthly(); // Zapisz zmienione dane do pliku
        saveDataToFileEvent(); // Zapisz zmienione dane do pliku
    }

    public double getPoints(String playerUUID, String ranking_type) {
        //pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: getPoints called with parameters: playerUUID "+playerUUID+" ranking_type "+ranking_type);
        Map<String, Double> pointsMap = allPlayerPoints.get(ranking_type);
        if (pointsMap == null) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: pointsMap is null! Returning 1000 points");
            return 1000;  // Nieznany typ rankingu
        }
        if (!pointsMap.containsKey(playerUUID)) {
            //pluginLogger.log("DataManager: getPoints: default" + capitalize(ranking_type) + " 1000");
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: pointsMap does not contain "+playerUUID+", Returning 1000 points");
            return 1000;  // Gracz nie jest w rankingu
        }
        //pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: returning "+pointsMap.get(playerUUID)+" for player "+playerUUID);
        //pluginLogger.log("DataManager: getPoints: " + ranking_type + ": " + pointsMap.get(playerUUID));
        return pointsMap.get(playerUUID);
    }
    public void initializeDataFolder() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }
    public void initializeDatabaseFile() {
        initializeFile(databaseFile, "databaseFile");
        initializeFile(dailyDatabaseFile, "dailyDatabaseFile");
        initializeFile(weeklyDatabaseFile, "weeklyDatabaseFile");
        initializeFile(monthlyDatabaseFile, "monthlyDatabaseFile");
        initializeFile(eventDatabasefile, "eventDatabaseFile");
    }
    private void initializeFile(File file, String fileName) {
        if (!file.exists()) {
            try {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: initializeFile file created.");
                file.createNewFile();

            } catch (IOException e) {
                pluginLogger.log(PluginLogger.LogLevel.ERROR,"DataManager: initializeFile: error: "+e);
                e.printStackTrace();
            }
        }
    }
    public int getPlayerRank(String playerUUID) {
        //pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: getPlayerRank called with parameters "+playerUUID);
        if (!playerPoints.containsKey(playerUUID)) {
            return -1; // Gracz nie jest w rankingu

        }
        double playerPoints = getPoints(playerUUID,"main");
        Map<String, Double> sortedPlayers = sortPlayersByPoints(this.playerPoints);
        int rank = 1;
        for (Map.Entry<String, Double> entry : sortedPlayers.entrySet()) {
            if (entry.getKey().equals(playerUUID)) {
                return rank;
            }
            rank++;
        }
        return rank; // Gracz nie jest w rankingu
    }
    public int getPlayerRank(String playerUUID, String ranking) {
        //pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: getPlayerRank called with parameters "+playerUUID);
        if (!playerPoints.containsKey(playerUUID)) {
            return -1; // Gracz nie jest w rankingu

        }
        Map<String, Double> sortedPlayers = sortPlayersByPoints(this.playerPoints);
        switch (ranking){
            case "main":
                sortedPlayers = sortPlayersByPoints(this.playerPoints);
                break;
            case "daily":
                sortedPlayers = sortPlayersByPoints(this.dailyPlayerPoints);
                break;
            case "weekly":
                sortedPlayers = sortPlayersByPoints(this.weeklyPlayerPoints);
                break;
            case "monthly":
                sortedPlayers = sortPlayersByPoints(this.monthlyPayerPoints);
                break;
            case "event":
                sortedPlayers = sortPlayersByPoints(this.eventPlayerPoints);
                break;

        }

        int rank = 1;
        for (Map.Entry<String, Double> entry : sortedPlayers.entrySet()) {
            if (entry.getKey().equals(playerUUID)) {
                return rank;
            }
            rank++;
        }
        return rank; // Gracz nie jest w rankingu
    }
    public void loadDataFromFile() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: loadDataFromFile called");
        playerPoints.clear();
        dailyPlayerPoints.clear();
        weeklyPlayerPoints.clear();
        monthlyPayerPoints.clear();
        eventPlayerPoints.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(databaseFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String playerUUID = parts[0];
                    double points = Double.parseDouble(parts[1]);
                    playerPoints.put(playerUUID, points);
                }
            }
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR,"DataManager: loadDataFromFile: error: "+e);

        }
        try (BufferedReader reader = new BufferedReader(new FileReader(dailyDatabaseFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String playerUUID = parts[0];
                    double points = Double.parseDouble(parts[1]);
                    dailyPlayerPoints.put(playerUUID, points);
                }
            }
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR,"DataManager: loadDataFromFile: error: "+e);
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(weeklyDatabaseFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String playerUUID = parts[0];
                    double points = Double.parseDouble(parts[1]);
                    weeklyPlayerPoints.put(playerUUID, points);
                }
            }
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR,"DataManager: loadDataFromFile: error: "+e);
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(monthlyDatabaseFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String playerUUID = parts[0];
                    double points = Double.parseDouble(parts[1]);
                    monthlyPayerPoints.put(playerUUID, points);
                }
            }
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR,"DataManager: loadDataFromFile: error: "+e);
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(eventDatabasefile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String playerUUID = parts[0];
                    double points = Double.parseDouble(parts[1]);
                    eventPlayerPoints.put(playerUUID, points);
                }
            }
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR,"DataManager: loadDataFromFile: error: "+e);
        }

        // Używamy nowego loggera do zapisywania wiadomości debugujących
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: loadDataFromFile: Data loaded");
    }
    public void saveDataToFileDaily() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: saveDataToFileDaily called");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dailyDatabaseFile))) {
            for (Map.Entry<String, Double> entry : dailyPlayerPoints.entrySet()) {
                String playerUUID = entry.getKey();
                double points = entry.getValue();
                if (!playerUUID.equals("AIR")) {
                    writer.write(playerUUID + ":" + points);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR,"DataManager: saveDataToFileDaily: error: "+e);
        }
        // Używamy nowego loggera do zapisywania wiadomości debugujących
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: saveDataToFileDaily saved");
    }
    public void saveDataToFileWeekly() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: saveDataToFileWeekly called");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(weeklyDatabaseFile))) {
            for (Map.Entry<String, Double> entry : weeklyPlayerPoints.entrySet()) {
                String playerUUID = entry.getKey();
                double points = entry.getValue();
                if (!playerUUID.equals("AIR")) {
                    writer.write(playerUUID + ":" + points);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR,"DataManager: saveDataToFileWeekly: error: "+e);
        }
        // Używamy nowego loggera do zapisywania wiadomości debugujących
        //pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: saveDataToFileWeekly saved");
    }
    public void saveDataToFileMonthly() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: saveDataToFileMonthly called");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(monthlyDatabaseFile))) {
            for (Map.Entry<String, Double> entry : monthlyPayerPoints.entrySet()) {
                String playerUUID = entry.getKey();
                double points = entry.getValue();
                if (!playerUUID.equals("AIR")) {
                    writer.write(playerUUID + ":" + points);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR,"DataManager: saveDataToFileMonthly: error: "+e);
        }
        // Używamy nowego loggera do zapisywania wiadomości debugujących
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: saveDataToFileMonthly saved");
    }
    public void saveDataToFileEvent() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: saveDataToFileEvent called");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(eventDatabasefile))) {
            for (Map.Entry<String, Double> entry : eventPlayerPoints.entrySet()) {
                String playerUUID = entry.getKey();
                double points = entry.getValue();
                if (!playerUUID.equals("AIR")) {
                    writer.write(playerUUID + ":" + points);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR,"DataManager: saveDataToFileEvent: error: "+e);
        }
        // Używamy nowego loggera do zapisywania wiadomości debugujących
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: saveDataToFileEvent saved");
    }
    public void saveDataToFile() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: saveDataToFile called");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(databaseFile))) {
            for (Map.Entry<String, Double> entry : playerPoints.entrySet()) {
                String playerUUID = entry.getKey();
                double points = entry.getValue();
                writer.write(playerUUID + ":" + points);
                writer.newLine();
            }
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR,"DataManager: saveDataToFile: error: "+e);
        }

        // Używamy nowego loggera do zapisywania wiadomości debugujących
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: saveDataToFile saved");
    }
    public double getExtremeElo(String ranking_type, boolean isMax) {
        //pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: getExtremeElo called with parameters "+ranking_type+" isMax "+isMax);
        Map<String, Double> playerPointsMap = null;
        switch (ranking_type) {
            case "main":
                playerPointsMap = playerPoints;
                break;
            case "daily":
                playerPointsMap = dailyPlayerPoints;
                break;
            case "weekly":
                playerPointsMap = weeklyPlayerPoints;
                break;
            case "monthly":
                playerPointsMap = monthlyPayerPoints; // Poprawienie literówki z "monthlyPayerPoints"
                break;
            case "event":
                playerPointsMap = eventPlayerPoints; // Poprawienie literówki z "monthlyPayerPoints"
                break;
            default:
                break;
        }
        if (playerPointsMap == null) {
            return 1000;
        }
        if (playerPointsMap.isEmpty()) {
            //pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: getExtremeElo: default" + capitalize(ranking_type) + " 1000");
            return 1000;
        } else {
            double extremeValue = isMax ? Collections.max(playerPointsMap.values()) : Collections.min(playerPointsMap.values());
            //pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: getExtremeElo: " + capitalize(ranking_type) + ": " + extremeValue);
            return extremeValue;
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    public double getMaxElo(String ranking_type) {
        //pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: getMaxElo called "+ranking_type);
        return getExtremeElo(ranking_type, true);
    }
    public double getMinElo(String ranking_type) {
        //pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: getMinElo called "+ranking_type);
        return getExtremeElo(ranking_type, false);
    }
    public double getPointsAtPosition(int position, Map<String, Double> points) {
        //pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: getPointsAtPosition called "+position);
        Map<String, Double> sortedPlayers = sortPlayersByPoints(points);

        int rank = 1;
        for (Map.Entry<String, Double> entry : sortedPlayers.entrySet()) {
            if (rank == position) {

                return entry.getValue(); // Znaleziono gracza na danej pozycji w rankingu
            }
            rank++;
        }

        return 0; // Brak gracza na danej pozycji w rankingu
    }
    public String getPlayerAtPosition(int position, Map<String, Double> points) {
        //pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: getPlayerAtPosition called "+position);
        Map<String, Double> sortedPlayers = sortPlayersByPoints(points);

        int rank = 1;
        for (Map.Entry<String, Double> entry : sortedPlayers.entrySet()) {
            if (rank == position) {
                // Znaleziono gracza na danej pozycji w rankingu
                return getPlayerName(entry.getKey());
            }
            rank++;
        }
        return null; // Brak gracza na danej pozycji w rankingu
    }
    public String getPlayerName(String playerUUID) {
        //pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: getPlayerName called "+playerUUID);
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID));
        return offlinePlayer != null ? offlinePlayer.getName() : null;
    }
    public OfflinePlayer getOfflinePlayer(String playerIdentifier) {
        // Sprawdź, czy gracz jest online
        Player onlinePlayer = Bukkit.getPlayer(playerIdentifier);
        if (onlinePlayer != null) {
            return onlinePlayer;
        }
        // Gracz jest offline - próba uzyskania obiektu OfflinePlayer
        UUID playerUUID;
        try {
            playerUUID = UUID.fromString(playerIdentifier);
        } catch (IllegalArgumentException e) {
            return null; // Nieprawidłowy identyfikator gracza
        }

        return Bukkit.getOfflinePlayer(playerUUID);
    }
    public boolean playerExists(String playerUUID,String reward_type) {
        //pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: playerExists called "+playerUUID+" "+reward_type);
        loadDataFromFile(); // Wczytaj dane z pliku przed sprawdzeniem
        switch (reward_type){
            case "main":
                return playerPoints.containsKey(playerUUID);

            case "daily":
                return dailyPlayerPoints.containsKey(playerUUID);

            case "weekly":
                return weeklyPlayerPoints.containsKey(playerUUID);

            case "monthly":
                return monthlyPayerPoints.containsKey(playerUUID);
            case "event":
                return eventPlayerPoints.containsKey(playerUUID);

            default:
                return playerPoints.containsKey(playerUUID);

        }


    }
    private Map<String, Double> sortPlayersByPoints(Map<String, Double> points) {
        //pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: sortPlayersByPoints called ");
        Map<String, Double> sortedPlayers = new LinkedHashMap<>();

        points.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEachOrdered(entry -> sortedPlayers.put(entry.getKey(), entry.getValue()));
        return sortedPlayers;
    }
}
