<<<<<<< HEAD
# Cinema Booking System (Skeleton)
=======
# CURATOR - Cinema Booking System

## Project structure
```
src/cinema/
├── Main.java        entry point + sample data
├── model/           User, Admin, Customer, Movie, Showtime, Seat, StandardSeat, VipSeat,
│                    CinemaRoom, CinemaBranch, Booking, Ticket, RegularTicket, StudentTicket, Promo
├── service/         UserManager, MovieManager, VenueManager, PromoManager,
│                    BookingManager (Singleton), ReportService
├── ui/              Screen (shared look + welcome screen), SeatMap, AccountMenu, MovieMenu, VenueMenu, BookingMenu
├── payment/         PaymentStrategy, CashPayment, CardPayment, EWalletPayment (GCash / Maya)
├── discount/        DiscountStrategy, NoDiscount, StudentDiscount, SeniorDiscount, PwdDiscount, PromoDiscount
├── layout/          SeatLayoutStrategy, StandardLayout, PremiumLayout, EconomyLayout
├── exception/       CinemaException + Validation/Login/Booking/Payment/VenueException
└── util/            Validator, ConsoleReader, Format (pesos and dates)
```
>>>>>>> main

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
| C - Booking & Promos | Booking, BookingManager (Singleton), DiscountStrategy, NoDiscount, StudentDiscount, SeniorDiscount, PwdDiscount, PromoDiscount, BookingMenu |
| D - Pricing, Payments, Reports | Ticket, RegularTicket, StudentTicket, PaymentStrategy, CashPayment, CardPayment, EWalletPayment, ReportService, ConsoleReader, loadSampleData() in Main |

Shared: the 6 Exception classes, Screen.java and SeatMap.java (used by every menu), Format.java and Main.java.

## Requirement checklist
- Encapsulation: private fields + getters/setters that validate (User, Customer, CinemaBranch, CinemaRoom, Movie)
- Abstraction: abstract User, abstract Seat, abstract Ticket, interfaces
- Inheritance: Admin/Customer extend User, StandardSeat/VipSeat extend Seat, RegularTicket/StudentTicket extend Ticket
- Polymorphism: getRole(), getDetails(), getExtraCharge(), isVipRow(), calculatePrice(), pay(), applyDiscount()
- Exceptions: CinemaException -> Validation / Login / Booking / Payment / Venue
- Singleton: BookingManager
- Strategy: PaymentStrategy (cash, card, GCash, Maya), DiscountStrategy (promo, senior, PWD, student) and SeatLayoutStrategy
- Validation: Validator class (Member A) + checks in every menu

## Rules
- All TODOs are implemented (`grep -rn TODO src` returns nothing).
- Don't rename methods others use without telling the team.
- Keep Scanner / System.out in the *Menu classes; keep managers as plain logic.
- Use `Screen` for titles, options and messages (`Screen.title`, `Screen.option`, `Screen.readChoice`,
  `Screen.success` / `error` / `info`) so every screen looks the same.
- Show money with `Format.peso(amount)` and dates with `Format.dateTime(...)` / `Format.shortDateTime(...)`.

## Sample data (loaded on start)
- Admin: `admin@cinema.com` / `Admin123`
- Customers: `juan@mail.com` / `Customer1` (age 21), `maria@mail.com` / `Customer2` (19),
  `jose@mail.com` / `Customer3` (34), `lola@mail.com` / `Senior123` (67, senior citizen)
- Promo codes: `WELCOME10` (10% off), `MOVIENIGHT` (15% off)
- Branches: SM City Lipa (Cinema 1-3) and Robinsons Place Lipa (Cinema 1-2)
- Now showing: Spider-Man: Brand New Day, Digger, Street Fighter, Clayface, The Social Reckoning, Terrifier 4.
  Coming soon: The Hunger Games: Sunrise on the Reaping, Avengers: Doomsday, Dune: Part Three.
  (Ratings and runtimes are sample values.)
- 23 showtimes dated tomorrow / the day after (always upcoming), PHP 350-400 for a regular seat
- A few seats are already booked by Maria and Jose, so the seat maps look like a real, busy cinema

## Behaviour decisions
- Register asks one field at a time and asks again if it is invalid; a blank field cancels. Passwords are typed twice.
- Login allows 3 attempts, then goes back to the main menu.
- Admins can create other admin accounts, reset a customer's password, and deactivate / reactivate customers.
- When adding a room the admin picks a seat layout: Standard (last row VIP), Premium (back half VIP) or Economy (no VIP).
  Single seats can be switched between Standard and VIP later; bookings already made keep their price.
- A room (or a whole branch) cannot be removed while it has upcoming showtimes with confirmed bookings;
  otherwise its showtimes are removed with it. Branch names are unique; room names are unique inside a branch.
- Currency is the Philippine peso, shown as `PHP 1,250.00` (the peso sign does not print in every console).
- Booking goes step by step like a real cinema app: choose a movie -> a schedule (grouped by branch) ->
  seats on the seat map -> ticket type and discount -> review and pay -> e-ticket with a booking reference (CUR-100001...).
- Seat map: `[A1 ]` regular, `{E1 }` VIP, `[XX ]` taken. VIP seats can be booked like any seat and cost PHP 200 more.
- Price = sum of each seat's own price (VIP = regular + PHP 200, Student ticket 20% off), then one discount
  (promo code, senior citizen 60+, or PWD with an ID number). Prices are VAT-inclusive.
- Payment: GCash / Maya (11-digit 09 number + a demo OTP shown on screen), credit/debit card, or cash.
- Seats are held first, then payment is taken (3 attempts). If payment fails, the seats are released and no booking is kept.
- Cancelling refunds through the same payment method; not allowed once a showtime has started.
- MTRCB ratings G, PG, R-13, R-16, R-18. R-13 / R-16 / R-18 movies are blocked for customers under 13 / 16 / 18.
- A movie cannot be removed while it has upcoming showtimes with confirmed bookings; otherwise its showtimes are removed with it.
- A room cannot host overlapping showtimes (15 minute cleaning gap). Showtimes use `yyyy-MM-dd HH:mm`.
- Passwords are stored in plain text (fine for a class project; hash them for real use).
