// [Member A] Abstract parent class for Admin and Customer (INHERITANCE + ABSTRACTION)
public abstract class User {
    // private fields = ENCAPSULATION
    private String userId;
    private String name;
    private String email;
    private String password;
    private boolean active = true;   // admin can deactivate a customer

    public User(String userId, String name, String email, String password) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // Each subclass says what role it is (POLYMORPHISM)
    public abstract String getRole();

    // TODO (A): add getters and setters for every field above
    // TODO (A): setters should call Validator before changing anything
    // TODO (A): add checkPassword(String input) that returns true if it matches
}
