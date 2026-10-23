package cinema.model;

import java.time.LocalDate;
import java.time.Period;

import cinema.exception.ValidationException;
import cinema.util.Validator;

// [Member A] Customer account. The birthdate is saved (not the age), so the age is always correct.
public class Customer extends User {
    private LocalDate birthDate;

    public Customer(String userId, String name, String email, String password, LocalDate birthDate)
            throws ValidationException {
        super(userId, name, email, password);
        setBirthDate(birthDate);
    }

    @Override
    public String getRole() {
        return "CUSTOMER";
    }

    public LocalDate getBirthDate() { return birthDate; }

    // Needed for age ratings and the senior discount. It goes up by itself every birthday.
    public int getAge() {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    // Only an admin can correct a birthdate (customers can't change their own age)
    public void setBirthDate(LocalDate birthDate) throws ValidationException {
        Validator.validateBirthDate(birthDate);
        this.birthDate = birthDate;
    }

    // Same row as every User, plus the age at the end (POLYMORPHISM: overrides User.getDetails)
    @Override
    public String getDetails() {
        return super.getDetails() + getAge();
    }
}
