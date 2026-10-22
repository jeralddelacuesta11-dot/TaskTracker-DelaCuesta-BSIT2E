package org.example.ui;

import org.example.dao.UserDao;
import org.example.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Authentication {

    private final Scanner scanner;
    private final UserDao userDao;

    public Authentication(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
    }

    public void start() {
        while (true) {
            System.out.println("\n========== TASKPILOT ==========");
            System.out.println("1. Login");
            System.out.println("2. Register Account");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> login();
                    case "2" -> register();
                    case "0" -> {
                        System.out.println("Thank you for using TaskPilot!");
                        return;
                    }
                    default -> System.out.println("Invalid option.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private void register() throws SQLException {
        System.out.println("\n========== REGISTER ACCOUNT ==========");

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if (username.isBlank() || password.isBlank()) {
            System.out.println("Username and password cannot be empty.");
            return;
        }

        System.out.println("\nChoose role:");
        System.out.println("1. Admin");
        System.out.println("2. Manager");
        System.out.println("3. Employee");
        System.out.print("Choose an option: ");

        String choice = scanner.nextLine().trim();
        String role;

        switch (choice) {
            case "1" -> role = "ADMIN";
            case "2" -> role = "MANAGER";
            case "3" -> role = "EMPLOYEE";
            default -> {
                System.out.println("Invalid role.");
                return;
            }
        }

        if (userDao.register(username, password, role)) {
            System.out.println("Registration successful!");
            System.out.println("Account status: PENDING");
            System.out.println("Wait for an administrator to approve your account.");
        } else {
            System.out.println("Registration failed. Username may already exist.");
        }
    }

    private void login() throws SQLException {
        System.out.println("\n========== LOGIN ==========");

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        User user = userDao.login(username, password);

        if (user == null) {
            System.out.println("Incorrect username or password.");
            return;
        }

        if (!user.status().equals("APPROVED")) {
            System.out.println("Your account is still PENDING approval.");
            return;
        }

        userDao.logActivity(user.id(), "Logged in");
        System.out.println("Welcome, " + user.username() + "!");

        switch (user.role()) {
            case "ADMIN" -> adminMenu(user);
            case "MANAGER" -> managerMenu(user);
            case "EMPLOYEE" -> employeeMenu(user);
            default -> System.out.println("Unknown role.");
        }

        userDao.logActivity(user.id(), "Logged out");
    }

    private void adminMenu(User user) throws SQLException {
        while (true) {
            System.out.println("\n========== ADMIN MENU ==========");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending Accounts");
            System.out.println("3. Approve Account");
            System.out.println("4. View Activity Logs");
            System.out.println("0. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    List<User> users = userDao.getAllUsers();
                    System.out.println("\nID | USERNAME | ROLE | STATUS");

                    for (User account : users) {
                        System.out.printf("%d | %s | %s | %s%n",
                                account.id(), account.username(),
                                account.role(), account.status());
                    }
                }
                case "2" -> {
                    List<User> users = userDao.getPendingUsers();

                    if (users.isEmpty()) {
                        System.out.println("No pending accounts.");
                    } else {
                        for (User account : users) {
                            System.out.printf("ID: %d | Username: %s | Role: %s%n",
                                    account.id(), account.username(), account.role());
                        }
                    }
                }
                case "3" -> {
                    List<User> pending = userDao.getPendingUsers();

                    if (pending.isEmpty()) {
                        System.out.println("No accounts need approval.");
                        break;
                    }

                    System.out.print("Enter user ID to approve: ");
                    String input = scanner.nextLine().trim();

                    try {
                        int id = Integer.parseInt(input);

                        if (userDao.approveUser(id)) {
                            userDao.logActivity(user.id(), "Approved user ID " + id);
                            System.out.println("Account approved successfully.");
                        } else {
                            System.out.println("Invalid ID or account already approved.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Enter a valid numeric ID.");
                    }
                }
                case "4" -> userDao.showActivityLogs();
                case "0" -> {
                    System.out.println("Logging out...");
                    return;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void managerMenu(User user) throws SQLException {
        while (true) {
            System.out.println("\n========== MANAGER MENU ==========");
            System.out.println("1. Create and Assign Task");
            System.out.println("0. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> createTask(user);
                case "0" -> {
                    System.out.println("Logging out...");
                    return;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void createTask(User manager) throws SQLException {
        System.out.println("\n========== CREATE TASK ==========");

        System.out.print("Task title: ");
        String title = scanner.nextLine().trim();

        if (title.isBlank()) {
            System.out.println("Title cannot be empty.");
            return;
        }

        System.out.print("Task description: ");
        String description = scanner.nextLine().trim();

        System.out.print("Assign to employee username: ");
        String employeeUsername = scanner.nextLine().trim();

        User employee = userDao.getUserByUsername(employeeUsername);

        if (employee == null
                || !employee.role().equals("EMPLOYEE")
                || !employee.status().equals("APPROVED")) {
            System.out.println("Approved employee account not found.");
            return;
        }

        System.out.print("Deadline (YYYY-MM-DD): ");
        String deadline = scanner.nextLine().trim();

        System.out.println("1. Low");
        System.out.println("2. Medium");
        System.out.println("3. High");
        System.out.print("Choose priority: ");

        String choice = scanner.nextLine().trim();
        String priority;

        switch (choice) {
            case "1" -> priority = "LOW";
            case "2" -> priority = "MEDIUM";
            case "3" -> priority = "HIGH";
            default -> {
                System.out.println("Invalid priority.");
                return;
            }
        }

        if (userDao.createTask(title, description, employee.id(),
                manager.id(), deadline, priority)) {
            userDao.logActivity(manager.id(),
                    "Created task '" + title + "' for " + employee.username());
            System.out.println("Task created and assigned successfully.");
        } else {
            System.out.println("Failed to create task.");
        }
    }

    private void employeeMenu(User user) throws SQLException {
        while (true) {
            System.out.println("\n========== EMPLOYEE MENU ==========");
            System.out.println("1. View My Tasks");
            System.out.println("2. Update Task Status");
            System.out.println("0. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> userDao.showTasksForEmployee(user.id());
                case "2" -> updateTask(user);
                case "0" -> {
                    System.out.println("Logging out...");
                    return;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void updateTask(User user) throws SQLException {
        System.out.print("Enter task ID: ");
        String input = scanner.nextLine().trim();

        int taskId;

        try {
            taskId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Enter a valid numeric task ID.");
            return;
        }

        System.out.println("1. Pending");
        System.out.println("2. In Progress");
        System.out.println("3. Completed");
        System.out.print("Choose new status: ");

        String choice = scanner.nextLine().trim();
        String status;

        switch (choice) {
            case "1" -> status = "PENDING";
            case "2" -> status = "IN_PROGRESS";
            case "3" -> status = "COMPLETED";
            default -> {
                System.out.println("Invalid status.");
                return;
            }
        }

        if (userDao.updateTaskStatus(taskId, user.id(), status)) {
            userDao.logActivity(user.id(),
                    "Updated task ID " + taskId + " to " + status);
            System.out.println("Task status updated successfully.");
        } else {
            System.out.println("Task not found or not assigned to your account.");
        }
    }
}