package cinema.exception;

// Not an error: thrown when the user types /return at any prompt.
// The screen that opened the current one catches it and shows itself again ("go back").
// It is unchecked (a RuntimeException) so every input method can throw it without extra "throws".
public class GoBackException extends RuntimeException {
    public GoBackException() {
        super("The user typed /return");
    }
}
