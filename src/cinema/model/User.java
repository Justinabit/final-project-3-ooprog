package cinema.model;

import java.time.LocalDateTime;

import cinema.exception.ValidationException;
import cinema.util.Validator;

// [Member A] Abstract parent class for Admin and Customer (INHERITANCE + ABSTRACTION)
public abstract class User {
    // Column layout for the accounts table (ID, NAME, EMAIL, ROLE, STATUS)
    public static final String TABLE_FORMAT = "%-5s %-18s %-22s %-9s %-12s";
    public static final int MAX_FAILED_LOGINS = 5;   // wrong passwords in a row before the account locks
    public static final int LOCK_MINUTES = 15;

    // private fields = ENCAPSULATION
    private String userId;
    private String name;
    private String email;
    private String password;
    private boolean active = true;                       // admin can deactivate a customer
    private LocalDateTime registeredAt = LocalDateTime.now();
    private int failedLogins = 0;
    private LocalDateTime lockedUntil = null;            // null = not locked

    // The constructor validates too, so an invalid User can never be created.
    public User(String userId, String name, String email, String password) throws ValidationException {
        Validator.validateName(name);
        Validator.validateEmail(email);
        Validator.validatePassword(password);
        this.userId = userId;
        this.name = Validator.cleanSpaces(name);
        this.email = email.trim().toLowerCase();
        this.password = password;
    }

    // Each subclass says what role it is (POLYMORPHISM)
    public abstract String getRole();

    public String getUserId() { return userId; }

    public String getName() { return name; }

    public void setName(String name) throws ValidationException {
        Validator.validateName(name);
        this.name = Validator.cleanSpaces(name);
    }

    public String getEmail() { return email; }

    public void setEmail(String email) throws ValidationException {
        Validator.validateEmail(email);
        this.email = email.trim().toLowerCase();
    }

    public String getPassword() { return password; }

    public void setPassword(String password) throws ValidationException {
        Validator.validatePassword(password);
        this.password = password;
    }

    public boolean checkPassword(String input) {
        return password.equals(input);
    }

    // Customers log in with their email. Admin overrides this to also accept a username (POLYMORPHISM).
    public boolean matchesLogin(String emailOrUsername) {
        return email.equalsIgnoreCase(emailOrUsername.trim());
    }

    public boolean isActive() { return active; }

    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getRegisteredAt() { return registeredAt; }

    // ---------- login lock: too many wrong passwords lock the account for a while ----------

    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    public LocalDateTime getLockedUntil() { return lockedUntil; }

    public int getFailedLogins() { return failedLogins; }

    // Counts one wrong password. Returns true if this attempt locked the account.
    public boolean recordFailedLogin() {
        failedLogins++;
        if (failedLogins >= MAX_FAILED_LOGINS) {
            lockedUntil = LocalDateTime.now().plusMinutes(LOCK_MINUTES);
            failedLogins = 0;
            return true;
        }
        return false;
    }

    // A correct password (or an admin) clears the wrong-password count and any lock
    public void unlock() {
        failedLogins = 0;
        lockedUntil = null;
    }

    // Used only when loading a saved account from the data file
    public void loadSavedState(boolean active, LocalDateTime registeredAt, int failedLogins, LocalDateTime lockedUntil) {
        this.active = active;
        this.registeredAt = registeredAt;
        this.failedLogins = failedLogins;
        this.lockedUntil = lockedUntil;
    }

    public String getStatus() {
        if (!active) return "DEACTIVATED";
        if (isLocked()) return "LOCKED";
        return "ACTIVE";
    }

    // One row of the accounts table. Customer overrides this to add the age (POLYMORPHISM).
    public String getDetails() {
        return String.format(TABLE_FORMAT, userId, name, email, getRole(), getStatus());
    }
}
