package cinema.ui;

import java.util.ArrayList;

import cinema.exception.LoginException;
import cinema.exception.ValidationException;
import cinema.model.Customer;
import cinema.model.User;
import cinema.service.UserManager;
import cinema.util.ConsoleReader;
import cinema.util.Validator;

// [Member A] Menus for register / login / profile / admin account management.
// This is where Scanner (ConsoleReader) is used; the logic itself goes in UserManager.
public class AccountMenu {
    private static final int MAX_LOGIN_ATTEMPTS = 3;

    private UserManager userManager;

    public AccountMenu(UserManager userManager) {
        this.userManager = userManager;
    }

    // ---------- register / login ----------

    public void register() {
        Screen.title("REGISTER A CUSTOMER ACCOUNT");
        Screen.info("Leave a field blank to cancel.");
        String name = askName("Full name: ");
        if (name == null) return;
        String email = askNewEmail("Email: ", null);
        if (email == null) return;
        String password = askNewPassword("Password");
        if (password == null) return;
        int age = askAge("Age: ");
        try {
            userManager.registerCustomer(name, email, password, age);
            Screen.success("Registration successful! You can now log in.");
        } catch (ValidationException e) {
            Screen.error("Registration failed: " + e.getMessage());
        }
    }

    // Returns the logged-in User, or null if login failed or was cancelled
    public User login(boolean adminLogin) {
        if (adminLogin) {
            Screen.title("ADMIN LOGIN");
        } else {
            Screen.title("CUSTOMER LOGIN");
        }
        Screen.info("Leave the email blank to go back.");

        for (int attempt = 1; attempt <= MAX_LOGIN_ATTEMPTS; attempt++) {
            String email = ConsoleReader.readLine("Email: ");
            if (email.isEmpty()) return null;
            String password = ConsoleReader.readLine("Password: ");
            try {
                User user = userManager.login(email, password);
                boolean isAdmin = "ADMIN".equals(user.getRole());
                if (adminLogin && !isAdmin) {
                    Screen.error("This is not an admin account. Please use Customer Login.");
                    return null;
                }
                if (!adminLogin && isAdmin) {
                    Screen.error("Admins must use the Admin Login option.");
                    return null;
                }
                Screen.success("Welcome, " + user.getName() + "!");
                return user;
            } catch (LoginException e) {
                Screen.error("Login failed: " + e.getMessage());
                int attemptsLeft = MAX_LOGIN_ATTEMPTS - attempt;
                if (attemptsLeft > 0) {
                    Screen.info("Attempts left: " + attemptsLeft);
                }
            }
        }
        Screen.error("Too many failed attempts. Returning to the main menu.");
        return null;
    }

    // ---------- my profile (customer or admin) ----------

    public void viewProfile(User user) {
        boolean back = false;
        while (!back) {
            Screen.title("MY PROFILE");
            System.out.println("  ID     : " + user.getUserId());
            System.out.println("  Name   : " + user.getName());
            System.out.println("  Email  : " + user.getEmail());
            System.out.println("  Role   : " + user.getRole());
            if (user instanceof Customer) {
                System.out.println("  Age    : " + ((Customer) user).getAge());
            }
            Screen.line();
            Screen.option(1, "Edit name");
            Screen.option(2, "Edit email");
            Screen.option(3, "Change password");
            if (user instanceof Customer) {
                Screen.option(4, "Edit age");
            }
            Screen.option(0, "Back");
            int choice = Screen.readChoice();
            switch (choice) {
                case 1:
                    editName(user);
                    break;
                case 2:
                    editEmail(user);
                    break;
                case 3:
                    changePassword(user);
                    break;
                case 4:
                    if (user instanceof Customer) {
                        editAge((Customer) user);
                    } else {
                        Screen.error("Invalid choice. Try again.");
                    }
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    Screen.error("Invalid choice. Try again.");
            }
        }
    }

    private void editName(User user) {
        String name = askName("New name (blank to cancel): ");
        if (name == null) return;
        try {
            user.setName(name);
            Screen.success("Name updated.");
        } catch (ValidationException e) {
            Screen.error(e.getMessage());
        }
    }

    private void editEmail(User user) {
        String email = askNewEmail("New email (blank to cancel): ", user);
        if (email == null) return;
        try {
            userManager.changeEmail(user, email);
            Screen.success("Email updated.");
        } catch (ValidationException e) {
            Screen.error(e.getMessage());
        }
    }

    private void changePassword(User user) {
        if (!user.checkPassword(ConsoleReader.readLine("Current password: "))) {
            Screen.error("Current password is incorrect.");
            return;
        }
        String password = askNewPassword("New password");
        if (password == null) return;
        try {
            user.setPassword(password);
            Screen.success("Password updated.");
        } catch (ValidationException e) {
            Screen.error(e.getMessage());
        }
    }

    private void editAge(Customer customer) {
        int age = askAge("New age: ");
        try {
            customer.setAge(age);
            Screen.success("Age updated.");
        } catch (ValidationException e) {
            Screen.error(e.getMessage());
        }
    }

    // ---------- admin: manage accounts ----------

