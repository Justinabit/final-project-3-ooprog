package cinema.ui;

import java.util.ArrayList;

import cinema.exception.LoginException;
import cinema.exception.ValidationException;
import cinema.model.Admin;
import cinema.model.Customer;
import cinema.model.User;
import cinema.service.UserManager;
import cinema.util.ConsoleReader;

// [Member A] Menus for register / login / profile / admin customer management.
// This is where Scanner (ConsoleReader) is used; the logic itself goes in UserManager.
public class AccountMenu {
    private UserManager userManager;

    public AccountMenu(UserManager userManager) {
        this.userManager = userManager;
    }

    public void register() {
        System.out.println("\n--- REGISTER ---");
        String name = ConsoleReader.readLine("Full name: ");
        String email = ConsoleReader.readLine("Email: ");
        String password = ConsoleReader.readLine("Password (8+ chars, a number, an uppercase letter): ");
        int age = ConsoleReader.readInt("Age: ");
        try {
            userManager.registerCustomer(name, email, password, age);
            System.out.println("Registration successful! You can now log in.");
        } catch (ValidationException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    // Returns the logged-in User, or null if login failed
    public User login(boolean adminLogin) {
        System.out.println(adminLogin ? "\n--- ADMIN LOGIN ---" : "\n--- LOGIN ---");
        String email = ConsoleReader.readLine("Email: ");
        String password = ConsoleReader.readLine("Password: ");
        try {
            User user = userManager.login(email, password);
            boolean isAdmin = "ADMIN".equals(user.getRole());
            if (adminLogin && !isAdmin) {
                System.out.println("This is not an admin account.");
                return null;
            }
            if (!adminLogin && isAdmin) {
                System.out.println("Admins must use the Admin Login option.");
                return null;
            }
            System.out.println("Welcome, " + user.getName() + "!");
            return user;
        } catch (LoginException e) {
            System.out.println("Login failed: " + e.getMessage());
            return null;
        }
    }

    public void viewProfile(User user) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- MY PROFILE ---");
            System.out.println("ID:    " + user.getUserId());
            System.out.println("Name:  " + user.getName());
            System.out.println("Email: " + user.getEmail());
            if (user instanceof Customer) System.out.println("Age:   " + ((Customer) user).getAge());
            System.out.println("Role:  " + user.getRole());
            System.out.println("\n1. Edit name");
            System.out.println("2. Edit email");
            System.out.println("3. Change password");
            if (user instanceof Customer) System.out.println("4. Edit age");
            System.out.println("0. Back");
            int choice = ConsoleReader.readInt("Choose: ");
            try {
                switch (choice) {
                    case 1:
                        user.setName(ConsoleReader.readLine("New name: "));
                        System.out.println("Name updated.");
                        break;
                    case 2:
                        userManager.changeEmail(user, ConsoleReader.readLine("New email: "));
                        System.out.println("Email updated.");
                        break;
                    case 3:
                        if (!user.checkPassword(ConsoleReader.readLine("Current password: "))) {
                            System.out.println("Current password is incorrect.");
                            break;
                        }
                        user.setPassword(ConsoleReader.readLine("New password: "));
                        System.out.println("Password updated.");
                        break;
                    case 4:
                        if (user instanceof Customer) {
                            ((Customer) user).setAge(ConsoleReader.readInt("New age: "));
                            System.out.println("Age updated.");
                        } else {
                            System.out.println("Invalid choice. Try again.");
                        }
                        break;
                    case 0:
                        back = true;
                        break;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            } catch (ValidationException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    public void manageCustomers() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- MANAGE CUSTOMERS (ADMIN) ---");
            System.out.println("1. List all customers");
            System.out.println("2. Search customers");
            System.out.println("3. Deactivate a customer");
            System.out.println("4. Reactivate a customer");
            System.out.println("0. Back");
            int choice = ConsoleReader.readInt("Choose: ");
            switch (choice) {
                case 1:
                    printCustomers(userManager.getAllCustomers());
                    break;
                case 2:
                    printCustomers(userManager.searchCustomers(ConsoleReader.readLine("Search name or email: ")));
                    break;
                case 3:
                    setActive(false);
                    break;
                case 4:
                    setActive(true);
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void setActive(boolean active) {
        String id = ConsoleReader.readLine("Customer ID: ");
        if (userManager.setCustomerActive(id, active)) {
            System.out.println("Customer " + (active ? "reactivated." : "deactivated."));
        } else {
            System.out.println("No customer with that ID.");
        }
    }

    private void printCustomers(ArrayList<Customer> customers) {
        if (customers.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }
        for (Customer c : customers) {
            System.out.println(c.getUserId() + " | " + c.getName() + " | " + c.getEmail()
                    + " | age " + c.getAge() + " | " + (c.isActive() ? "ACTIVE" : "DEACTIVATED"));
        }
    }
}
