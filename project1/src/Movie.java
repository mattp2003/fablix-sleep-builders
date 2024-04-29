import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;


@WebServlet(name = "Movie", urlPatterns = "/api/movie")
public class Movie extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String id = request.getParameter("id");
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            //get movie details
            String movieDataQuery = "SELECT id, rating, numVotes, title, year, director from ratings, movies where movies.id = ? and ratings.movieId = movies.id;";

            PreparedStatement movieStatement = conn.prepareStatement(movieDataQuery);
            movieStatement.setString(1, id);

            ResultSet rs = movieStatement.executeQuery();

            rs.next();

            JsonObject result = new JsonObject();

            result.addProperty("rating", rs.getString("rating"));
            result.addProperty("title", rs.getString("title"));
            result.addProperty("year", rs.getString("year"));
            result.addProperty("director", rs.getString("director"));



            //get movie stars
            String starsQuery = "select name, starId from (select name, s.starId, count(name) as c from (select starId, name from stars_in_movies, stars where movieId = ? and starId = stars.id)s inner join stars_in_movies sim where sim.starId = s.starId group by name, s.starId order by c desc, name asc)a;";

            PreparedStatement starsStatement = conn.prepareStatement(starsQuery);
            starsStatement.setString(1, id);

            ResultSet starrs = starsStatement.executeQuery();

            JsonArray stars = new JsonArray();
            while (starrs.next()){
                JsonObject star = new JsonObject();

                star.addProperty("starId", starrs.getString("starId"));
                star.addProperty("name", starrs.getString("name"));

                stars.add(star);
            }



            //get movie genres
            String genresQuery = "select genreId, name from genres_in_movies, genres where movieId = ? and genreId = genres.id;";

            PreparedStatement genresStatement = conn.prepareStatement(genresQuery);
            genresStatement.setString(1, id);

            ResultSet genrers = genresStatement.executeQuery();

            JsonArray genres = new JsonArray();
            while (genrers.next()){
                JsonObject genre = new JsonObject();

                genre.addProperty("genreId", genrers.getString("genreId"));
                genre.addProperty("name", genrers.getString("name"));

                genres.add(genre);
            }




            // Write JSON string to output
            result.addProperty("stars", stars.toString());
            result.addProperty("genres", genres.toString());
            out.write(result.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}
