package cinema.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import cinema.model.Booking;
import cinema.model.Showtime;
import cinema.ui.Screen;
import cinema.util.Format;

// [Member D] Reports for the admin. Read data from BookingManager.getInstance()
public class ReportService {

    public void showRevenueReport() {
        Screen.title("REVENUE REPORT");
        ArrayList<Booking> bookings = BookingManager.getInstance().getAllBookings();
        double totalRevenue = 0.0;
        int confirmed = 0, cancelled = 0;
        Map<String, Double> byMovie = new LinkedHashMap<>();

        for (Booking b : bookings) {
            if ("CONFIRMED".equalsIgnoreCase(b.getStatus())) {
                totalRevenue += b.getTotalPrice();
                confirmed++;
                byMovie.merge(b.getShowtime().getMovie().getTitle(), b.getTotalPrice(), Double::sum);
            } else {
                cancelled++;
            }
        }

        System.out.println("  Confirmed bookings: " + confirmed);
        System.out.println("  Cancelled bookings: " + cancelled);
        System.out.println("  Total revenue: " + Format.peso(totalRevenue));
        if (!byMovie.isEmpty()) {
            Screen.section("REVENUE BY MOVIE");
            for (Map.Entry<String, Double> e : byMovie.entrySet()) {
                System.out.printf("  %-42s %s%n", e.getKey(), Format.peso(e.getValue()));
            }
        }
    }

    public void showAttendanceReport() {
        Screen.title("ATTENDANCE REPORT");
        ArrayList<Booking> bookings = BookingManager.getInstance().getAllBookings();
        int totalTicketsSold = 0;
        Map<Showtime, Integer> perShowtime = new LinkedHashMap<>();

        for (Booking b : bookings) {
            if ("CONFIRMED".equalsIgnoreCase(b.getStatus())) {
                int n = b.getSeats().size();
                totalTicketsSold += n;
                perShowtime.merge(b.getShowtime(), n, Integer::sum);
            }
        }

        System.out.println("  Total tickets sold (confirmed): " + totalTicketsSold);
        if (!perShowtime.isEmpty()) {
            Screen.section("PER SHOWTIME");
            for (Map.Entry<Showtime, Integer> e : perShowtime.entrySet()) {
                Showtime s = e.getKey();
                System.out.printf("  %s | %s | %s | %d/%d seats (%.0f%%)%n", s.getShowtimeId(), s.getMovie().getTitle(),
                        Format.shortDateTime(s.getStart()), e.getValue(), s.getCapacity(), 100.0 * e.getValue() / s.getCapacity());
            }
        }
    }
}
