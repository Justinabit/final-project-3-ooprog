# Cinema Booking System

## How to run
```
cd src
javac *.java
java Main
```

## Who does what
| Member | Files |
|---|---|
| A - Accounts & Venues | User, Admin, Customer, Validator, UserManager, AccountMenu, Seat, CinemaRoom, CinemaBranch, VenueManager, VenueMenu |
| B - Movies & Showtimes | Movie, Showtime, MovieManager, MovieMenu |
| C - Booking & Promos | Booking, BookingManager (Singleton), DiscountStrategy, NoDiscount, StudentDiscount, BookingMenu |
| D - Pricing, Payments, Reports | Ticket, RegularTicket, PaymentStrategy, CashPayment, CardPayment, ReportService, ConsoleReader, loadSampleData() in Main |

Shared: the 5 Exception classes and Main.java.

## Requirement checklist
- Encapsulation: private fields + getters/setters
- Abstraction: abstract User, abstract Ticket, interfaces
- Inheritance: Admin/Customer extend User, RegularTicket extends Ticket
- Polymorphism: getRole(), calculatePrice(), pay(), applyDiscount()
- Exceptions: CinemaException -> Validation / Login / Booking / Payment
- Singleton: BookingManager
- Strategy: PaymentStrategy and DiscountStrategy
- Validation: Validator class (Member A) + checks in every menu

## Rules
- All TODOs are implemented (`grep -rn TODO src` returns nothing).
- Don't rename methods others use without telling the team.
- Keep Scanner / System.out in the *Menu classes; keep managers as plain logic.

## Sample data (loaded on start)
- Admin: `admin@cinema.com` / `Admin123`
- Customer: `juan@mail.com` / `Customer1` (age 21)
- Promo code: `WELCOME10` (10% off)
- 2 branches, 3 rooms, 5 movies, 5 showtimes (dated tomorrow / the day after, so they are always upcoming)

## Behaviour decisions
- Price = sum of each seat's own price (VIP seats x1.5, Student ticket 20% off), then one discount (promo code, or senior 60+).
- Seats are held first, then payment is taken (3 attempts). If payment fails, the seats are released and no booking is kept.
- Cancelling refunds through the same payment method; not allowed once a showtime has started.
- R-16 / R-18 movies are blocked for customers under 16 / 18.
- A movie cannot be removed while it has upcoming showtimes with confirmed bookings; otherwise its showtimes are removed with it.
- A room cannot host overlapping showtimes (15 minute cleaning gap). Showtimes use `yyyy-MM-dd HH:mm`.
- Passwords are stored in plain text (fine for a class project; hash them for real use).
