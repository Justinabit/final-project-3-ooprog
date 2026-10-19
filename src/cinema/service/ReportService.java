package cinema.service;

// [Member D] Reports for the admin. Read data from BookingManager.getInstance()
public class ReportService {

    public void showRevenueReport() {
        System.out.println("\n--- REVENUE REPORT ---");
        // TODO (D): total revenue (only CONFIRMED bookings), maybe per branch or per movie
    }

    public void showAttendanceReport() {
        System.out.println("\n--- ATTENDANCE REPORT ---");
        // TODO (D): number of tickets sold per movie / per showtime
    }
}
