package kz.atu.lab12.repository;

import kz.atu.lab12.model.Request;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RequestRepository {
    private static final String URL = "jdbc:sqlite:requests.db";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public void initializeDatabase() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS requests (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                theme TEXT NOT NULL,
                priority TEXT NOT NULL,
                status TEXT NOT NULL,
                created_at TEXT NOT NULL
            )
            """;
        try (Connection c = DriverManager.getConnection(URL);
             Statement st = c.createStatement()) {
            st.execute(sql);
            // Миграция если старая таблица без created_at
            try {
                st.execute("ALTER TABLE requests ADD COLUMN created_at TEXT DEFAULT ''");
            } catch (SQLException ignored) {}
        }
    }

    public List<Request> findAll() throws SQLException {
        List<Request> result = new ArrayList<>();
        String sql = "SELECT id, theme, priority, status, created_at FROM requests ORDER BY id DESC";
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Request(
                    rs.getInt("id"),
                    rs.getString("theme"),
                    rs.getString("priority"),
                    rs.getString("status"),
                    rs.getString("created_at") != null ? rs.getString("created_at") : ""
                ));
            }
        }
        return result;
    }

    public void insert(String theme, String priority, String status) throws SQLException {
        String sql = "INSERT INTO requests (theme, priority, status, created_at) VALUES (?, ?, ?, ?)";
        String now = LocalDateTime.now().format(FORMATTER);
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, theme);
            ps.setString(2, priority);
            ps.setString(3, status);
            ps.setString(4, now);
            ps.executeUpdate();
        }
    }

    public int update(int id, String theme, String priority, String status) throws SQLException {
        String sql = "UPDATE requests SET theme=?, priority=?, status=? WHERE id=?";
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, theme);
            ps.setString(2, priority);
            ps.setString(3, status);
            ps.setInt(4, id);
            return ps.executeUpdate();
        }
    }

    public int delete(int id) throws SQLException {
        String sql = "DELETE FROM requests WHERE id=?";
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }
}
