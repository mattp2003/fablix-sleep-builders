import java.util.ArrayList;

public class Movie {
    private final String title;
    private final String year;
    private final String director;
    private ArrayList<String> genres;

    public Movie (String title, String year, String director, ArrayList<String> genres){
        this.title = title;
        this.year = year;
        this.director = director;
        this.genres = genres;
    }

    public String getTitle() {
        return title;
    }

    public String getYear(){
        return year;
    }

    public String getDirector(){
        return director;
    }

    public ArrayList<String> getGenres() {
        return genres;
    }

}
