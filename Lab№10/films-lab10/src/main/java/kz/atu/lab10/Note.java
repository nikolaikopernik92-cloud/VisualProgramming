package kz.atu.lab10;

import java.util.List;

/**
 * Одна запись списка фильмов.
 * title - название, director - режиссёр, cast - актёры, rating - оценка 0..10 (0 = без оценки),
 * status - статус просмотра, body - краткое описание.
 */
public class Note {
    public static final String WANT = "Хочу посмотреть";
    public static final String WATCHING = "Смотрю";
    public static final String WATCHED = "Просмотрено";
    public static final List<String> STATUSES = List.of(WANT, WATCHING, WATCHED);

    private String title;
    private String director;
    private String cast;
    private int rating;
    private String status;
    private String body;

    public Note() {}

    public Note(String title, String director, String cast, int rating, String status, String body) {
        this.title = title;
        this.director = director;
        this.cast = cast;
        this.rating = rating;
        this.status = status;
        this.body = body;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    public String getCast() { return cast; }
    public void setCast(String cast) { this.cast = cast; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    @Override
    public String toString() { return title + "  [" + status + "]"; }
}
