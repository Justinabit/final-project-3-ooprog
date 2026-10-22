package cinema.model;

import cinema.exception.ValidationException;

// [Member A] Admin account
public class Admin extends User {
    public Admin(String userId, String name, String email, String password) throws ValidationException {
        super(userId, name, email, password);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }
}
