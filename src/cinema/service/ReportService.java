package cinema.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cinema.model.Booking;
import cinema.model.Showtime;
import cinema.ui.Screen;
import cinema.ui.Table;
import cinema.util.Format;

// [Member D] Reports for the admin. Read data from BookingManager.getInstance().
//
// How the numbers are made (all from the saved Booking records, never from temporary variables):
//   Gross sales  = sum of each counted booking's subtotal      (before discounts)
//   Discounts    = sum of each counted booking's discount amount
//   Net revenue  = sum of the amounts actually PAID            (= gross - discounts)
//   Refunded     = sum of the refunds of cancelled bookings    (money already given back)
// A booking is "counted" only when it is CONFIRMED and its payment was really recorded.
// Cancelled bookings are fully refunded, so they are not revenue; they are shown separately.
// Money is added with BigDecimal, so the total always equals the sum of the lines under it.
public class ReportService {

    // Revenue counts only confirmed bookings that were really paid.
    private boolean isCounted(Booking b) {
        return "CONFIRMED".equalsIgnoreCase(b.getStatus()) && b.isPaid();
    }

    private String money(BigDecimal amount) {
        return Format.peso(amount.doubleValue());
    }

    public void showRevenueReport() {
        Screen.title("REVENUE REPORT");
        ArrayList<Booking> bookings = BookingManager.getInstance().getAllBookings();
        if (bookings.isEmpty()) {
            Screen.info("No bookings have been made yet.");
            return;
        }

        BigDecimal gross = BigDecimal.ZERO;
        BigDecimal discounts = BigDecimal.ZERO;
        BigDecimal net = BigDecimal.ZERO;
        BigDecimal refunded = BigDecimal.ZERO;
        int confirmed = 0, cancelled = 0, awaitingPayment = 0;
        Map<String, BigDecimal> byMovie = new LinkedHashMap<>();
        Map<String, BigDecimal> byMethod = new LinkedHashMap<>();

        for (Booking b : bookings) {
            if (isCounted(b)) {
                BigDecimal paid = BookingManager.toMoney(b.getAmountPaid());
                gross = gross.add(BookingManager.toMoney(b.getSubtotal()));
                discounts = discounts.add(BookingManager.toMoney(b.getDiscountAmount()));
                net = net.add(paid);
                confirmed++;
                byMovie.merge(b.getShowtime().getMovie().getTitle(), paid, BigDecimal::add);
                byMethod.merge(b.getPayment().getDisplayName(), paid, BigDecimal::add);
            } else if ("CANCELLED".equalsIgnoreCase(b.getStatus())) {
                cancelled++;
                refunded = refunded.add(BookingManager.toMoney(b.getRefundedAmount()));
            } else {
                awaitingPayment++;   // should never happen: a booking is removed if its payment fails
            }
        }

        System.out.println("  Confirmed bookings: " + confirmed);
        System.out.println("  Cancelled bookings: " + cancelled);
        if (awaitingPayment > 0) {
            System.out.println("  Bookings without a payment record (not counted): " + awaitingPayment);
        }
        Screen.section("SALES");
        printLine("Gross ticket sales", money(gross));
        printLine("Less: discounts given", "- " + money(discounts));
        printLine("Total revenue (net, paid)", money(net));
        printLine("Refunded for cancelled bookings", money(refunded));

        if (!byMovie.isEmpty()) {
            Screen.section("REVENUE BY MOVIE");
            for (Map.Entry<String, BigDecimal> e : byMovie.entrySet()) {
                printLine(e.getKey(), money(e.getValue()));
            }
        }
        if (!byMethod.isEmpty()) {
            Screen.section("REVENUE BY PAYMENT METHOD");
            for (Map.Entry<String, BigDecimal> e : byMethod.entrySet()) {
                printLine(e.getKey(), money(e.getValue()));
            }
        }
    }

    // "label ...... amount". A long movie title is cut with "..." so the amounts stay in one column.
    private void printLine(String label, String amount) {
        System.out.println("  " + Format.fit(label, 42) + " " + Format.fitRight(amount, 18));
    }

    public void showAttendanceReport() {
        Screen.title("ATTENDANCE REPORT");
        ArrayList<Booking> bookings = BookingManager.getInstance().getAllBookings();
        int totalTicketsSold = 0;
        Map<Showtime, Integer> perShowtime = new LinkedHashMap<>();

        for (Booking b : bookings) {
            if (isCounted(b)) {
                int n = b.getSeats().size();
                totalTicketsSold += n;
                perShowtime.merge(b.getShowtime(), n, Integer::sum);
            }
        }

        if (totalTicketsSold == 0) {
            Screen.info("No tickets have been sold yet.");
            return;
        }
        System.out.println("  Total tickets sold (confirmed): " + totalTicketsSold);
        Screen.info("Includes confirmed bookings for shows that have not started yet.");

        // Earliest showtime first
        List<Map.Entry<Showtime, Integer>> rows = new ArrayList<>(perShowtime.entrySet());
        rows.sort((a, b) -> a.getKey().getStart().compareTo(b.getKey().getStart()));

        Screen.section("PER SHOWTIME");
        Table table = new Table(6, 25, 21, 7, 6);
        table.alignRight(3, 4);
        table.printHeader("ID", "MOVIE", "DATE AND TIME", "SOLD", "FILLED");
        for (Map.Entry<Showtime, Integer> e : rows) {
            Showtime s = e.getKey();
            int capacity = s.getCapacity();
            double percent = 0;
            if (capacity > 0) {
                percent = 100.0 * e.getValue() / capacity;
            }
            table.printRow(s.getShowtimeId(), s.getMovie().getTitle(), Format.shortDateTime(s.getStart()),
                    e.getValue() + "/" + capacity, String.format("%.0f%%", percent));
        }
    }
}