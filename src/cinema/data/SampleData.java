package cinema.data;

import java.time.LocalDate;
import java.util.ArrayList;

import cinema.discount.NoDiscount;
import cinema.exception.CinemaException;
import cinema.layout.PremiumLayout;
import cinema.layout.StandardLayout;
import cinema.model.Booking;
import cinema.model.CinemaBranch;
import cinema.model.CinemaRoom;
import cinema.model.Customer;
import cinema.model.Movie;
import cinema.model.RegularTicket;
import cinema.model.Showtime;
import cinema.model.Ticket;
import cinema.payment.CardPayment;
import cinema.payment.CashPayment;
import cinema.payment.EWalletPayment;
import cinema.payment.PaymentStrategy;
import cinema.service.BookingManager;
import cinema.service.MovieManager;
import cinema.service.PromoManager;
import cinema.service.UserManager;
import cinema.service.VenueManager;

// [D] Starter data, created only on the FIRST run (when there is no data folder yet) and then saved.
// Showtimes are created relative to today so they are always in the future on the first run.
// Ratings and runtimes are sample values for this project.
public class SampleData {

    public static void load(UserManager users, VenueManager venues, MovieManager movies, PromoManager promos)
            throws CinemaException {
        // Accounts: the default admin (username Admin123) and some customers
        users.registerAdmin("System Admin", "admin@cinema.com", "Admin123", "AdminLogin12");
        users.registerCustomer("Juan Dela Cruz", "juan@mail.com", "Customer1", LocalDate.of(2005, 3, 14));
        Customer maria = users.registerCustomer("Maria Santos", "maria@mail.com", "Customer2", LocalDate.of(2007, 6, 2));
        Customer jose = users.registerCustomer("Jose Reyes", "jose@mail.com", "Customer3", LocalDate.of(1992, 1, 20));
        users.registerCustomer("Remedios Cruz", "lola@mail.com", "Senior123", LocalDate.of(1959, 5, 10));   // senior citizen

        // Branches and rooms (Standard = last row VIP, Premium = back half VIP)
        CinemaBranch sm = venues.createBranch("SM City Lipa", "Ayala Highway, Lipa City, Batangas");
        CinemaBranch rob = venues.createBranch("Robinsons Place Lipa", "JP Laurel Highway, Lipa City, Batangas");
        CinemaRoom sm1 = venues.addRoomToBranch(sm.getBranchId(), "Cinema 1", 6, 10, new StandardLayout());
        CinemaRoom sm2 = venues.addRoomToBranch(sm.getBranchId(), "Cinema 2", 5, 8, new StandardLayout());
        CinemaRoom sm3 = venues.addRoomToBranch(sm.getBranchId(), "Cinema 3", 4, 8, new PremiumLayout());
        CinemaRoom rob1 = venues.addRoomToBranch(rob.getBranchId(), "Cinema 1", 6, 10, new PremiumLayout());
        CinemaRoom rob2 = venues.addRoomToBranch(rob.getBranchId(), "Cinema 2", 5, 8, new StandardLayout());

        // Movies (MTRCB ratings: G, PG, R-13, R-16, R-18)
        Movie spiderMan = movies.createMovie("Spider-Man: Brand New Day", "Superhero", "PG", 140, "NOW SHOWING");
        Movie digger = movies.createMovie("Digger", "Comedy", "R-13", 135, "NOW SHOWING");
        Movie streetFighter = movies.createMovie("Street Fighter", "Action", "R-13", 115, "NOW SHOWING");
        Movie clayface = movies.createMovie("Clayface", "Horror", "R-16", 110, "NOW SHOWING");
        Movie socialReckoning = movies.createMovie("The Social Reckoning", "Drama", "R-13", 125, "NOW SHOWING");
        Movie terrifier = movies.createMovie("Terrifier 4", "Horror", "R-18", 120, "NOW SHOWING");
        movies.createMovie("The Hunger Games: Sunrise on the Reaping", "Sci-Fi", "R-13", 140, "COMING SOON");
        movies.createMovie("Avengers: Doomsday", "Superhero", "PG", 150, "COMING SOON");
        movies.createMovie("Dune: Part Three", "Sci-Fi", "PG", 165, "COMING SOON");

        // Showtimes for tomorrow (d1) and the day after (d2). Prices are for a regular seat.
        String d1 = LocalDate.now().plusDays(1).toString();
        String d2 = LocalDate.now().plusDays(2).toString();

        movies.createShowtime(spiderMan, sm1, d1 + " 10:30", 400);
        Showtime spiderManAfternoon = movies.createShowtime(spiderMan, sm1, d1 + " 13:30", 400);
        movies.createShowtime(spiderMan, sm1, d1 + " 16:30", 400);
        Showtime spiderManEvening = movies.createShowtime(spiderMan, sm1, d1 + " 19:30", 400);
        movies.createShowtime(spiderMan, sm1, d2 + " 13:30", 400);
        movies.createShowtime(spiderMan, sm1, d2 + " 19:30", 400);

        movies.createShowtime(digger, sm2, d1 + " 11:00", 380);
        movies.createShowtime(streetFighter, sm2, d1 + " 14:00", 380);
        movies.createShowtime(digger, sm2, d1 + " 16:30", 380);
        Showtime clayfaceEvening = movies.createShowtime(clayface, sm2, d1 + " 19:30", 380);
        movies.createShowtime(streetFighter, sm2, d2 + " 15:00", 380);
        movies.createShowtime(clayface, sm2, d2 + " 20:00", 380);

        movies.createShowtime(socialReckoning, sm3, d1 + " 13:00", 360);
        movies.createShowtime(terrifier, sm3, d1 + " 21:30", 360);
        movies.createShowtime(socialReckoning, sm3, d2 + " 18:00", 360);

        movies.createShowtime(spiderMan, rob1, d1 + " 12:00", 370);
        movies.createShowtime(spiderMan, rob1, d1 + " 15:00", 370);
        Showtime spiderManRobinsons = movies.createShowtime(spiderMan, rob1, d1 + " 18:00", 370);
        movies.createShowtime(spiderMan, rob1, d2 + " 15:00", 370);
        movies.createShowtime(spiderMan, rob1, d2 + " 20:30", 370);

        movies.createShowtime(streetFighter, rob2, d1 + " 13:00", 350);
        movies.createShowtime(terrifier, rob2, d1 + " 22:00", 350);
        movies.createShowtime(digger, rob2, d2 + " 17:00", 350);

        // Bookings by other customers, so some seats are already taken like in a real cinema
        sampleBooking(maria, spiderManAfternoon, "D5,D6", new EWalletPayment("GCash"));
        sampleBooking(jose, spiderManAfternoon, "F5,F6", new CardPayment());
        sampleBooking(jose, spiderManEvening, "E4,E5,E6,E7", new CardPayment());
        sampleBooking(maria, spiderManRobinsons, "C5,C6", new EWalletPayment("Maya"));
        sampleBooking(jose, clayfaceEvening, "C3,C4", new CashPayment());

        promos.addPromo("WELCOME10", 10, LocalDate.now().plusDays(180));
        promos.addPromo("MOVIENIGHT", 15, LocalDate.now().plusDays(90));
    }

    // A paid booking made by another customer (regular tickets, no discount)
    private static void sampleBooking(Customer customer, Showtime showtime, String seatIds, PaymentStrategy payment)
            throws CinemaException {
        ArrayList<Ticket> tickets = new ArrayList<>();
        for (String id : seatIds.split(",")) {
            tickets.add(new RegularTicket(showtime, showtime.findSeat(id)));
        }
        NoDiscount noDiscount = new NoDiscount();
        Booking booking = BookingManager.getInstance().createBooking(customer, showtime, tickets, noDiscount);
        BookingManager.getInstance().confirmPayment(booking, payment, booking.getTotalPrice(), noDiscount);
    }
}
