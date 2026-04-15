package dev.skypaolo.database;

import java.sql.*;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:data/vault_tec_games.db";
    private static DatabaseManager instance;

    private DatabaseManager() {
        initializeDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void initializeDatabase() {
        String createScoresTable = """
            CREATE TABLE IF NOT EXISTS scores_plus_ou_moins (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                player_name TEXT NOT NULL,
                score INTEGER NOT NULL,
                attempts_used INTEGER NOT NULL,
                secret_number INTEGER NOT NULL,
                game_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;

        String createPlayerStatsTable = """
            CREATE TABLE IF NOT EXISTS player_stats (
                player_name TEXT PRIMARY KEY,
                games_played INTEGER DEFAULT 0,
                best_score INTEGER DEFAULT 0,
                total_score INTEGER DEFAULT 0
            )
            """;

        String createQuestionsTable = """
            CREATE TABLE IF NOT EXISTS questions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                text TEXT NOT NULL,
                answer INTEGER NOT NULL,
                category TEXT NOT NULL
            )
            """;

        String createSnakePlayersTable = """
            CREATE TABLE IF NOT EXISTS players_snake (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE
            )
            """;

        String createSnakeScoresTable = """
            CREATE TABLE IF NOT EXISTS scores_snake (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                player_id INTEGER NOT NULL,
                score INTEGER NOT NULL,
                snake_length INTEGER NOT NULL,
                game_duration_ms INTEGER,
                date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (player_id) REFERENCES players_snake(id)
            )
            """;

        String createBlackjackPlayersTable = """
            CREATE TABLE IF NOT EXISTS players_blackjack (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                balance INTEGER DEFAULT 1000
            )
            """;

        String createBlackjackHistoryTable = """
            CREATE TABLE IF NOT EXISTS history_blackjack (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                player_id INTEGER NOT NULL,
                bet INTEGER NOT NULL,
                result TEXT NOT NULL,
                balance_after INTEGER NOT NULL,
                player_hand_value INTEGER,
                dealer_hand_value INTEGER,
                date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (player_id) REFERENCES players_blackjack(id)
            )
            """;

        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement()
        ) {
            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.execute("PRAGMA journal_mode = WAL");

            stmt.execute(createScoresTable);
            stmt.execute(createPlayerStatsTable);
            stmt.execute(createQuestionsTable);
            stmt.execute(createSnakePlayersTable);
            stmt.execute(createSnakeScoresTable);
            stmt.execute(createBlackjackPlayersTable);
            stmt.execute(createBlackjackHistoryTable);

            System.out.println("[VT-OS] Database initialized successfully.");
            System.out.println("[VT-OS] Vault-Tec data storage: OPERATIONAL");
        } catch (SQLException e) {
            System.err.println(
                "[VT-OS CRITICAL] Database initialization failed!"
            );
            System.err.println("[VT-OS ERROR] " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public boolean saveGameResult(
        String playerName,
        int score,
        int attemptsUsed,
        int secretNumber
    ) {
        String insertScoreSQL = """
            INSERT INTO scores_plus_ou_moins (player_name, score, attempts_used, secret_number)
            VALUES (?, ?, ?, ?)
            """;

        String upsertStatsSQL = """
            INSERT INTO player_stats (player_name, games_played, best_score, total_score)
            VALUES (?, 1, ?, ?)
            ON CONFLICT(player_name) DO UPDATE SET
                games_played = games_played + 1,
                best_score = CASE
                    WHEN excluded.best_score > best_score THEN excluded.best_score
                    ELSE best_score
                END,
                total_score = total_score + excluded.total_score
            """;

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try {
                try (
                    PreparedStatement pstmt = conn.prepareStatement(
                        insertScoreSQL
                    )
                ) {
                    pstmt.setString(1, playerName);
                    pstmt.setInt(2, score);
                    pstmt.setInt(3, attemptsUsed);
                    pstmt.setInt(4, secretNumber);
                    pstmt.executeUpdate();
                }

                try (
                    PreparedStatement pstmt = conn.prepareStatement(
                        upsertStatsSQL
                    )
                ) {
                    pstmt.setString(1, playerName);
                    pstmt.setInt(2, score);
                    pstmt.setInt(3, score);
                    pstmt.executeUpdate();
                }

                conn.commit();
                System.out.println(
                    "[VT-OS] Game result saved for player: " + playerName
                );
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("[VT-OS ERROR] Failed to save game result!");
            System.err.println("[VT-OS ERROR] " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public ResultSet getPlayerStats(String playerName) {
        String sql = "SELECT * FROM player_stats WHERE player_name = ?";

        try {
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, playerName);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            System.err.println(
                "[VT-OS ERROR] Failed to retrieve player stats!"
            );
            System.err.println("[VT-OS ERROR] " + e.getMessage());
            return null;
        }
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println(
                "[VT-OS ERROR] Database connection test failed!"
            );
            return false;
        }
    }

    public int getOrCreateSnakePlayerId(String playerName) {
        String selectSQL = "SELECT id FROM players_snake WHERE name = ?";
        String insertSQL = "INSERT INTO players_snake (name) VALUES (?)";

        try (Connection conn = getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
                pstmt.setString(1, playerName);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, playerName);
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    System.out.println("[VT-OS] New Snake player registered: " + playerName);
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[VT-OS ERROR] Failed to get/create Snake player: " + e.getMessage());
        }
        return -1;
    }

    public boolean saveSnakeScore(String playerName, int score, int snakeLength, long durationMs) {
        String insertSQL = """
            INSERT INTO scores_snake (player_id, score, snake_length, game_duration_ms)
            VALUES (?, ?, ?, ?)
            """;

        int playerId = getOrCreateSnakePlayerId(playerName);
        if (playerId == -1) {
            System.err.println("[VT-OS ERROR] Cannot save score - invalid player ID");
            return false;
        }

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setInt(1, playerId);
            pstmt.setInt(2, score);
            pstmt.setInt(3, snakeLength);
            pstmt.setLong(4, durationMs);
            pstmt.executeUpdate();

            System.out.println("[VT-OS] Snake score saved for player: " + playerName);
            return true;

        } catch (SQLException e) {
            System.err.println("[VT-OS ERROR] Failed to save Snake score: " + e.getMessage());
            return false;
        }
    }

    public ResultSet getTopSnakeScores(int limit) {
        String sql = """
            SELECT ps.name, ss.score, ss.snake_length, ss.game_duration_ms, ss.date
            FROM scores_snake ss
            JOIN players_snake ps ON ss.player_id = ps.id
            ORDER BY ss.score DESC
            LIMIT ?
            """;

        try {
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("[VT-OS ERROR] Failed to retrieve Snake scores: " + e.getMessage());
            return null;
        }
    }

    public int getPlayerBestSnakeScore(String playerName) {
        String sql = """
            SELECT MAX(ss.score) as best_score
            FROM scores_snake ss
            JOIN players_snake ps ON ss.player_id = ps.id
            WHERE ps.name = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, playerName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("best_score");
            }
        } catch (SQLException e) {
            System.err.println("[VT-OS ERROR] Failed to get best Snake score: " + e.getMessage());
        }
        return 0;
    }

    public int getOrCreateBlackjackPlayerId(String playerName) {
        String selectSQL = "SELECT id FROM players_blackjack WHERE name = ?";
        String insertSQL = "INSERT INTO players_blackjack (name, balance) VALUES (?, 1000)";

        try (Connection conn = getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
                pstmt.setString(1, playerName);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, playerName);
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    System.out.println("[VT-OS] New Blackjack player registered: " + playerName);
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[VT-OS ERROR] Failed to get/create Blackjack player: " + e.getMessage());
        }
        return -1;
    }

    public int getBlackjackPlayerBalance(int playerId) {
        String sql = "SELECT balance FROM players_blackjack WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, playerId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("balance");
            }
        } catch (SQLException e) {
            System.err.println("[VT-OS ERROR] Failed to get player balance: " + e.getMessage());
        }
        return 0;
    }

    public boolean updateBlackjackPlayerBalance(int playerId, int newBalance) {
        String sql = "UPDATE players_blackjack SET balance = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, newBalance);
            pstmt.setInt(2, playerId);
            pstmt.executeUpdate();

            return true;
        } catch (SQLException e) {
            System.err.println("[VT-OS ERROR] Failed to update player balance: " + e.getMessage());
        }
        return false;
    }

    public boolean saveBlackjackGameResult(int playerId, int bet, String result, 
                                           int balanceAfter, int playerHandValue, int dealerHandValue) {
        String insertSQL = """
            INSERT INTO history_blackjack (player_id, bet, result, balance_after, player_hand_value, dealer_hand_value)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setInt(1, playerId);
            pstmt.setInt(2, bet);
            pstmt.setString(3, result);
            pstmt.setInt(4, balanceAfter);
            pstmt.setInt(5, playerHandValue);
            pstmt.setInt(6, dealerHandValue);
            pstmt.executeUpdate();

            System.out.println("[VT-OS] Blackjack game result saved. Result: " + result);
            return true;

        } catch (SQLException e) {
            System.err.println("[VT-OS ERROR] Failed to save Blackjack game result: " + e.getMessage());
        }
        return false;
    }

    public ResultSet getBlackjackGameHistory(int playerId, int limit) {
        String sql = """
            SELECT bet, result, balance_after, player_hand_value, dealer_hand_value, date
            FROM history_blackjack
            WHERE player_id = ?
            ORDER BY date DESC
            LIMIT ?
            """;

        try {
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, playerId);
            pstmt.setInt(2, limit);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("[VT-OS ERROR] Failed to retrieve game history: " + e.getMessage());
            return null;
        }
    }
}
