# Cinema Booking System (Skeleton)
mason ako gago

## Project structure
```
src/cinema/
├── Main.java        entry point + sample data
├── model/           User, Admin, Customer, Movie, Showtime, Seat, CinemaRoom,
│                    CinemaBranch, Booking, Ticket, RegularTicket
├── service/         UserManager, MovieManager, VenueManager,
│                    BookingManager (Singleton), ReportService
├── ui/              AccountMenu, MovieMenu, VenueMenu, BookingMenu
├── payment/         PaymentStrategy, CashPayment, CardPayment
├── discount/        DiscountStrategy, NoDiscount, StudentDiscount
├── exception/       CinemaException + Validation/Login/Booking/PaymentException
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
- Search the code for `TODO` to find what's left. The letter in brackets is the owner.
- Don't rename methods others use without telling the team.
- Keep Scanner / System.out in the *Menu classes; keep managers as plain logic.
