// [Member A] Abstract parent class for Admin and Customer (INHERITANCE + ABSTRACTION)
public abstract class User {
    // private fields = ENCAPSULATION
    private String userId;
    private String name;
    private String email;
    private String password;
    private boolean active = true;   // admin can deactivate a customer

    // The constructor validates too, so an invalid User can never be created.
    public User(String userId, String name, String email, String password) throws ValidationException {
        Validator.validateName(name);
        Validator.validateEmail(email);
        Validator.validatePassword(password);
        this.userId = userId;
        this.name = name.trim();
        this.email = email.trim();
        this.password = password;
    }

    // Each subclass says what role it is (POLYMORPHISM)
    public abstract String getRole();

    public String getUserId() { return userId; }

    public String getName() { return name; }

    public void setName(String name) throws ValidationException {
        Validator.validateName(name);
        this.name = name.trim();
    }

    public String getEmail() { return email; }

    public void setEmail(String email) throws ValidationException {
        Validator.validateEmail(email);
        this.email = email.trim();
    }

    public String getPassword() { return password; }

    public void setPassword(String password) throws ValidationException {
        Validator.validatePassword(password);
        this.password = password;
    }

    public boolean isActive() { return active; }

    public void setActive(boolean active) { this.active = active; }

    public boolean checkPassword(String input) {
        return password.equals(input);
    }
}