    public void manageAccounts() {
        boolean back = false;
        while (!back) {
            Screen.title("MANAGE ACCOUNTS (ADMIN)");
            Screen.option(1, "List all accounts");
            Screen.option(2, "Search customers");
            Screen.option(3, "Deactivate a customer");
            Screen.option(4, "Reactivate a customer");
            Screen.option(5, "Reset a customer's password");
            Screen.option(6, "Create an admin account");
            Screen.option(0, "Back");
            int choice = Screen.readChoice();
            switch (choice) {
                case 1:
                    listAllAccounts();
                    break;
                case 2:
                    searchCustomers();
                    break;
                case 3:
                    changeCustomerStatus(false);
                    break;
                case 4:
                    changeCustomerStatus(true);
                    break;
                case 5:
                    resetCustomerPassword();
                    break;
                case 6:
                    createAdmin();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    Screen.error("Invalid choice. Try again.");
            }
        }
    }

    private void listAllAccounts() {
        Screen.section("ALL ACCOUNTS");
        printTableHeader();
        for (User user : userManager.getAllUsers()) {
            // Admin and Customer each describe themselves (POLYMORPHISM)
            System.out.println("  " + user.getDetails());
        }
        System.out.println("  Total: " + userManager.getAllUsers().size() + " account(s)");
    }

    private void searchCustomers() {
        String keyword = ConsoleReader.readLine("Search name or email: ");
        ArrayList<Customer> found = userManager.searchCustomers(keyword);
        if (found.isEmpty()) {
            Screen.info("No customers found.");
            return;
        }
        Screen.section("SEARCH RESULTS");
        printTableHeader();
        for (Customer customer : found) {
            System.out.println("  " + customer.getDetails());
        }
    }

    private void changeCustomerStatus(boolean active) {
        Customer customer = chooseCustomer();
        if (customer == null) return;
        if (customer.isActive() == active) {
            Screen.info(customer.getName() + " is already " + statusText(active) + ".");
            return;
        }
        userManager.setCustomerActive(customer.getUserId(), active);
        Screen.success(customer.getName() + " is now " + statusText(active) + ".");
    }

    private void resetCustomerPassword() {
        Customer customer = chooseCustomer();
        if (customer == null) return;
        Screen.info("Setting a new password for " + customer.getName() + ".");
        String password = askNewPassword("New password");
        if (password == null) return;
        try {
            customer.setPassword(password);
            Screen.success("Password reset for " + customer.getName() + ".");
        } catch (ValidationException e) {
            Screen.error(e.getMessage());
        }
    }

    private void createAdmin() {
        Screen.section("NEW ADMIN ACCOUNT");
        Screen.info("Leave a field blank to cancel.");
        String name = askName("Full name: ");
        if (name == null) return;
        String email = askNewEmail("Email: ", null);
        if (email == null) return;
        String password = askNewPassword("Password");
        if (password == null) return;
        try {
            userManager.registerAdmin(name, email, password);
            Screen.success("Admin account created for " + name + ".");
        } catch (ValidationException e) {
            Screen.error(e.getMessage());
        }
    }

    private Customer chooseCustomer() {
        String id = ConsoleReader.readLine("Customer ID (e.g. U-2): ");
        Customer customer = userManager.findCustomer(id);
        if (customer == null) {
            Screen.error("No customer with that ID.");
        }
        return customer;
    }

    private void printTableHeader() {
        System.out.println("  " + String.format(User.TABLE_FORMAT, "ID", "NAME", "EMAIL", "ROLE", "STATUS") + "AGE");
        Screen.line();
    }

    private String statusText(boolean active) {
        if (active) {
            return "active";
        }
        return "deactivated";
    }

    // ---------- input helpers: keep asking until the value is valid ----------

    // Returns null if the user leaves it blank (cancel)
    private String askName(String prompt) {
        while (true) {
            String name = ConsoleReader.readLine(prompt);
            if (name.isEmpty()) {
                Screen.info("Cancelled.");
                return null;
            }
            try {
                Validator.validateName(name);
                return name;
            } catch (ValidationException e) {
                Screen.error(e.getMessage());
            }
        }
    }

    // owner = the account being edited (so its own email is allowed), or null for a new account
    private String askNewEmail(String prompt, User owner) {
        while (true) {
            String email = ConsoleReader.readLine(prompt);
            if (email.isEmpty()) {
                Screen.info("Cancelled.");
                return null;
            }
            try {
                userManager.checkNewEmail(email, owner);
                return email;
            } catch (ValidationException e) {
                Screen.error(e.getMessage());
            }
        }
    }

    // Asks twice so a typo can't lock the user out. Returns null if left blank.
    private String askNewPassword(String label) {
        Screen.info("Password rules: 8+ characters, at least one number and one uppercase letter.");
        while (true) {
            String password = ConsoleReader.readLine(label + ": ");
            if (password.isEmpty()) {
                Screen.info("Cancelled.");
                return null;
            }
            try {
                Validator.validatePassword(password);
                String confirm = ConsoleReader.readLine("Confirm " + label.toLowerCase() + ": ");
                if (confirm.equals(password)) {
                    return password;
                }
                Screen.error("Passwords do not match. Try again.");
            } catch (ValidationException e) {
                Screen.error(e.getMessage());
            }
        }
    }

    private int askAge(String prompt) {
        while (true) {
            int age = ConsoleReader.readInt(prompt);
            try {
                Validator.validateAge(age);
                return age;
            } catch (ValidationException e) {
                Screen.error(e.getMessage());
            }
        }
    }
}
