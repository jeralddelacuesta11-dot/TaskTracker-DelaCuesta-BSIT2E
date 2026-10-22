package org.example;

import org.example.dao.DatabaseInitializer;
import org.example.dao.UserDao;
import org.example.ui.Authentication;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try {
            DatabaseInitializer.initialize();
            System.out.println("Database initialized successfully.");

            Scanner scanner = new Scanner(System.in);
            UserDao userDao = new UserDao();

            Authentication authentication =
                    new Authentication(scanner, userDao);

            authentication.start();

        } catch (SQLException e) {
            System.out.println(
                    "Database initialization failed: " + e.getMessage()
            );
        }
    }
}