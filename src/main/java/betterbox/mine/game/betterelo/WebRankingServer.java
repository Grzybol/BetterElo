package betterbox.mine.game.betterelo;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.stream.Collectors;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class WebRankingServer {

    private final String databasePath;
    private final int port;
    private final PluginLogger pluginLogger;

    public WebRankingServer(String databasePath, int port, PluginLogger pluginLogger) {
        this.databasePath = databasePath;
        this.port = port;
        this.pluginLogger = pluginLogger;
    }

    public void startServer() {
        try {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "WebRankingServer: startServer: starting server on port " + port + ".");
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/ranking", new RankingHandler(databasePath));
            server.start();
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "WebRankingServer: startServer: server started successfully.");
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "WebRankingServer: startServer: failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static class RankingHandler implements HttpHandler {

        private final String databasePath;

        public RankingHandler(String databasePath) {
            this.databasePath = databasePath;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String response = generateHtmlResponse();
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } catch (Exception e) {
                String errorMsg = "RankingHandler: handle: error handling HTTP exchange: " + e.getMessage();
                //pluginLogger.log(PluginLogger.LogLevel.DEBUG,"WebRankingServer: startServer: server started successfully.");
                System.err.println(errorMsg);  // Log to server console, replace with a logger if available.
                e.printStackTrace();
                exchange.sendResponseHeaders(500, errorMsg.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(errorMsg.getBytes());
                }
            }
        }


        private String generateHtmlResponse() throws IOException {
            StringBuilder html = new StringBuilder();
            html.append("<html><head><title>BetterBox Main Ranking</title><style>");
            html.append("li.low { color: red; }");
            html.append("li.medium { color: green; }");
            html.append("li.high { color: blue; }");
            html.append("</style></head><body>");
            html.append("<h1>Main Player Ranking</h1>");
            html.append("<ul>");

            List<PlayerData> players = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(databasePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        try {
                            double score = Double.parseDouble(parts[1].trim());
                            players.add(new PlayerData(parts[0], score));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            players.sort(Comparator.comparingDouble(PlayerData::getScore).reversed());

            double totalScore = 0.0;

            for (PlayerData player : players) {
                String cssClass;
                if (player.getScore() < 0) {
                    cssClass = "low";
                } else if (player.getScore() <= 2500) {
                    cssClass = "medium";
                } else {
                    cssClass = "high";
                }

                html.append(String.format(Locale.US, "<li class=\"%s\">%s: %.2f</li>", cssClass, player.getName(), player.getScore()));
                totalScore += player.getScore();
            }

            // Calculate and display the additional statistics
            double averageScore = players.isEmpty() ? 0 : totalScore / players.size();
            html.append(String.format(Locale.US, "</ul><p>Total Players: %d</p>", players.size()));
            html.append(String.format(Locale.US, "<p>Total Score: %.2f</p>", totalScore));
            html.append(String.format(Locale.US, "<p>Average Score: %.2f</p>", averageScore));
            html.append("</body></html>");

            return html.toString();
        }


        static class PlayerData {
            private final String name;
            private final double score;  // Zmieniamy typ danych na double

            public PlayerData(String name, double score) {
                this.name = name;
                this.score = score;
            }

            public String getName() {
                return name;
            }

            public double getScore() {  // Zmieniamy typ zwracanej wartości na double
                return score;
            }
        }
    }
}
