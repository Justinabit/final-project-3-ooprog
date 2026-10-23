package cinema.ui;

import java.util.ArrayList;

import cinema.data.FileManager;
import cinema.data.TransactionLog;
import cinema.exception.BookingException;
import cinema.exception.GoBackException;
import cinema.exception.DataException;
import cinema.model.Booking;
import cinema.model.Customer;
import cinema.model.Showtime;
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
        AccountMenu.printAccounts(userManager.getAllUsers());

        Screen.section("CUSTOMER ACTIVITY");
        Table table = new Table(6, 16, 12, 8, 7, 14);
        table.alignRight(3, 4, 5);
        table.printHeader("ID", "NAME", "REGISTERED", "BOOKINGS", "TICKETS", "TOTAL SPENT");
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
            table.printRow(c.getUserId(), c.getName(), Format.date(c.getRegisteredAt().toLocalDate()),
                    String.valueOf(bookings), String.valueOf(tickets), Format.peso(spent));
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
            Screen.title("BOOKED TICKETS, TRANSACTIONS AND FILES (ADMIN)");
            Screen.option(1, "All bookings");
            Screen.option(2, "Bookings of one customer");
            Screen.option(3, "Open a booking by reference (e-ticket / cancel)");
            Screen.option(4, "Transaction log (every payment and refund)");
            Screen.option(5, "Data files (saved records)");
            Screen.option(0, "Back");
            int choice = Screen.readMenuChoice(5);
            try {
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
                    case 5:
                        showDataFiles();
                        break;
                    case 0:
                        back = true;
                        break;
                    default:
                        Screen.error("Invalid choice. Try again.");
                }
            } catch (GoBackException e) {
                Screen.backTo("Booked Tickets");
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
        int lastOption = 0;
        if (canCancel) {
            Screen.option(1, "Cancel this booking and refund the customer");
            lastOption = 1;
        }
        Screen.option(0, "Back");
        int choice = Screen.readChoice(lastOption);
        if (choice != 1) return;
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

    // Four lines per booking, then the totals. Long names, titles and cinema names end with "...".
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
            System.out.println("  " + Format.fit(b.getBookingId(), 10) + "  " + Format.fit(b.getStatus(), 9)
                    + "  " + Format.peso(b.getTotalPrice()) + " (" + paidWith + ")");
            // the title gets whatever room the name leaves on the line
            String who = Format.shorten(b.getCustomer().getName(), 22) + " (" + b.getCustomer().getUserId() + ")";
            System.out.println("              " + Format.shorten(who + "  |  " + s.getMovie().getTitle(), 61));
            System.out.println("              " + Format.shorten(venueManager.getCinemaName(s.getRoom()), 35)
                    + "  |  " + Format.shortDateTime(s.getStart()));
            System.out.println("              Seats: " + b.getSeatList());
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
        Table table = new Table(16, 7, 10, 5, 17, 8);
        table.alignRight(5);
        table.printHeader("DATE AND TIME", "TYPE", "BOOKING", "USER", "METHOD", "AMOUNT");
        for (String[] r : rows) {
            if (r.length == 6) {
                table.printRow(r);
            }
        }
        System.out.println("  " + rows.size() + " transaction(s). Saved in " + FileManager.FOLDER + "/"
                + TransactionLog.FILE_NAME);
    }

    // ---------- data files ----------

    private void showDataFiles() {
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
            int choice = Screen.readMenuChoice(1);
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
