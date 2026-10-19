// [Member A] Customer account
public class Customer extends User {
    private int age;   // needed for age ratings and age-based discounts

    public Customer(String userId, String name, String email, String password, int age) throws ValidationException {
        super(userId, name, email, password);
        setAge(age);
    }

    @Override
    public String getRole() {
        return "CUSTOMER";
    }

    public int getAge() { return age; }

    public void setAge(int age) throws ValidationException {
        Validator.validateAge(age);
        this.age = age;
    }

    public String getCustomerId() { return getUserId(); }
}
