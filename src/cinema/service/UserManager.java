package cinema.service;

import java.time.LocalDate;
import java.util.ArrayList;

import cinema.exception.LoginException;
import cinema.exception.ValidationException;
import cinema.model.Admin;
import cinema.model.Customer;
import cinema.model.User;
import cinema.util.Format;
import cinema.util.Validator;

// [Member A] Stores and manages all users (no Scanner / System.out here, just logic)
public class UserManager {
    private ArrayList<User> users = new ArrayList<>();
    private int idCounter = 0;   // the number of the last ID given out (U-1, U-2, ...)

    // The next ID is only used up when the account is really added (so a failed sign-up leaves no gap)
    private String peekNextUserId() { return "U-" + (idCounter + 1); }

    public Customer registerCustomer(String name, String email, String password, LocalDate birthDate)
            throws ValidationException {
        Customer customer = new Customer(peekNextUserId(), name, email, password, birthDate);   // validates
        addUser(customer);
        return customer;
    }

    public Admin registerAdmin(String name, String email, String username, String password) throws ValidationException {
        Admin admin = new Admin(peekNextUserId(), name, email, password, username);   // validates
        addUser(admin);
        return admin;
    }

    // Used for new accounts and for accounts loaded from the data file
    public void addUser(User user) throws ValidationException {
        if (isEmailTaken(user.getEmail(), null)) {
            throw new ValidationException("That email is already registered.");
        }
        if (user instanceof Admin && isUsernameTaken(((Admin) user).getUsername())) {
            throw new ValidationException("That username is already taken.");
        }
        int number = Integer.parseInt(user.getUserId().substring(2));   // "U-12" -> 12
        users.add(user);
        if (number > idCounter) {
            idCounter = number;
        }
    }

    public boolean isEmailTaken(String email, User ignore) {
        for (User u : users) {
            if (u != ignore && u.getEmail().equalsIgnoreCase(email.trim())) return true;
        }
        return false;
    }

    public boolean isUsernameTaken(String username) {
        for (User u : users) {
            if (u instanceof Admin && ((Admin) u).getUsername().equalsIgnoreCase(username.trim())) return true;
        }
        return false;
    }

    // Checks the email format and that no other account uses it.
    // owner = the account being edited (its own email is allowed), or null for a new account.
    public void checkNewEmail(String email, User owner) throws ValidationException {
        Validator.validateEmail(email);
        if (isEmailTaken(email, owner)) {
            throw new ValidationException("That email is already registered.");
        }
    }

    // Edit email while keeping emails unique
    public void changeEmail(User user, String newEmail) throws ValidationException {
        checkNewEmail(newEmail, user);
        user.setEmail(newEmail);
    }

    // Customers use their email; admins can also use their username (see Admin.matchesLogin).
    // After User.MAX_FAILED_LOGINS wrong passwords in a row the account is locked for a while.
    public User login(String emailOrUsername, String password) throws LoginException {
        User found = null;
        for (User u : users) {
            if (u.matchesLogin(emailOrUsername)) {
                found = u;
                break;
            }
        }
        if (found == null) {
            throw new LoginException("Invalid email or password.");
        }
        if (found.isLocked()) {
            throw new LoginException("Too many wrong passwords. This account is locked until "
                    + Format.shortDateTime(found.getLockedUntil()) + ".");
        }
        if (!found.checkPassword(password)) {
            if (found.recordFailedLogin()) {
                throw new LoginException("Too many wrong passwords. This account is now locked for "
                        + User.LOCK_MINUTES + " minutes.");
            }
            throw new LoginException("Invalid email or password.");
        }
        if (!found.isActive()) {
            throw new LoginException("This account has been deactivated. Please contact an admin.");
        }
        found.unlock();   // a correct password clears the wrong-password count
        return found;
    }

    public ArrayList<Customer> getAllCustomers() {
        ArrayList<Customer> result = new ArrayList<>();
        for (User u : users) {
            if (u instanceof Customer) result.add((Customer) u);
        }
        return result;
    }

    public ArrayList<Customer> searchCustomers(String keyword) {
        ArrayList<Customer> result = new ArrayList<>();
        String k = keyword.trim().toLowerCase();
        for (Customer c : getAllCustomers()) {
            if (c.getName().toLowerCase().contains(k) || c.getEmail().toLowerCase().contains(k)) {
                result.add(c);
            }
        }
        return result;
    }

    public User findUser(String userId) {
        for (User u : users) {
            if (u.getUserId().equalsIgnoreCase(userId.trim())) return u;
        }
        return null;
    }

    public Customer findCustomer(String userId) {
        User user = findUser(userId);
        if (user instanceof Customer) {
            return (Customer) user;
        }
        return null;
    }

    // Returns false if no such customer
    public boolean setCustomerActive(String userId, boolean active) {
        Customer c = findCustomer(userId);
        if (c == null) return false;
        c.setActive(active);
        return true;
    }

    public ArrayList<User> getAllUsers() { return users; }
}
