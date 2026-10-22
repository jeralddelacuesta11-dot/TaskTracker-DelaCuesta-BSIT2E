package org.example.model;

public record User(
        int id,
        String username,
        String password,
        String role,
        String status
) {
}