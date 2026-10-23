package cinema.ui;

import java.util.ArrayList;

import cinema.data.FileManager;
import cinema.data.TransactionLog;
import cinema.exception.BookingException;
import cinema.exception.DataException;
import cinema.model.Booking;
import cinema.model.Customer;
import cinema.model.Showtime;
import cinema.model.User;
import cinema.service.BookingManager;
import cinema.service.UserManager;
import cinema.service.VenueManager;
import cinema.util.ConsoleReader;
import cinema.util.Format;

// Admin screens for the saved records: registered users, booked tickets, transactions and data files
public class RecordsMenu {
    private UserManager userManager;
    private VenueManager venueManager;
    private BookingMenu bookingMenu;     // to show the same e-ticket the customer sees
    private FileManager fileManager;

    public RecordsMenu(UserManager userManager, VenueManager venueManager, BookingMenu bookingMenu,
            FileManager fileManager) {
        this.userManager = userManager;
        this.venueManager = venueManager;
        this.bookingMenu = bookingMenu;
        this.fileManager = fileManager;
    }

    // ---------- registered users ----------

    public void viewUsers() {
        Screen.title("REGISTERED USERS (ADMIN)");
        System.out.println("  " + String.format(User.TABLE_FORMAT, "ID", "NAME", "EMAIL", "ROLE", "STATUS") + "AGE");
        Screen.line();
        for (User user : userManager.getAllUsers()) {
            System.out.println("  " + user.getDetails());   // Admin and Customer each describe themselves
        }

        Screen.section("CUSTOMER ACTIVITY");
        System.out.println(String.format("  %-5s %-18s %-13s %8s %8s %16s",
                "ID", "NAME", "REGISTERED", "BOOKINGS", "TICKETS", "TOTAL SPENT"));
        Screen.line();
        for (Customer c : userManager.getAllCustomers()) {
            int bookings = 0;
            int tickets = 0;
            double spent = 0;
            for (Booking b : BookingManager.getInstance().getBookingsForCustomer(c.getUserId())) {
                if ("CONFIRMED".equals(b.getStatus())) {
                    bookings++;
                    tickets += b.getSeats().size();
                    spent += b.getAmountPaid();
                }
            }
            System.out.println(String.format("  %-5s %-18s %-13s %8d %8d %16s", c.getUserId(), c.getName(),
                    Format.date(c.getRegisteredAt().toLocalDate()), bookings, tickets, Format.peso(spent)));
        }
        Screen.line();
        System.out.println("  " + userManager.getAllUsers().size() + " account(s), "
                + userManager.getAllCustomers().size() + " customer(s). Saved in "
                + FileManager.FOLDER + "/users.txt");
    }

    // ---------- booked tickets and transactions ----------

    public void viewBookings() {
        boolean back = false;
        while (!back) {
            Screen.title("BOOKED TICKETS (ADMIN)");
            Screen.option(1, "All bookings");
            Screen.option(2, "Bookings of one customer");
            Screen.option(3, "Open a booking by reference (e-ticket / cancel)");
            Screen.option(4, "Transaction log (every payment and refund)");
            Screen.option(0, "Back");
            int choice = Screen.readChoice();
            switch (choice) {
                case 1:
                    Screen.section("ALL BOOKINGS");
                    printBookings(BookingManager.getInstance().getAllBookings());
                    break;
                case 2:
                    bookingsOfCustomer();
                    break;
                case 3:
                    openBooking();
                    break;
                case 4:
                    printTransactions();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    Screen.error("Invalid choice. Try again.");
            }
        }
    }

    private void bookingsOfCustomer() {
        Customer customer = userManager.findCustomer(ConsoleReader.readLine("Customer ID (e.g. U-2): "));
        if (customer == null) {
            Screen.error("No customer with that ID.");
            return;
        }
        Screen.section("BOOKINGS OF " + customer.getName().toUpperCase());
        printBookings(BookingManager.getInstance().getBookingsForCustomer(customer.getUserId()));
    }

