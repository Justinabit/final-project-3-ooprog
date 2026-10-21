package cinema;

import cinema.exception.CinemaException;
import cinema.model.Admin;
import cinema.model.CinemaBranch;
import cinema.model.CinemaRoom;
import cinema.model.Customer;
import cinema.model.Movie;
import cinema.model.Promo;
import cinema.model.Showtime;
import cinema.model.Ticket;
import cinema.model.User;
import cinema.service.MovieManager;
import cinema.service.PromoManager;
import cinema.service.ReportService;
import cinema.service.UserManager;
import cinema.service.VenueManager;
import cinema.ui.AccountMenu;
import cinema.ui.BookingMenu;
import cinema.ui.MovieMenu;
import cinema.ui.Screen;
import cinema.ui.VenueMenu;

// Entry point. Shows the menus and sends each choice to the right class.
// Tags in comments tell you who owns what:  [A] accounts/venues  [B] movies  [C] booking  [D] payments/reports
public class Main {

    // Shared managers and menus (created once, passed around)
    static UserManager userManager = new UserManager();         // A
    static VenueManager venueManager = new VenueManager();      // A
    static MovieManager movieManager = new MovieManager();      // B
    static AccountMenu accountMenu = new AccountMenu(userManager);                 // A
    static VenueMenu venueMenu = new VenueMenu(venueManager, movieManager);        // A
    static MovieMenu movieMenu = new MovieMenu(movieManager, venueManager);       // B
    static PromoManager promoManager = new PromoManager();                         // C
    static BookingMenu bookingMenu = new BookingMenu(movieManager, venueManager, promoManager); // C
    static ReportService reportService = new ReportService();                      // D

    public static void main(String[] args) {
        loadSampleData();
        Screen.showWelcome();
        mainMenu();
        Screen.showGoodbye();
    }

    // [D] Fill the system with starter data so we can test without typing everything.
    // Showtimes are created relative to today so they are always in the future.
    static void loadSampleData() {
        try {
            // Default admin and one sample customer
            userManager.addUser(new Admin(userManager.nextUserId(), "System Admin", "admin@cinema.com", "Admin123"));
            userManager.registerCustomer("Juan Dela Cruz", "juan@mail.com", "Customer1", 21);

            // Branches, rooms and seats (last row of each room is VIP)
            CinemaBranch sm = new CinemaBranch(venueManager.nextBranchId(), "SM Mall Branch", "SM City, Main Street");
            CinemaBranch ayala = new CinemaBranch(venueManager.nextBranchId(), "Ayala Branch", "Ayala Center, Business District");
            venueManager.addBranch(sm);
            venueManager.addBranch(ayala);
            CinemaRoom smRoom1 = venueManager.addRoomToBranch(sm.getBranchId(), "Cinema 1", 5, 8);
            CinemaRoom smRoom2 = venueManager.addRoomToBranch(sm.getBranchId(), "Cinema 2", 4, 6);
            CinemaRoom ayalaRoom1 = venueManager.addRoomToBranch(ayala.getBranchId(), "Cinema 1", 6, 10);

            // Movies
            Movie m1 = new Movie(movieManager.nextMovieId(), "Space Odyssey", "Sci-Fi", "PG", 130, "NOW SHOWING");
            Movie m2 = new Movie(movieManager.nextMovieId(), "Midnight Terror", "Horror", "R-16", 105, "NOW SHOWING");
            Movie m3 = new Movie(movieManager.nextMovieId(), "Laugh Out Loud", "Comedy", "G", 95, "NOW SHOWING");
            Movie m4 = new Movie(movieManager.nextMovieId(), "Dark Streets", "Crime", "R-18", 120, "NOW SHOWING");
            Movie m5 = new Movie(movieManager.nextMovieId(), "Summer Dreams", "Romance", "PG", 110, "COMING SOON");
            for (Movie m : new Movie[] {m1, m2, m3, m4, m5}) movieManager.addMovie(m);

            // Showtimes (tomorrow and the day after)
            String d1 = java.time.LocalDate.now().plusDays(1).toString();
            String d2 = java.time.LocalDate.now().plusDays(2).toString();
            movieManager.addShowtime(new Showtime(movieManager.nextShowtimeId(), m1, smRoom1, d1 + " 14:00", 250));
            movieManager.addShowtime(new Showtime(movieManager.nextShowtimeId(), m2, smRoom1, d1 + " 19:00", 280));
            movieManager.addShowtime(new Showtime(movieManager.nextShowtimeId(), m3, smRoom2, d1 + " 16:00", 200));
            movieManager.addShowtime(new Showtime(movieManager.nextShowtimeId(), m4, ayalaRoom1, d2 + " 20:00", 300));
            movieManager.addShowtime(new Showtime(movieManager.nextShowtimeId(), m1, ayalaRoom1, d2 + " 15:00", 270));

            promoManager.addPromo("WELCOME10", 10);
        } catch (CinemaException e) {
            Screen.error("Could not load sample data: " + e.getMessage());
        }
    }

    // ---------- MAIN MENU (not logged in) ----------
    static void mainMenu() {
        boolean running = true;
        while (running) {
            Screen.title("MAIN MENU");
            Screen.option(1, "Register");
            Screen.option(2, "Customer Login");
            Screen.option(3, "Admin Login");
            Screen.option(0, "Exit");
            int choice = Screen.readChoice();

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
                    Screen.error("Invalid choice. Try again.");
            }
        }
    }

    // ---------- CUSTOMER MENU ----------
    static void customerMenu(Customer customer) {
        boolean loggedIn = true;
        while (loggedIn) {
            Screen.title("CUSTOMER MENU");
            Screen.info("Logged in as " + customer.getName() + " (" + customer.getRole() + ")");
            Screen.option(1, "Browse Movies");
            Screen.option(2, "Book a Ticket");
            Screen.option(3, "My Bookings");
            Screen.option(4, "My Profile");
            Screen.option(0, "Logout");
            int choice = Screen.readChoice();

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
                    Screen.success("You have been logged out.");
                    break;
                default:
                    Screen.error("Invalid choice. Try again.");
            }
        }
    }

    // ---------- ADMIN MENU ----------
    static void adminMenu(Admin admin) {
        boolean loggedIn = true;
        while (loggedIn) {
            Screen.title("ADMIN MENU");
            Screen.info("Logged in as " + admin.getName() + " (" + admin.getRole() + ")");
            Screen.option(1, "Manage Accounts");
            Screen.option(2, "Manage Venues (branches / rooms / seats)");
            Screen.option(3, "Manage Movies");
            Screen.option(4, "Manage Showtimes");
            Screen.option(5, "Manage Promo Codes");
            Screen.option(6, "Revenue Report");
            Screen.option(7, "Attendance Report");
            Screen.option(0, "Logout");
            int choice = Screen.readChoice();

            switch (choice) {
                case 1:
                    accountMenu.manageAccounts();                    // [A]
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
                    Screen.success("You have been logged out.");
                    break;
                default:
                    Screen.error("Invalid choice. Try again.");
            }
        }
    }
}
