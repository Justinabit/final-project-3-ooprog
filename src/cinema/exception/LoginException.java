package cinema.exception;

// Thrown when login fails (wrong password, account deactivated, user not found)
public class LoginException extends CinemaException {
    public LoginException(String message) { super(message); }
}
