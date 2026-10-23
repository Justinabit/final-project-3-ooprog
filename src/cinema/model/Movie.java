package cinema.model;

import cinema.exception.ValidationException;
import cinema.util.Validator;

// [Member B] A movie in the catalog
public class Movie {
    // Column layout for movie tables (ID, TITLE, GENRE, RATING, RUNTIME, STATUS)
    public static final String TABLE_FORMAT = "%-5s %-28s %-9s %-5s %-7s %s";

    private String movieId;
    private String title;
    private String genre;
    private String ageRating;    // MTRCB rating: "G", "PG", "R-13", "R-16", "R-18"
    private int durationMinutes;
    private String status;       // "NOW SHOWING" or "COMING SOON"

    public Movie(String movieId, String title, String genre, String ageRating, int durationMinutes, String status)
            throws ValidationException {
        this.movieId = movieId;
        setTitle(title);
        setGenre(genre);
        setAgeRating(ageRating);
        setDurationMinutes(durationMinutes);
        setStatus(status);
    }

    public String getMovieId() { return movieId; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public String getAgeRating() { return ageRating; }
    public int getDurationMinutes() { return durationMinutes; }
    public String getStatus() { return status; }

    public void setTitle(String title) throws ValidationException {
        Validator.validateTitle(title);
        this.title = Validator.cleanSpaces(title);
    }

    // Must be one of Validator.GENRES
    public void setGenre(String genre) throws ValidationException {
        this.genre = Validator.validateGenre(genre);
    }

    public void setAgeRating(String ageRating) throws ValidationException {
        String r = ageRating == null ? null : ageRating.trim().toUpperCase();
        Validator.validateAgeRating(r);
        this.ageRating = r;
    }

    public void setDurationMinutes(int durationMinutes) throws ValidationException {
        Validator.validateRange("Duration (minutes)", durationMinutes, 1, 600);
        this.durationMinutes = durationMinutes;
    }

    public void setStatus(String status) throws ValidationException {
        String s = status == null ? null : status.trim().toUpperCase();
        Validator.validateMovieStatus(s);
        this.status = s;
    }

    // Minimum customer age for this movie's rating
    public int getMinimumAge() {
        switch (ageRating) {
            case "R-18": return 18;
            case "R-16": return 16;
            case "R-13": return 13;
            default: return 0;
        }
    }

    // 140 -> "2h 20m", 125 -> "2h 05m"
    public String getRuntime() {
        int hours = durationMinutes / 60;
        int minutes = durationMinutes % 60;
        return String.format("%dh %02dm", hours, minutes);
    }

    // true if this movie can be booked right now
    public boolean isNowShowing() {
        return "NOW SHOWING".equals(status);
    }

    // true if a customer of this age is allowed to watch (MTRCB rating)
    public boolean isAllowedForAge(int age) {
        return age >= getMinimumAge();
    }

    @Override
    public String toString() {
        String shownTitle = title;
        if (shownTitle.length() > 28) {
            shownTitle = shownTitle.substring(0, 25) + "...";   // keep the table columns lined up
        }
        return String.format(TABLE_FORMAT, movieId, shownTitle, genre, ageRating, getRuntime(), status);
    }
}