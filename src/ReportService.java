import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

// [Member D] Reports for the admin. Read data from BookingManager.getInstance()
public class ReportService {

    public void showRevenueReport() {
        System.out.println("\n--- REVENUE REPORT ---");
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

        System.out.println("Confirmed bookings: " + confirmed);
        System.out.println("Cancelled bookings: " + cancelled);
        System.out.printf("Total revenue: $%.2f%n", totalRevenue);
        if (!byMovie.isEmpty()) {
            System.out.println("Revenue by movie:");
            for (Map.Entry<String, Double> e : byMovie.entrySet()) {
                System.out.printf("  %-30s $%.2f%n", e.getKey(), e.getValue());
            }
        }
    }

    public void showAttendanceReport() {
        System.out.println("\n--- ATTENDANCE REPORT ---");
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

        System.out.println("Total tickets sold (confirmed): " + totalTicketsSold);
        if (!perShowtime.isEmpty()) {
            System.out.println("Per showtime:");
            for (Map.Entry<Showtime, Integer> e : perShowtime.entrySet()) {
                Showtime s = e.getKey();
                System.out.printf("  %s | %s | %s | %d/%d seats (%.0f%%)%n", s.getShowtimeId(), s.getMovie().getTitle(),
                        s.getDateTime(), e.getValue(), s.getCapacity(), 100.0 * e.getValue() / s.getCapacity());
            }
        }
    }
}
