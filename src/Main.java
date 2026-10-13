// Entry point. Shows the menus and sends each choice to the right class.
// Tags in comments tell you who owns what:  [A] accounts/venues  [B] movies  [C] booking  [D] payments/reports
public class Main {

    // Shared managers and menus (created once, passed around)
    static UserManager userManager = new UserManager();         // A
    static VenueManager venueManager = new VenueManager();      // A
    static MovieManager movieManager = new MovieManager();      // B
    static AccountMenu accountMenu = new AccountMenu(userManager);                 // A
    static VenueMenu venueMenu = new VenueMenu(venueManager);                      // A
    static MovieMenu movieMenu = new MovieMenu(movieManager, venueManager);       // B
    static BookingMenu bookingMenu = new BookingMenu(movieManager);               // C
    static ReportService reportService = new ReportService();                      // D

    public static void main(String[] args) {
        loadSampleData();
        mainMenu();
        System.out.println("Thank you for using the Cinema System. Goodbye!");
    }

    // [D] Fill the system with starter data so we can test without typing everything.
    static void loadSampleData() {
        // TODO (D): create a default admin account  (use Admin class from A)
        // TODO (D): create 1-2 branches with rooms and seats (A's classes)
        // TODO (D): create a few movies and showtimes (B's classes)
    }

    // ---------- MAIN MENU (not logged in) ----------
    static void mainMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n===== CINEMA BOOKING SYSTEM =====");
            System.out.println("1. Register");
            System.out.println("2. Customer Login");
            System.out.println("3. Admin Login");
            System.out.println("0. Exit");
            int choice = ConsoleReader.readInt("Choose: ");

            switch (choice) {
                case 1:
                    accountMenu.register();                          // [A]
                    break;
                case 2:
                    User customer = accountMenu.login(false);        // [A]
                    if (customer instanceof Customer) {
                        customerMenu((Customer) customer);
                    }
                    break;
                case 3:
                    User admin = accountMenu.login(true);            // [A]
                    if (admin instanceof Admin) {
                        adminMenu((Admin) admin);
                    }
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // ---------- CUSTOMER MENU ----------
    static void customerMenu(Customer customer) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n===== CUSTOMER MENU =====");
            System.out.println("1. Browse Movies");
            System.out.println("2. Book a Ticket");
            System.out.println("3. My Bookings");
            System.out.println("4. My Profile");
            System.out.println("0. Logout");
            int choice = ConsoleReader.readInt("Choose: ");

            switch (choice) {
                case 1:
                    movieMenu.browseMovies();                        // [B]
                    break;
                case 2:
                    bookingMenu.bookTicket(customer);                // [C] (uses D's tickets + payments)
                    break;
                case 3:
                    bookingMenu.viewMyBookings(customer);            // [C]
                    break;
                case 4:
                    accountMenu.viewProfile(customer);               // [A]
                    break;
                case 0:
                    loggedIn = false;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // ---------- ADMIN MENU ----------
    static void adminMenu(Admin admin) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n===== ADMIN MENU =====");
            System.out.println("1. Manage Customers");
            System.out.println("2. Manage Venues (branches/rooms/seats)");
            System.out.println("3. Manage Movies");
            System.out.println("4. Manage Showtimes");
            System.out.println("5. Manage Promo Codes");
            System.out.println("6. Revenue Report");
            System.out.println("7. Attendance Report");
            System.out.println("0. Logout");
            int choice = ConsoleReader.readInt("Choose: ");

            switch (choice) {
                case 1:
                    accountMenu.manageCustomers();                   // [A]
                    break;
                case 2:
                    venueMenu.show();                                // [A]
                    break;
                case 3:
                    movieMenu.manageMovies();                        // [B]
                    break;
                case 4:
                    movieMenu.manageShowtimes();                     // [B]
                    break;
                case 5:
                    bookingMenu.managePromos();                      // [C]
                    break;
                case 6:
                    reportService.showRevenueReport();               // [D]
                    break;
                case 7:
                    reportService.showAttendanceReport();            // [D]
                    break;
                case 0:
                    loggedIn = false;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
}
