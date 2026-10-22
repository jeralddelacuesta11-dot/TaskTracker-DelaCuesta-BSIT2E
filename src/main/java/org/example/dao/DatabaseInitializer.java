package org.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() throws SQLException {

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    roles TEXT NOT NULL
                        CHECK (roles IN ('ADMIN', 'MANAGER', 'EMPLOYEE')),
                    status TEXT NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING', 'APPROVED'))
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    description TEXT,
                    assigned_to INTEGER NOT NULL,
                    created_by INTEGER NOT NULL,
                    deadline TEXT,
                    priority TEXT NOT NULL DEFAULT 'MEDIUM',
                    status TEXT NOT NULL DEFAULT 'PENDING',
                    FOREIGN KEY (assigned_to) REFERENCES users(id),
                    FOREIGN KEY (created_by) REFERENCES users(id)
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS activity_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    activity TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
                """);
        }

        createDefaultAdmin();
    }

    private static void createDefaultAdmin() throws SQLException {
        String sql = """
            INSERT OR IGNORE INTO users
                (username, password, roles, status)
            VALUES (?, ?, 'ADMIN', 'APPROVED')
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "admin");
            statement.setString(2, "admin123");
            statement.executeUpdate();
        }
    }
}