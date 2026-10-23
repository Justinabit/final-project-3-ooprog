# CURATOR - Cinema Booking System

## How to run
From the project root:
```
# PowerShell
javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
# bash / macOS / Linux
javac -d out $(find src -name "*.java")

java -cp out cinema.Main
```
The first run creates the sample data and saves it in a `data` folder next to where you ran the program.
Every later run loads that folder, so accounts, bookings and admin changes are kept.

## Project structure
Each folder has one job, so when something goes wrong you know where to look.
```
src/cinema/
├── Main.java        start-up only: load data, show the menus, save after every action
├── model/           the things in a cinema: User, Admin, Customer, Movie, Showtime, Seat, StandardSeat, VipSeat,
│                    CinemaRoom, CinemaBranch, Booking, Ticket, RegularTicket, StudentTicket, Promo
├── service/         the rules (no Scanner / System.out): UserManager, MovieManager, VenueManager, PromoManager,
│                    BookingManager (Singleton), ReportService
├── ui/              the screens: Screen (shared look + welcome screen), SeatMap, AccountMenu, MovieMenu,
│                    VenueMenu, BookingMenu, RecordsMenu (admin: users, bookings, transactions, data files)
├── data/            everything that reads or writes files: FileManager, SampleData (first run only),
│                    TransactionLog (payments and refunds), ErrorLog (unexpected errors)
├── payment/         PaymentStrategy, CashPayment, CardPayment, EWalletPayment (GCash / Maya)
├── discount/        DiscountStrategy, PercentageDiscount, NoDiscount, StudentDiscount, SeniorDiscount,
│                    PwdDiscount, PromoDiscount
├── layout/          SeatLayoutStrategy, StandardLayout, PremiumLayout, EconomyLayout
├── exception/       CinemaException + Validation / Login / Booking / Payment / Venue / DataException,
│                    GoBackException (not an error: the user typed /return)
└── util/            Validator (every input rule), ConsoleReader (keyboard input), Format (pesos and dates)
```

## Going back with /return
Type `/return` at any prompt (any letter case, spaces around it are fine) to go back to the previous screen:
- At a menu, it works like `0` (Back). At the customer or admin menu it logs out; at the main menu it shows the main menu again.
- In the middle of a task (registering, logging in, adding a movie, editing a profile...), it leaves the task without
  saving anything and shows the menu the task was started from.
- While booking, it goes back ONE step: payment -> payment method -> ticket type / discount -> seats -> schedule -> movie
  -> customer menu. At the promo code or PWD ID prompt it goes back to the discount list. Seats are only held while paying,
  and are released if you go back, so nothing half-made is ever kept.

How it works: `ConsoleReader` throws `GoBackException` when it reads `/return`; the screen that opened the current one
catches it (`catch (GoBackException e)`) and shows itself again. Menus use `Screen.readMenuChoice`, which turns `/return` into `0`.

## How to debug
| Problem | Look here |
|---|---|
| An input is accepted or rejected wrongly | `util/Validator.java` - every rule and limit is a constant at the top |
| A booking, refund or price is wrong | `service/BookingManager.java` (money is rounded to centavos there) |
| Saved data is missing or wrong | the `data/*.txt` files - each starts with a `#` line naming the columns |
| A payment or refund needs checking | `data/transactions.txt` (one line per payment / refund, never changed) |
| "Something went wrong" appeared | `data/error.log` - the time and the full Java error with the line number |
| A data file was edited by hand and broke | the program skips the bad line, says which one, and keeps a copy as `<file>.bak` |

To start over with fresh sample data, close CURATOR and delete the `data` folder.

## Who does what
| Member | Files |
|---|---|
| A - Accounts & Venues | User, Admin, Customer, Validator, UserManager, AccountMenu, Seat, StandardSeat, VipSeat, CinemaRoom, CinemaBranch, SeatLayoutStrategy + layouts, VenueManager, VenueMenu |
| B - Movies & Showtimes | Movie, Showtime, MovieManager, MovieMenu |
| C - Booking & Promos | Booking, Promo, BookingManager (Singleton), PromoManager, DiscountStrategy + discounts, BookingMenu |
| D - Pricing, Payments, Reports | Ticket, RegularTicket, StudentTicket, PaymentStrategy + payments, ReportService, ConsoleReader, SampleData |

Shared: Main, the `data/` package, RecordsMenu, Screen, SeatMap, Format and the 7 exception classes.

## Requirement checklist
- Encapsulation: private fields + setters that validate (User, Customer, CinemaBranch, CinemaRoom, Movie, Showtime, Booking)
- Abstraction: abstract User, Seat, Ticket and PercentageDiscount, plus the strategy interfaces
- Inheritance: Admin/Customer extend User, StandardSeat/VipSeat extend Seat, RegularTicket/StudentTicket extend Ticket,
  the discounts extend PercentageDiscount
- Polymorphism: getRole(), getDetails(), matchesLogin(), getExtraCharge(), isVipRow(), calculatePrice(), pay(), applyDiscount()
- Exceptions: CinemaException -> Validation / Login / Booking / Payment / Venue / Data. GoBackException carries /return
  back to the previous screen. File errors become DataException,
  unexpected errors are caught in Main and written to `data/error.log` instead of crashing
- Singleton: BookingManager
- Strategy: PaymentStrategy (cash, card, GCash, Maya), DiscountStrategy (promo, senior, PWD, student), SeatLayoutStrategy
- File handling: FileManager saves and loads every record with PrintWriter / Scanner; TransactionLog appends

