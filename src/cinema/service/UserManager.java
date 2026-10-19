package cinema.service;

import java.util.ArrayList;

import cinema.exception.LoginException;
import cinema.exception.ValidationException;
import cinema.model.Customer;
import cinema.model.User;

// [Member A] Stores and manages all users (no Scanner / System.out here, just logic)
public class UserManager {
    private ArrayList<User> users = new ArrayList<>();

    public void registerCustomer(String name, String email, String password, int age) throws ValidationException {
        // TODO (A): 1. validate everything with Validator
        //           2. make sure email is not already used (throw ValidationException if it is)
        //           3. create a Customer with a new id and add to the users list
    }

    public User login(String email, String password) throws LoginException {
        // TODO (A): find user by email, check password, check user is active
        //           throw LoginException if anything is wrong
        return null;
    }

    public ArrayList<Customer> searchCustomers(String keyword) {
        // TODO (A): return customers whose name or email contains the keyword
        return new ArrayList<>();
    }

    public void deactivateCustomer(String userId) {
        // TODO (A): find the customer and set active = false
    }

    public ArrayList<User> getAllUsers() {
        return users;
    }

    // TODO (A): add a default admin account somewhere (e.g. in Main.loadSampleData)
}
