// [Member A] Customer account
public class Customer extends User {
    private int age;   // needed for age-based discounts (Member C)

    public Customer(String userId, String name, String email, String password, int age) {
        super(userId, name, email, password);
        this.age = age;
    }

    @Override
    public String getRole() {
        return "CUSTOMER";
    }

    // TODO (A): add getAge() and setAge() (validate age is realistic, e.g. 1-120)
    // NOTE: other members call getCustomerId(), getName(), getEmail() -> make sure these exist!
    //       (you can add getCustomerId() that returns getUserId())
}
