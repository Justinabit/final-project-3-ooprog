package cinema.model;

import cinema.exception.ValidationException;
import cinema.util.Validator;

// [Member A] Admin account. Admins can log in with their email or their username.
public class Admin extends User {
    private String username;   // example: "Admin123"

    public Admin(String userId, String name, String email, String password, String username) throws ValidationException {
        super(userId, name, email, password);
        setUsername(username);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }

    // Email OR username works for admins (POLYMORPHISM: overrides User.matchesLogin)
    @Override
    public boolean matchesLogin(String emailOrUsername) {
        return super.matchesLogin(emailOrUsername) || username.equalsIgnoreCase(emailOrUsername.trim());
    }

    public String getUsername() { return username; }

    public void setUsername(String username) throws ValidationException {
        Validator.validateUsername(username);
        this.username = username.trim();
    }
}
