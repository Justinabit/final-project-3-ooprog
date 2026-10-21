# CURATOR - Cinema Management System

## Project structure
```
src/cinema/
├── Main.java        entry point + sample data
├── model/           User, Admin, Customer, Movie, Showtime, Seat, StandardSeat, VipSeat,
│                    CinemaRoom, CinemaBranch, Booking, Ticket, RegularTicket, StudentTicket, Promo
├── service/         UserManager, MovieManager, VenueManager, PromoManager,
│                    BookingManager (Singleton), ReportService
├── ui/              Screen (shared look + welcome screen), AccountMenu, MovieMenu, VenueMenu, BookingMenu
├── payment/         PaymentStrategy, CashPayment, CardPayment
├── discount/        DiscountStrategy, NoDiscount, StudentDiscount, SeniorDiscount, PromoDiscount
├── layout/          SeatLayoutStrategy, StandardLayout, PremiumLayout, EconomyLayout
├── exception/       CinemaException + Validation/Login/Booking/Payment/VenueException
└── util/            Validator, ConsoleReader
```

## How to run
From the project root:
```
# PowerShell
javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
# bash / macOS / Linux
javac -d out $(find src -name "*.java")

java -cp out cinema.Main
```

## Who does what
| Member | Files |
|---|---|
| A - Accounts & Venues | User, Admin, Customer, Validator, UserManager, AccountMenu, Seat, StandardSeat, VipSeat, CinemaRoom, CinemaBranch, SeatLayoutStrategy + layouts, VenueManager, VenueMenu |
| B - Movies & Showtimes | Movie, Showtime, MovieManager, MovieMenu |
| C - Booking & Promos | Booking, BookingManager (Singleton), DiscountStrategy, NoDiscount, StudentDiscount, BookingMenu |
| D - Pricing, Payments, Reports | Ticket, RegularTicket, PaymentStrategy, CashPayment, CardPayment, ReportService, ConsoleReader, loadSampleData() in Main |

Shared: the 6 Exception classes, Screen.java (every menu uses it) and Main.java.

## Requirement checklist
- Encapsulation: private fields + getters/setters that validate (User, Customer, CinemaBranch, CinemaRoom, Movie)
- Abstraction: abstract User, abstract Seat, abstract Ticket, interfaces
- Inheritance: Admin/Customer extend User, StandardSeat/VipSeat extend Seat, RegularTicket/StudentTicket extend Ticket
- Polymorphism: getRole(), getDetails(), getPriceMultiplier(), isVipRow(), calculatePrice(), pay(), applyDiscount()
- Exceptions: CinemaException -> Validation / Login / Booking / Payment / Venue
- Singleton: BookingManager
- Strategy: PaymentStrategy, DiscountStrategy and SeatLayoutStrategy
- Validation: Validator class (Member A) + checks in every menu

## Rules
- All TODOs are implemented (`grep -rn TODO src` returns nothing).
- Don't rename methods others use without telling the team.
- Keep Scanner / System.out in the *Menu classes; keep managers as plain logic.
- Use `Screen` for titles, options and messages (`Screen.title`, `Screen.option`, `Screen.readChoice`,
  `Screen.success` / `error` / `info`) so every screen looks the same.

## Sample data (loaded on start)
- Admin: `admin@cinema.com` / `Admin123`
- Customer: `juan@mail.com` / `Customer1` (age 21)
- Promo code: `WELCOME10` (10% off)
- 2 branches, 3 rooms, 5 movies, 5 showtimes (dated tomorrow / the day after, so they are always upcoming)

## Behaviour decisions
- Register asks one field at a time and asks again if it is invalid; a blank field cancels. Passwords are typed twice.
- Login allows 3 attempts, then goes back to the main menu.
- Admins can create other admin accounts, reset a customer's password, and deactivate / reactivate customers.
- When adding a room the admin picks a seat layout: Standard (last row VIP), Premium (back half VIP) or Economy (no VIP).
  Single seats can be switched between Standard and VIP later; bookings already made keep their price.
- A room (or a whole branch) cannot be removed while it has upcoming showtimes with confirmed bookings;
  otherwise its showtimes are removed with it. Branch names are unique; room names are unique inside a branch.
- Price = sum of each seat's own price (VIP seats x1.5, Student ticket 20% off), then one discount (promo code, or senior 60+).
- Seats are held first, then payment is taken (3 attempts). If payment fails, the seats are released and no booking is kept.
- Cancelling refunds through the same payment method; not allowed once a showtime has started.
- R-16 / R-18 movies are blocked for customers under 16 / 18.
- A movie cannot be removed while it has upcoming showtimes with confirmed bookings; otherwise its showtimes are removed with it.
- A room cannot host overlapping showtimes (15 minute cleaning gap). Showtimes use `yyyy-MM-dd HH:mm`.
- Passwords are stored in plain text (fine for a class project; hash them for real use).
