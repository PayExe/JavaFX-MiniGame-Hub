package dev.skypaolo.database;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * Vault-Tec Database Manager
 * Manages SQLite database connections and operations for the Mini Game Hub.
 *
 * S.P.E.C.I.A.L. Stats:
 * - Intelligence: Complex SQL operations
 * - Endurance: Connection pooling and error handling
 * - Perception: Query optimization
 *
 * Database Location: data/vault_tec_games.db (Project directory)
 * Vault-Tec is not responsible for any data corruption due to RadRoach infestation.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:data/vault_tec_games.db";
    private static DatabaseManager instance;

    /**
     * Private constructor - Vault-Tec approved singleton pattern
     */
    private DatabaseManager() {
        initializeDatabase();
    }

    /**
     * Get singleton instance
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Initialize database - create tables if they don't exist
     * Vault-Tec standard database schema deployment
     */
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

        // Snake Game tables
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

        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement()
        ) {
            // Enable foreign keys and WAL mode for better performance
            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.execute("PRAGMA journal_mode = WAL");

            // Create tables
            stmt.execute(createScoresTable);
            stmt.execute(createPlayerStatsTable);
            stmt.execute(createQuestionsTable);
            stmt.execute(createSnakePlayersTable);
            stmt.execute(createSnakeScoresTable);

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

    /**
     * Get database connection
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Save a game result to the database
     *
     * @param playerName Player name
     * @param score Calculated score
     * @param attemptsUsed Number of attempts used
     * @param secretNumber The secret number that was guessed
     * @return true if save successful, false otherwise
     */
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
                // Insert score record
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

                // Update player stats
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

    /**
     * Get player statistics
     *
     * @param playerName Player name
     * @return ResultSet with player stats, or null if not found
     */
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

    /**
     * Test database connection
     * Vault-Tec diagnostic routine
     */
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

    // ============================================
    // SNAKE GAME DATABASE METHODS
    // ============================================

    /**
     * Get or create a player ID for Snake game
     *
     * @param playerName Player name
     * @return player ID, or -1 if error
     */
    public int getOrCreateSnakePlayerId(String playerName) {
        String selectSQL = "SELECT id FROM players_snake WHERE name = ?";
        String insertSQL = "INSERT INTO players_snake (name) VALUES (?)";

        try (Connection conn = getConnection()) {
            // Try to find existing player
            try (PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
                pstmt.setString(1, playerName);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }

            // Create new player
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

    /**
     * Save a Snake game score to the database
     *
     * @param playerName Player name
     * @param score Game score
     * @param snakeLength Length of snake at game end
     * @param durationMs Game duration in milliseconds
     * @return true if save successful, false otherwise
     */
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

    /**
     * Get top Snake scores for leaderboard
     *
     * @param limit Number of scores to retrieve
     * @return ResultSet with player name, score, and date
     */
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

    /**
     * Get player's best Snake score
     *
     * @param playerName Player name
     * @return Best score, or 0 if no games played
     */
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
}