## Rules for the code
- Keep Scanner / System.out in the `ui` classes; keep `service` classes as plain logic.
- Put new input rules in `Validator` so every screen uses the same limits.
- Use `Screen` for titles, options and messages, `Format.peso(...)` for money and `Format.dateTime(...)` for dates.
- Don't rename methods others use without telling the team.

## Sample data (created on the first run)
- Admin: username `Admin123` (or email `admin@cinema.com`) / password `AdminLogin12`
- Customers: `juan@mail.com` / `Customer1` (born 2005-03-14), `maria@mail.com` / `Customer2` (2007-06-02),
  `jose@mail.com` / `Customer3` (1992-01-20), `lola@mail.com` / `Senior123` (1959-05-10, senior citizen)
- Promo codes: `WELCOME10` (10% off, 180 days), `MOVIENIGHT` (15% off, 90 days) - once per customer
- Branches: SM City Lipa (Cinema 1-3) and Robinsons Place Lipa (Cinema 1-2)
- Now showing: Spider-Man: Brand New Day, Digger, Street Fighter, Clayface, The Social Reckoning, Terrifier 4.
  Coming soon: The Hunger Games: Sunrise on the Reaping, Avengers: Doomsday, Dune: Part Three.
  (Ratings and runtimes are sample values.)
- 23 showtimes for the two days after the first run, PHP 350-400 for a regular seat
- A few seats are already booked by Maria and Jose, so the seat maps look like a real, busy cinema
- Demo card for paying: `4111 1111 1111 1111`, any future expiry (e.g. `12/30`), any 3-digit CVV

## Input rules (all in `util/Validator.java`)
| Field | Rule |
|---|---|
| Menu choice | exactly the number shown, nothing else: one digit (0-9) in every menu. Long lists with 10+ items (e.g. 11 schedules) also take the number shown, like `10`. ` 1`, `1 `, `01`, `+1`, `1.0` and numbers that are not listed are rejected and asked again |
| Name | 2-50 characters; letters (ñ, é... allowed), spaces, and `. ' -` after a letter; extra spaces are removed |
| Email | like `name@example.com`, no `..`, ending of 2+ letters, max 100 characters, saved in lowercase, unique |
| Password | 8-64 characters with an uppercase letter, a lowercase letter and a number; no spaces or `\|` |
| Birthdate | `yyyy-MM-dd`, a real date, not in the future; age 13-100 to have an account |
| Admin username | 4-20 letters or digits, unique |
| Branch / location / room name | not empty; max 40 / 60 / 30 characters; branch names unique, room names unique per branch |
| Room size | 2-26 rows, 4-30 seats per row |
| Movie | title max 60 characters and unique; genre from the list; rating G, PG, R-13, R-16, R-18; 1-600 minutes |
| Showtime | `yyyy-MM-dd HH:mm`, not in the past, at most 365 days ahead, starts 9:00 AM-11:30 PM, no overlap (15-min cleaning gap) |
| Regular seat price | PHP 100.00-2,000.00, at most 2 decimals (VIP seats add PHP 200) |
| Money typed in | digits only, at most 2 decimals (`450`, `450.50`, `1,000`); `NaN`, `-100`, `1e3`, `400f` are asked again |
| Promo code | 3-15 letters/digits, 1-90% off, end date from today to 365 days ahead |
| PWD ID | digits and dashes, 10-25 characters, at least 8 digits (e.g. `04-1014-000-0001234`) |
| Card | 16 digits, Visa (4) or Mastercard (5 / 2), passes the Luhn check, expiry `MM/YY` not in the past, 3-digit CVV |
| GCash / Maya | 11-digit number starting with `09`, then the OTP shown on screen (demo) |
| `\|` character | not allowed in typed text, because it separates the values in the data files |

## Behaviour decisions
- Every screen asks again after a wrong entry; a blank line or `0` cancels; `/return` goes back one screen.
  Yes/no questions accept only y / n.
- Admin menu (one digit each): 1 Registered Users, 2 Booked Tickets / Transactions / Data Files, 3 Accounts,
  4 Venues, 5 Movies, 6 Showtimes, 7 Promo Codes, 8 Reports (revenue / attendance), 9 My Profile, 0 Logout.
- Login: 3 tries per visit; 5 wrong passwords in a row lock the account for 15 minutes (an admin can unlock it).
- Customers can't change their own birthdate (that would get around age ratings and the senior discount);
  an admin can correct it after checking an ID. The age is worked out from the birthdate, so it is always current.
- Booking: movie -> schedule (grouped by branch) -> seats on the seat map -> ticket type and discount ->
  review and pay -> e-ticket with a booking reference (CUR-100001...).
- Seat map: `[A1 ]` regular, `{E1 }` VIP, `[XX ]` taken. VIP seats cost PHP 200 more.
- Price = sum of each seat's price, then ONE discount for the whole booking (student tickets get only the student discount;
  otherwise promo code, senior citizen 60+, or PWD). Prices are VAT-inclusive.
- Seats are held first, then payment is taken (3 attempts). If payment fails, the seats are released and nothing is kept.
- Customers can cancel online until 2 hours before the show (full refund the same way they paid);
  an admin can cancel any booking until the show starts.
- An admin can change a showtime's price (bookings already made keep their price) or cancel a showtime
  (every booking is refunded and the showtime is kept, marked cancelled, for the history).
- A movie, room or branch can't be removed once it has bookings (set the movie to COMING SOON instead).
- A booked seat can't change between Standard and VIP until that show has ended.
- Changing a movie's duration is refused if its showtimes would then overlap other shows in the same room.
- New IDs (U-, M-, S-, BR-, RM-, CUR-) are used only when something is really added, and continue after a restart.
- Passwords are stored in plain text in `data/users.txt` (fine for a class project; hash them for real use).
