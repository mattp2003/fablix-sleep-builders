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


// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "Movies", urlPatterns = "/api/movies")
public class Movies extends HttpServlet {
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
        response.setContentType("application/json"); // Response mime type

        //set parameters default values
        int max_movies = 20;
        String genre = "g.name";
        String startsWith = "'%'";

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            //replace parameters if in request query
            String g = request.getParameter("genre");
            if (g != null){
                genre = "'" + g + "'";
            }

            String n = request.getParameter("n");
            if (n != null){
                max_movies = Integer.parseInt(n);
            }

            String sw = request.getParameter("startsWith");
            if (sw != null){
                if (sw.equals("*")){
                    startsWith += " and title not regexp '^[A-Za-z0-9].*$' ";
                }
                else{
                    startsWith = "'" + sw + "%'";
                }
            }

            //String query = "SELECT m.id, m.title, m.year, m.director, SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT m.name ORDER BY m.name DESC SEPARATOR ', '), ',', 3) as genres, SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.name DESC SEPARATOR ', '), ',', 3) as stars, SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.name DESC SEPARATOR ', '), ',', 3) as stars_id, m.rating FROM (select movies.id, title, year, director, rating, genreId, name from movies LEFT JOIN ratings as r ON movies.id = r.movieId INNER JOIN genres_in_movies gim ON movies.id = gim.movieId INNER JOIN genres g ON gim.genreId = g.id and g.name = " + genre + " ORDER BY rating DESC LIMIT " + max_movies + " ) as m INNER JOIN stars_in_movies sim ON m.id = sim.movieId INNER JOIN stars s ON sim.starId = s.id GROUP BY m.id, m.rating ORDER BY \tm.rating desc;";
            String query = "SELECT m.id, m.title, m.year, m.director, SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name DESC SEPARATOR ', '), ',', 3) as genres, SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.name DESC SEPARATOR ', '), ',', 3) as stars, SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.name DESC SEPARATOR ', '), ',', 3) as stars_id, m.rating FROM (select distinct movies.id, title, year, director, rating from movies LEFT JOIN ratings as r ON movies.id = r.movieId INNER JOIN genres_in_movies gim ON movies.id = gim.movieId INNER JOIN genres g ON gim.genreId = g.id and g.name = " + genre + " and movies.title LIKE " + startsWith +  " ORDER BY rating DESC LIMIT " + max_movies + " ) as m INNER JOIN stars_in_movies sim ON m.id = sim.movieId INNER JOIN stars s ON sim.starId = s.id INNER JOIN genres_in_movies gim ON m.id = gim.movieId INNER JOIN genres g ON gim.genreId = g.id GROUP BY m.id, m.rating ORDER BY m.rating desc;\n";
            //System.out.println(query);
            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);
            //statement.setString(1, genre);

            String s = statement.toString();
            // Perform the query
            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movieID = rs.getString("id");
                String movieTitle = rs.getString("title");
                String year = rs.getString("year");
                String director = rs.getString("director");
                String stars = rs.getString("stars");
                String stars_id = rs.getString("stars_id");
                String genres = rs.getString("genres");
                double rating = rs.getDouble("rating");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", movieID);
                jsonObject.addProperty("title", movieTitle);
                jsonObject.addProperty("year", year);
                jsonObject.addProperty("director", director);
                jsonObject.addProperty("stars", stars);
                jsonObject.addProperty("stars_id", stars_id);
                jsonObject.addProperty("genres", genres);
                jsonObject.addProperty("rating", rating);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
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