    private void openBooking() {
        Booking booking = BookingManager.getInstance().findBooking(
                ConsoleReader.readLine("Booking reference (e.g. CUR-100001): "));
        if (booking == null) {
            Screen.error("No booking with that reference.");
            return;
        }
        Customer c = booking.getCustomer();
        bookingMenu.printETicket(booking);
        System.out.println("  Booked by : " + c.getName() + " (" + c.getUserId() + ", " + c.getEmail() + ")");
        if (booking.getPaidAt() != null) {
            System.out.println("  Paid on   : " + Format.dateTime(booking.getPaidAt()));
        }

        boolean canCancel = "CONFIRMED".equals(booking.getStatus()) && !booking.getShowtime().hasStarted();
        if (canCancel) {
            Screen.option(1, "Cancel this booking and refund the customer");
        }
        Screen.option(0, "Back");
        int choice = Screen.readChoice();
        if (choice != 1 || !canCancel) return;
        if (!ConsoleReader.readYesNo("Cancel booking " + booking.getBookingId() + "? (y/n): ")) {
            Screen.info("The booking was kept.");
            return;
        }
        try {
            // BookingManager refunds the payment as part of cancelling
            BookingManager.getInstance().cancelBooking(booking.getBookingId(), null);
            Screen.success("Booking " + booking.getBookingId() + " cancelled.");
        } catch (BookingException e) {
            Screen.error(e.getMessage());
        }
    }

    // Three lines per booking, then the totals
    private void printBookings(ArrayList<Booking> bookings) {
        if (bookings.isEmpty()) {
            Screen.info("No bookings yet.");
            return;
        }
        int confirmed = 0;
        int tickets = 0;
        double paid = 0;
        for (Booking b : bookings) {
            Showtime s = b.getShowtime();
            String paidWith = "-";
            if (b.getPayment() != null) {
                paidWith = b.getPayment().getDisplayName();
            }
            System.out.println("  " + String.format("%-11s %-10s", b.getBookingId(), b.getStatus())
                    + "  " + Format.peso(b.getTotalPrice()) + " (" + paidWith + ")");
            System.out.println("              " + b.getCustomer().getName() + " (" + b.getCustomer().getUserId() + ")"
                    + "  |  " + s.getMovie().getTitle());
            System.out.println("              " + venueManager.getCinemaName(s.getRoom()) + "  |  "
                    + Format.shortDateTime(s.getStart()) + "  |  Seats: " + b.getSeatList());
            if ("CONFIRMED".equals(b.getStatus())) {
                confirmed++;
                tickets += b.getSeats().size();
                paid += b.getAmountPaid();
            }
        }
        Screen.line();
        System.out.println("  " + bookings.size() + " booking(s): " + confirmed + " confirmed, "
                + (bookings.size() - confirmed) + " cancelled  |  " + tickets + " ticket(s)  |  " + Format.peso(paid));
    }

    private void printTransactions() {
        Screen.section("TRANSACTION LOG");
        ArrayList<String[]> rows = TransactionLog.readAll();
        if (rows.isEmpty()) {
            Screen.info("No payments or refunds yet.");
            return;
        }
        System.out.println(String.format("  %-17s %-8s %-11s %-6s %-18s %12s",
                "DATE AND TIME", "TYPE", "BOOKING", "USER", "METHOD", "AMOUNT"));
        Screen.line();
        for (String[] r : rows) {
            if (r.length == 6) {
                System.out.println(String.format("  %-17s %-8s %-11s %-6s %-18s %12s", r[0], r[1], r[2], r[3], r[4], r[5]));
            }
        }
        System.out.println("  " + rows.size() + " transaction(s). Saved in " + FileManager.FOLDER + "/"
                + TransactionLog.FILE_NAME);
    }

    // ---------- data files ----------

    public void showDataFiles() {
        boolean back = false;
        while (!back) {
            Screen.title("DATA FILES (ADMIN)");
            System.out.println("  Folder: " + fileManager.getFolderPath());
            System.out.println();
            try {
                for (String line : fileManager.getFileSummary()) {
                    System.out.println("    " + line);
                }
            } catch (DataException e) {
                Screen.error(e.getMessage());
            }
            System.out.println();
            Screen.info("Changes are saved automatically after every action.");
            Screen.info("To start over with fresh sample data, close CURATOR and delete the data folder.");
            Screen.line();
            Screen.option(1, "Save now");
            Screen.option(0, "Back");
            int choice = Screen.readChoice();
            if (choice == 1) {
                try {
                    fileManager.saveAll();
                    Screen.success("All data saved.");
                } catch (DataException e) {
                    Screen.error(e.getMessage());
                }
            } else if (choice == 0) {
                back = true;
            } else {
                Screen.error("Invalid choice. Try again.");
            }
        }
    }
}
