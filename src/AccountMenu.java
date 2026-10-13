// [Member A] Menus for register / login / profile / admin customer management.
// This is where Scanner (ConsoleReader) is used; the logic itself goes in UserManager.
public class AccountMenu {
    private UserManager userManager;

    public AccountMenu(UserManager userManager) {
        this.userManager = userManager;
    }

    public void register() {
        System.out.println("\n--- REGISTER ---");
        // TODO (A): ask for name, email, password, age using ConsoleReader
        //           call userManager.registerCustomer(...) inside try/catch
        //           show the error message if ValidationException is caught
    }

    // Returns the logged-in User, or null if login failed
    public User login(boolean adminLogin) {
        System.out.println("\n--- LOGIN ---");
        // TODO (A): ask for email + password, call userManager.login(...)
        //           if adminLogin is true, make sure the user's role is "ADMIN"
        return null;
    }

    public void viewProfile(User user) {
        System.out.println("\n--- MY PROFILE ---");
        // TODO (A): show the user's details, then let them edit name / email / password
    }

    public void manageCustomers() {
        System.out.println("\n--- MANAGE CUSTOMERS (ADMIN) ---");
        // TODO (A): menu -> 1. list all  2. search  3. deactivate  0. back
    }
}
