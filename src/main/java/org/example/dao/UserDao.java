package org.example.dao;

import org.example.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    public boolean register(String username, String password, String role)
            throws SQLException {

        String sql = """
            INSERT INTO users (username, password, roles, status)
            VALUES (?, ?, ?, 'PENDING')
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, role);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            if (e.getErrorCode() == 19) {
                return false;
            }
            throw e;
        }
    }

    public User login(String username, String password) throws SQLException {
        String sql = """
            SELECT id, username, password, roles, status
            FROM users
            WHERE username = ? AND password = ?
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new User(
                            result.getInt("id"),
                            result.getString("username"),
                            result.getString("password"),
                            result.getString("roles"),
                            result.getString("status")
                    );
                }
            }
        }

        return null;
    }

    public User getUserByUsername(String username) throws SQLException {
        String sql = """
            SELECT id, username, password, roles, status
            FROM users WHERE username = ?
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new User(
                            result.getInt("id"),
                            result.getString("username"),
                            result.getString("password"),
                            result.getString("roles"),
                            result.getString("status")
                    );
                }
            }
        }

        return null;
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();

        String sql = """
            SELECT id, username, password, roles, status
            FROM users ORDER BY id
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                users.add(new User(
                        result.getInt("id"),
                        result.getString("username"),
                        result.getString("password"),
                        result.getString("roles"),
                        result.getString("status")
                ));
            }
        }

        return users;
    }

    public List<User> getPendingUsers() throws SQLException {
        List<User> users = new ArrayList<>();

        String sql = """
            SELECT id, username, password, roles, status
            FROM users
            WHERE status = 'PENDING'
            ORDER BY id
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                users.add(new User(
                        result.getInt("id"),
                        result.getString("username"),
                        result.getString("password"),
                        result.getString("roles"),
                        result.getString("status")
                ));
            }
        }

        return users;
    }

    public boolean approveUser(int userId) throws SQLException {
        String sql = """
            UPDATE users
            SET status = 'APPROVED'
            WHERE id = ? AND status = 'PENDING'
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean createTask(
            String title,
            String description,
            int assignedTo,
            int createdBy,
            String deadline,
            String priority
    ) throws SQLException {

        String sql = """
            INSERT INTO tasks
                (title, description, assigned_to, created_by, deadline, priority)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, title);
            statement.setString(2, description);
            statement.setInt(3, assignedTo);
            statement.setInt(4, createdBy);
            statement.setString(5, deadline);
            statement.setString(6, priority);

            return statement.executeUpdate() > 0;
        }
    }

    public void showTasksForEmployee(int userId) throws SQLException {
        String sql = """
            SELECT id, title, description, deadline, priority, status
            FROM tasks
            WHERE assigned_to = ?
            ORDER BY id DESC
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet result = statement.executeQuery()) {
                System.out.println("\n========== MY TASKS ==========");
                boolean found = false;

                while (result.next()) {
                    found = true;
                    System.out.println("\nTask ID: " + result.getInt("id"));
                    System.out.println("Title: " + result.getString("title"));
                    System.out.println("Description: " + result.getString("description"));
                    System.out.println("Deadline: " + result.getString("deadline"));
                    System.out.println("Priority: " + result.getString("priority"));
                    System.out.println("Status: " + result.getString("status"));
                }

                if (!found) {
                    System.out.println("No tasks assigned to you yet.");
                }
            }
        }
    }

    public boolean updateTaskStatus(
            int taskId, int employeeId, String status
    ) throws SQLException {

        String sql = """
            UPDATE tasks
            SET status = ?
            WHERE id = ? AND assigned_to = ?
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setInt(2, taskId);
            statement.setInt(3, employeeId);

            return statement.executeUpdate() > 0;
        }
    }

    public void logActivity(int userId, String activity) throws SQLException {
        String sql = """
            INSERT INTO activity_logs (user_id, activity)
            VALUES (?, ?)
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, activity);
            statement.executeUpdate();
        }
    }

    public void showActivityLogs() throws SQLException {
        String sql = """
            SELECT activity_logs.id, users.username,
                   activity_logs.activity, activity_logs.created_at
            FROM activity_logs
            LEFT JOIN users ON activity_logs.user_id = users.id
            ORDER BY activity_logs.id DESC
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            System.out.println("\n========== ACTIVITY LOGS ==========");
            boolean found = false;

            while (result.next()) {
                found = true;
                System.out.println(
                        result.getInt("id") + " | " +
                                result.getString("username") + " | " +
                                result.getString("activity") + " | " +
                                result.getString("created_at")
                );
            }

            if (!found) {
                System.out.println("No activity recorded yet.");
            }
        }
    }
}