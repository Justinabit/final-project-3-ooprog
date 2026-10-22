package cinema;

import java.util.ArrayList;

import cinema.discount.NoDiscount;
import cinema.exception.CinemaException;
import cinema.layout.PremiumLayout;
import cinema.model.Admin;
import cinema.model.Booking;
import cinema.model.CinemaBranch;
import cinema.model.CinemaRoom;
import cinema.model.Customer;
import cinema.model.Movie;
import cinema.model.RegularTicket;
import cinema.model.Showtime;
import cinema.model.Ticket;
import cinema.model.User;
import cinema.payment.CardPayment;
import cinema.payment.CashPayment;
import cinema.payment.EWalletPayment;
import cinema.payment.PaymentStrategy;
import cinema.service.BookingManager;
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
    // Ratings and runtimes are sample values for this project.
    static void loadSampleData() {
        try {
            // Accounts: the default admin and some customers
            userManager.addUser(new Admin(userManager.nextUserId(), "System Admin", "admin@cinema.com", "Admin123"));
            userManager.registerCustomer("Juan Dela Cruz", "juan@mail.com", "Customer1", 21);
            Customer maria = new Customer(userManager.nextUserId(), "Maria Santos", "maria@mail.com", "Customer2", 19);
            Customer jose = new Customer(userManager.nextUserId(), "Jose Reyes", "jose@mail.com", "Customer3", 34);
            userManager.addUser(maria);
            userManager.addUser(jose);
            userManager.registerCustomer("Remedios Cruz", "lola@mail.com", "Senior123", 67);   // senior citizen

            // Branches and rooms (Standard = last row VIP, Premium = back half VIP)
            CinemaBranch sm = new CinemaBranch(venueManager.nextBranchId(), "SM City Lipa", "Ayala Highway, Lipa City, Batangas");
            CinemaBranch rob = new CinemaBranch(venueManager.nextBranchId(), "Robinsons Place Lipa", "JP Laurel Highway, Lipa City, Batangas");
            venueManager.addBranch(sm);
            venueManager.addBranch(rob);
            CinemaRoom sm1 = venueManager.addRoomToBranch(sm.getBranchId(), "Cinema 1", 6, 10);
            CinemaRoom sm2 = venueManager.addRoomToBranch(sm.getBranchId(), "Cinema 2", 5, 8);
            CinemaRoom sm3 = venueManager.addRoomToBranch(sm.getBranchId(), "Cinema 3", 4, 8, new PremiumLayout());
            CinemaRoom rob1 = venueManager.addRoomToBranch(rob.getBranchId(), "Cinema 1", 6, 10, new PremiumLayout());
            CinemaRoom rob2 = venueManager.addRoomToBranch(rob.getBranchId(), "Cinema 2", 5, 8);

            // Movies (MTRCB ratings: G, PG, R-13, R-16, R-18)
            Movie spiderMan = addMovie("Spider-Man: Brand New Day", "Superhero", "PG", 140, "NOW SHOWING");
            Movie digger = addMovie("Digger", "Comedy", "R-13", 135, "NOW SHOWING");
            Movie streetFighter = addMovie("Street Fighter", "Action", "R-13", 115, "NOW SHOWING");
            Movie clayface = addMovie("Clayface", "Horror", "R-16", 110, "NOW SHOWING");
            Movie socialReckoning = addMovie("The Social Reckoning", "Drama", "R-13", 125, "NOW SHOWING");
            Movie terrifier = addMovie("Terrifier 4", "Horror", "R-18", 120, "NOW SHOWING");
            addMovie("The Hunger Games: Sunrise on the Reaping", "Sci-Fi", "R-13", 140, "COMING SOON");
            addMovie("Avengers: Doomsday", "Superhero", "PG", 150, "COMING SOON");
            addMovie("Dune: Part Three", "Sci-Fi", "PG", 165, "COMING SOON");

            // Showtimes for tomorrow (d1) and the day after (d2). Prices are for a regular seat.
            String d1 = java.time.LocalDate.now().plusDays(1).toString();
            String d2 = java.time.LocalDate.now().plusDays(2).toString();

            addShow(spiderMan, sm1, d1 + " 10:30", 400);
            Showtime spiderManAfternoon = addShow(spiderMan, sm1, d1 + " 13:30", 400);
            addShow(spiderMan, sm1, d1 + " 16:30", 400);
            Showtime spiderManEvening = addShow(spiderMan, sm1, d1 + " 19:30", 400);
            addShow(spiderMan, sm1, d2 + " 13:30", 400);
            addShow(spiderMan, sm1, d2 + " 19:30", 400);

            addShow(digger, sm2, d1 + " 11:00", 380);
            addShow(streetFighter, sm2, d1 + " 14:00", 380);
            addShow(digger, sm2, d1 + " 16:30", 380);
            Showtime clayfaceEvening = addShow(clayface, sm2, d1 + " 19:30", 380);
            addShow(streetFighter, sm2, d2 + " 15:00", 380);
            addShow(clayface, sm2, d2 + " 20:00", 380);

            addShow(socialReckoning, sm3, d1 + " 13:00", 360);
            addShow(terrifier, sm3, d1 + " 21:30", 360);
            addShow(socialReckoning, sm3, d2 + " 18:00", 360);

            addShow(spiderMan, rob1, d1 + " 12:00", 370);
            addShow(spiderMan, rob1, d1 + " 15:00", 370);
            Showtime spiderManRobinsons = addShow(spiderMan, rob1, d1 + " 18:00", 370);
            addShow(spiderMan, rob1, d2 + " 15:00", 370);
            addShow(spiderMan, rob1, d2 + " 20:30", 370);

            addShow(streetFighter, rob2, d1 + " 13:00", 350);
            addShow(terrifier, rob2, d1 + " 22:00", 350);
            addShow(digger, rob2, d2 + " 17:00", 350);

            // Bookings by other customers, so some seats are already taken like in a real cinema
            sampleBooking(maria, spiderManAfternoon, "D5,D6", new EWalletPayment("GCash"));
            sampleBooking(jose, spiderManAfternoon, "F5,F6", new CardPayment());
            sampleBooking(jose, spiderManEvening, "E4,E5,E6,E7", new CardPayment());
            sampleBooking(maria, spiderManRobinsons, "C5,C6", new EWalletPayment("Maya"));
            sampleBooking(jose, clayfaceEvening, "C3,C4", new CashPayment());

            promoManager.addPromo("WELCOME10", 10);
            promoManager.addPromo("MOVIENIGHT", 15);
        } catch (CinemaException e) {
            Screen.error("Could not load sample data: " + e.getMessage());
        }
    }

    static Movie addMovie(String title, String genre, String rating, int minutes, String status) throws CinemaException {
        Movie movie = new Movie(movieManager.nextMovieId(), title, genre, rating, minutes, status);
        movieManager.addMovie(movie);
        return movie;
    }

    static Showtime addShow(Movie movie, CinemaRoom room, String dateTime, double price) throws CinemaException {
        Showtime showtime = new Showtime(movieManager.nextShowtimeId(), movie, room, dateTime, price);
        movieManager.addShowtime(showtime);
        return showtime;
    }

    // A paid booking made by another customer (regular tickets, no discount)
    static void sampleBooking(Customer customer, Showtime showtime, String seatIds, PaymentStrategy payment)
            throws CinemaException {
        ArrayList<Ticket> tickets = new ArrayList<>();
        for (String id : seatIds.split(",")) {
            tickets.add(new RegularTicket(showtime, showtime.findSeat(id)));
        }
        Booking booking = BookingManager.getInstance().createBooking(customer, showtime, tickets, new NoDiscount());
        booking.setPayment(payment);
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
