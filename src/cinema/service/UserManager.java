package cinema.service;

import java.util.ArrayList;

import cinema.exception.LoginException;
import cinema.exception.ValidationException;
import cinema.model.Customer;
import cinema.model.User;
import cinema.util.Validator;

// [Member A] Stores and manages all users (no Scanner / System.out here, just logic)
public class UserManager {
    private ArrayList<User> users = new ArrayList<>();
    private int idCounter = 0;

    public String nextUserId() { return "U-" + (++idCounter); }

    public void registerCustomer(String name, String email, String password, int age) throws ValidationException {
        // The Customer constructor runs all the Validator checks
        Customer customer = new Customer(nextUserId(), name, email, password, age);
        addUser(customer);
    }

    // Used for registration and for adding the default admin
    public void addUser(User user) throws ValidationException {
        if (isEmailTaken(user.getEmail(), null)) {
            throw new ValidationException("That email is already registered.");
        }
        users.add(user);
    }

    public boolean isEmailTaken(String email, User ignore) {
        for (User u : users) {
            if (u != ignore && u.getEmail().equalsIgnoreCase(email.trim())) return true;
        }
        return false;
    }

    // Edit email while keeping emails unique
    public void changeEmail(User user, String newEmail) throws ValidationException {
        Validator.validateEmail(newEmail);
        if (isEmailTaken(newEmail, user)) {
            throw new ValidationException("That email is already registered.");
        }
        user.setEmail(newEmail);
    }

    public User login(String email, String password) throws LoginException {
        User found = null;
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email.trim())) {
                found = u;
                break;
            }
        }
        if (found == null || !found.checkPassword(password)) {
            throw new LoginException("Invalid email or password.");
        }
        if (!found.isActive()) {
            throw new LoginException("This account has been deactivated. Please contact an admin.");
        }
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

    public Customer findCustomer(String userId) {
        for (Customer c : getAllCustomers()) {
            if (c.getUserId().equalsIgnoreCase(userId.trim())) return c;
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

    public boolean deactivateCustomer(String userId) {
        return setCustomerActive(userId, false);
    }

    public ArrayList<User> getAllUsers() { return users; }
}
