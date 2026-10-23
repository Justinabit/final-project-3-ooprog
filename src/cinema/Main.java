package cinema;

import java.util.ArrayList;

import cinema.data.ErrorLog;
import cinema.data.FileManager;
import cinema.data.SampleData;
import cinema.exception.CinemaException;
import cinema.model.Admin;
import cinema.model.Customer;
import cinema.model.User;
import cinema.service.BookingManager;
import cinema.service.MovieManager;
import cinema.service.PromoManager;
import cinema.service.ReportService;
import cinema.service.UserManager;
import cinema.service.VenueManager;
import cinema.ui.AccountMenu;
import cinema.ui.BookingMenu;
import cinema.ui.MovieMenu;
import cinema.ui.RecordsMenu;
import cinema.ui.Screen;
import cinema.ui.VenueMenu;

// Entry point. Loads the saved data, shows the menus and sends each choice to the right class.
// Tags in comments tell you who owns what:  [A] accounts/venues  [B] movies  [C] booking  [D] payments/reports
public class Main {

    // Shared managers and menus (created once, passed around)
    static UserManager userManager = new UserManager();         // A
    static VenueManager venueManager = new VenueManager();      // A
    static MovieManager movieManager = new MovieManager();      // B
    static PromoManager promoManager = new PromoManager();      // C
    static FileManager fileManager = new FileManager(userManager, venueManager, movieManager, promoManager);
    static AccountMenu accountMenu = new AccountMenu(userManager);                              // A
    static VenueMenu venueMenu = new VenueMenu(venueManager, movieManager);                     // A
    static MovieMenu movieMenu = new MovieMenu(movieManager, venueManager);                    // B
    static BookingMenu bookingMenu = new BookingMenu(movieManager, venueManager, promoManager); // C
    static ReportService reportService = new ReportService();                                   // D
    static RecordsMenu recordsMenu = new RecordsMenu(userManager, venueManager, bookingMenu, fileManager);

    public static void main(String[] args) {
        loadData();
        Screen.showWelcome();
        mainMenu();
        saveData();
        Screen.showGoodbye();
    }

    // ---------- FILE HANDLING ----------

    // First run (no data folder yet): create the sample data and save it. Later runs: load the saved files.
    static void loadData() {
        if (!fileManager.hasSavedData()) {
            try {
                SampleData.load(userManager, venueManager, movieManager, promoManager);
                Screen.info("First run: sample data created and saved in " + fileManager.getFolderPath());
            } catch (CinemaException e) {
                Screen.error("Could not create the sample data: " + e.getMessage());
            }
            saveData();
            return;
        }
        try {
            ArrayList<String> warnings = fileManager.loadAll();
            for (String warning : warnings) {
                Screen.error(warning);
            }
            Screen.info("Loaded saved data from " + fileManager.getFolderPath() + " ("
                    + userManager.getAllUsers().size() + " accounts, "
                    + BookingManager.getInstance().getAllBookings().size() + " bookings)");
        } catch (CinemaException e) {
            Screen.error(e.getMessage());
        }
        if (movieManager.getUpcomingShowtimes(null).isEmpty()) {
            Screen.info("There are no upcoming showtimes. An admin can add some in Manage Showtimes,");
            Screen.info("or close CURATOR and delete the data folder to start over with fresh sample data.");
        }
    }

    // Saves everything. Called after every menu action, so nothing is lost if the program is closed.
    static void saveData() {
        try {
            fileManager.saveAll();
        } catch (CinemaException e) {
            Screen.error(e.getMessage());
        }
    }

    // A program bug should never crash CURATOR in front of the user: log it and go on
    static void handleUnexpectedError(RuntimeException e) {
        ErrorLog.write(e);
        Screen.error("Something went wrong (" + e.getClass().getSimpleName() + "). The details were saved in "
                + FileManager.FOLDER + "/" + ErrorLog.FILE_NAME + ".");
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
            int choice = Screen.readChoice(3);
            try {
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
            } catch (RuntimeException e) {
                handleUnexpectedError(e);
            }
            saveData();
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
            int choice = Screen.readChoice(4);
            try {
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
            } catch (RuntimeException e) {
                handleUnexpectedError(e);
            }
            saveData();
        }
    }

    // ---------- ADMIN MENU ----------
    static void adminMenu(Admin admin) {
        boolean loggedIn = true;
        while (loggedIn) {
            Screen.title("ADMIN MENU");
            Screen.info("Logged in as " + admin.getName() + " (" + admin.getRole() + ")");
            Screen.option(1, "View Registered Users");
            Screen.option(2, "Booked Tickets, Transactions and Data Files");
            Screen.option(3, "Manage Accounts");
            Screen.option(4, "Manage Venues (branches / rooms / seats)");
            Screen.option(5, "Manage Movies");
            Screen.option(6, "Manage Showtimes");
            Screen.option(7, "Manage Promo Codes");
            Screen.option(8, "Reports (revenue / attendance)");
            Screen.option(9, "My Profile");
            Screen.option(0, "Logout");
            int choice = Screen.readChoice(9);
            try {
                switch (choice) {
                    case 1:
                        recordsMenu.viewUsers();
                        break;
                    case 2:
                        recordsMenu.viewBookings();
                        break;
                    case 3:
                        accountMenu.manageAccounts();                    // [A]
                        break;
                    case 4:
                        venueMenu.show();                                // [A]
                        break;
                    case 5:
                        movieMenu.manageMovies();                        // [B]
                        break;
                    case 6:
                        movieMenu.manageShowtimes();                     // [B]
                        break;
                    case 7:
                        bookingMenu.managePromos();                      // [C]
                        break;
                    case 8:
                        reportsMenu();                                   // [D]
                        break;
                    case 9:
                        accountMenu.viewProfile(admin);                  // [A]
                        break;
                    case 0:
                        loggedIn = false;
                        Screen.success("You have been logged out.");
                        break;
                    default:
                        Screen.error("Invalid choice. Try again.");
                }
            } catch (RuntimeException e) {
                handleUnexpectedError(e);
            }
            saveData();
        }
    }

    // ---------- REPORTS (admin) ----------
    static void reportsMenu() {
        boolean back = false;
        while (!back) {
            Screen.title("REPORTS (ADMIN)");
            Screen.option(1, "Revenue Report");
            Screen.option(2, "Attendance Report");
            Screen.option(0, "Back");
            int choice = Screen.readChoice(2);
            switch (choice) {
                case 1:
                    reportService.showRevenueReport();
                    break;
                case 2:
                    reportService.showAttendanceReport();
                    break;
                default:
                    back = true;
            }
        }
    }
}
