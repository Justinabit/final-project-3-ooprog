// [Member B] A movie in the catalog
public class Movie {
    private String movieId;
    private String title;
    private String genre;        // TODO (B): you can change this to an enum Genre
    private String ageRating;    // "G", "PG", "R-16", "R-18"
    private int durationMinutes;
    private String status;       // "NOW SHOWING" or "COMING SOON"

    public Movie(String movieId, String title, String genre, String ageRating, int durationMinutes, String status) {
        this.movieId = movieId;
        this.title = title;
        this.genre = genre;
        this.ageRating = ageRating;
        this.durationMinutes = durationMinutes;
        this.status = status;
    }

    // TODO (B): getters and setters (validate title is not empty, duration > 0)
}
