package com.ChiChiFOOD.model;

import org.hibernate.Session;


import java.util.Scanner;

public class MenuService {
    private final Scanner scanner = new Scanner(System.in);
    private final AuthService authService;

    public MenuService(Session session) {
        this.authService = new AuthService(session);
    }
    public boolean start() {
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Exit");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1 -> registerFlow();
            case 2 -> loginFlow();
            case 3 -> {
                System.out.println("Exiting...");
                return false;  // کاربر می‌خواهد خارج شود
            }
            default -> System.out.println("Invalid option.");
        }
        return true;  // ادامه برنامه
    }

    private void registerFlow() {
        System.out.println("=== Register ===");

        System.out.print("Name: ");
        String name = scanner.nextLine();

        System.out.print("Phone Number: ");
        String phone = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        System.out.println("Select Role: ");
        System.out.println("1. Buyer");
        System.out.println("2. Seller");
        System.out.println("3. Courier");

        int roleChoice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        Role role;
        switch (roleChoice) {
            case 1 -> role = Role.BUYER;
            case 2 -> role = Role.SELLER;
            case 3 -> role = Role.COURIER;
            default -> {
                System.out.println("Invalid role selected.");
                return;
            }
        }

        System.out.print("Address: ");
        String address = scanner.nextLine();

        boolean success = authService.registerUser(name, phone, email, password, role, address);
        if (success) {
            System.out.println("Registration successful.");
        } else {
            System.out.println("Registration failed.");
        }    }

    private void loginFlow() {
        System.out.println("=== Login ===");

        System.out.print("Phone Number: ");
        String phone = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        var user = authService.loginUser(phone, password);
        if (user != null) {
            System.out.println("Login successful. Welcome, " + user.getName() + "!");
        } else {
            System.out.println("Login failed. Invalid phone or password.");
        }    }
}