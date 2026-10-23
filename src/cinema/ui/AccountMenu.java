package cinema.ui;

import java.time.LocalDate;
import java.util.ArrayList;

import cinema.exception.LoginException;
import cinema.exception.GoBackException;
import cinema.exception.ValidationException;
import cinema.model.Admin;
import cinema.model.Customer;
import cinema.model.User;
import cinema.service.UserManager;
import cinema.util.ConsoleReader;
import cinema.util.Format;
import cinema.util.Validator;

// [Member A] Menus for register / login / profile / admin account management.
// This is where Scanner (ConsoleReader) is used; the logic itself goes in UserManager.
public class AccountMenu {
    private static final int MAX_LOGIN_ATTEMPTS = 3;   // per visit to the login screen

    private UserManager userManager;

    public AccountMenu(UserManager userManager) {
        this.userManager = userManager;
    }

    // ---------- register / login ----------

    public void register() {
        Screen.title("REGISTER A CUSTOMER ACCOUNT");
        Screen.info("Leave a field blank to cancel. You must be at least " + Validator.MIN_ACCOUNT_AGE + " years old.");
        String name = askName("Full name: ");
        if (name == null) return;
        String email = askNewEmail("Email: ", null);
        if (email == null) return;
        String password = askNewPassword("Password");
        if (password == null) return;
        LocalDate birthDate = askBirthDate("Birthdate (yyyy-MM-dd, e.g. 2004-08-15): ");
        if (birthDate == null) return;
        try {
            userManager.registerCustomer(name, email, password, birthDate);
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
        Screen.info("Leave it blank to go back.");

        for (int attempt = 1; attempt <= MAX_LOGIN_ATTEMPTS; attempt++) {
            String emailOrUsername;
            if (adminLogin) {
                emailOrUsername = ConsoleReader.readLine("Username or email: ");
            } else {
                emailOrUsername = ConsoleReader.readLine("Email: ");
            }
            if (emailOrUsername.isEmpty()) return null;
            String password = ConsoleReader.readLine("Password: ");
            try {
                User user = userManager.login(emailOrUsername, password);
                boolean isAdmin = user instanceof Admin;
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
                if (e.getMessage().contains("locked")) {
                    return null;   // no point trying again right now
                }
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
            System.out.println("  ID           : " + user.getUserId());
            System.out.println("  Name         : " + user.getName());
            System.out.println("  Email        : " + user.getEmail());
            System.out.println("  Role         : " + user.getRole());
            if (user instanceof Admin) {
                System.out.println("  Username     : " + ((Admin) user).getUsername());
            }
            if (user instanceof Customer) {
                Customer c = (Customer) user;
                System.out.println("  Birthdate    : " + Format.date(c.getBirthDate()) + " (age " + c.getAge() + ")");
            }
            System.out.println("  Member since : " + Format.date(user.getRegisteredAt().toLocalDate()));
            Screen.line();
            Screen.option(1, "Edit name");
            Screen.option(2, "Edit email");
            Screen.option(3, "Change password");
            Screen.option(0, "Back");
            if (user instanceof Customer) {
                Screen.info("To correct your birthdate, please ask an admin (bring a valid ID).");
            }
            int choice = Screen.readMenuChoice(3);
            try {
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
                    case 0:
                        back = true;
                        break;
                    default:
                        Screen.error("Invalid choice. Try again.");
                }
            } catch (GoBackException e) {
                Screen.backTo("My Profile");
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

    // ---------- admin: manage accounts ----------

    public void manageAccounts() {
        boolean back = false;
        while (!back) {
            Screen.title("MANAGE ACCOUNTS (ADMIN)");
            Screen.option(1, "List all accounts");
            Screen.option(2, "Search customers");
            Screen.option(3, "Deactivate a customer");
            Screen.option(4, "Reactivate a customer");
            Screen.option(5, "Unlock an account (too many wrong passwords)");
            Screen.option(6, "Reset a customer's password");
            Screen.option(7, "Correct a customer's birthdate (check their ID)");
            Screen.option(8, "Create an admin account");
            Screen.option(0, "Back");
            int choice = Screen.readMenuChoice(8);
            try {
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
                        unlockAccount();
                        break;
                    case 6:
                        resetCustomerPassword();
                        break;
                    case 7:
                        correctBirthDate();
                        break;
                    case 8:
                        createAdmin();
                        break;
                    case 0:
                        back = true;
                        break;
                    default:
                        Screen.error("Invalid choice. Try again.");
                }
            } catch (GoBackException e) {
                Screen.backTo("Manage Accounts");
            }
        }
    }

    private void listAllAccounts() {
        Screen.section("ALL ACCOUNTS");
        printAccounts(userManager.getAllUsers());
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
        printAccounts(new ArrayList<User>(found));
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

    private void unlockAccount() {
        User user = userManager.findUser(ConsoleReader.readLine("Account ID (e.g. U-2): "));
        if (user == null) {
            Screen.error("No account with that ID.");
            return;
        }
        if (!user.isLocked()) {
            Screen.info(user.getName() + "'s account is not locked.");
            return;
        }
        user.unlock();
        Screen.success(user.getName() + "'s account is unlocked.");
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

    private void correctBirthDate() {
        Customer customer = chooseCustomer();
        if (customer == null) return;
        Screen.info(customer.getName() + "'s birthdate is " + Format.date(customer.getBirthDate()) + ".");
        LocalDate birthDate = askBirthDate("Correct birthdate as shown on their ID (yyyy-MM-dd): ");
        if (birthDate == null) return;
        try {
            customer.setBirthDate(birthDate);
            Screen.success("Birthdate updated. " + customer.getName() + " is " + customer.getAge() + " years old.");
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
        String username = askNewUsername();
        if (username == null) return;
        String password = askNewPassword("Password");
        if (password == null) return;
        try {
            userManager.registerAdmin(name, email, username, password);
            Screen.success("Admin account created for " + Validator.cleanSpaces(name) + " (username " + username + ").");
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

    // The accounts table, also used by RecordsMenu. A name or email that is too long ends with "...".
    public static void printAccounts(ArrayList<User> users) {
        Table table = new Table(6, 16, 19, 8, 11, 3);
        table.printHeader("ID", "NAME", "EMAIL", "ROLE", "STATUS", "AGE");
        for (User user : users) {
            // Admin and Customer each give their own row (POLYMORPHISM)
            table.printRow(user.getTableRow());
        }
    }

    private String statusText(boolean active) {
        if (active) {
            return "active";
        }
        return "deactivated";
    }

    // ---------- input helpers: keep asking until the value is valid (blank = cancel) ----------

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

    // Admin usernames must be unique
    private String askNewUsername() {
        while (true) {
            String username = ConsoleReader.readLine("Username (4-20 letters or digits): ");
            if (username.isEmpty()) {
                Screen.info("Cancelled.");
                return null;
            }
            try {
                Validator.validateUsername(username);
                if (userManager.isUsernameTaken(username)) {
                    throw new ValidationException("That username is already taken.");
                }
                return username;
            } catch (ValidationException e) {
                Screen.error(e.getMessage());
            }
        }
    }

    // Asks twice so a typo can't lock the user out
    private String askNewPassword(String label) {
        Screen.info("Password rules: " + Validator.MIN_PASSWORD_LENGTH + "-" + Validator.MAX_PASSWORD_LENGTH
                + " characters with an uppercase letter, a lowercase letter and a number.");
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

    private LocalDate askBirthDate(String prompt) {
        while (true) {
            String text = ConsoleReader.readLine(prompt);
            if (text.isEmpty()) {
                Screen.info("Cancelled.");
                return null;
            }
            try {
                return Validator.validateBirthDate(text);
            } catch (ValidationException e) {
                Screen.error(e.getMessage());
            }
        }
    }
}
