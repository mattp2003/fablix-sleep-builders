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
            String n = request.getParameter("n");
            if (n != null && !n.trim().isEmpty()){
                max_movies = Integer.parseInt(n);
            }
            String query;
            String searchTitle = request.getParameter("title");
            String searchYear = request.getParameter("year");
            String searchDirector = request.getParameter("director");
            String searchStarName = request.getParameter("star");
//            System.out.println("Title: " + searchTitle + " Year: " + searchYear + " Director: " + searchDirector + " Star's Name: " + searchStarName);
            PreparedStatement statement;

            String sortBy = request.getParameter("sortBy");
            String sortOrder = request.getParameter("sortOrder");
            System.out.println("Sort By: " + sortBy + " SortOrder: " + sortOrder);
            if (sortBy == null || sortOrder == null) {
                sortBy = "rating"; // Default column to sort by
                sortOrder = "desc"; // Default sort order
            }

            boolean isSearched = (searchTitle != null && !searchTitle.isEmpty()) || (searchYear != null && !searchYear.isEmpty())|| (searchDirector != null && !searchDirector.isEmpty()) || (searchStarName != null && !searchStarName.isEmpty());
            StringBuilder queryBuilder;

            if (isSearched){
                System.out.println("Search Mode");
                queryBuilder = new StringBuilder();
                queryBuilder.append("SELECT m.id, m.title, m.year, m.director, ");
                queryBuilder.append("SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name DESC SEPARATOR ', '), ',', 3) as genres, ");
                queryBuilder.append("SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.name DESC SEPARATOR ', '), ',', 3) as stars, ");
                queryBuilder.append("SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.name DESC SEPARATOR ', '), ',', 3) as stars_id, ");
                queryBuilder.append("m.rating ");
                queryBuilder.append("FROM (select distinct movies.id, title, year, director, rating from movies ");
                queryBuilder.append("LEFT JOIN ratings as r ON movies.id = r.movieId ");
                queryBuilder.append("INNER JOIN genres_in_movies gim ON movies.id = gim.movieId ");
                queryBuilder.append("INNER JOIN genres g ON gim.genreId = g.id ");
                queryBuilder.append("WHERE 1=1 ");

                if (searchTitle != null && !searchTitle.isEmpty()) {
                    queryBuilder.append("AND movies.title LIKE ? ");
                }
                if (searchYear != null && !searchYear.isEmpty()) {
                    queryBuilder.append("AND movies.year = ? ");
                }
                if (searchDirector != null && !searchDirector.isEmpty()) {
                    queryBuilder.append("AND movies.director LIKE ? ");
                }
                if (searchStarName != null && !searchStarName.isEmpty()) {
                    queryBuilder.append("AND s.name LIKE ? ");
                }

//                queryBuilder.append("ORDER BY rating DESC LIMIT ? ) as m ");

                if ("title".equals(sortBy)) {
                    queryBuilder.append("ORDER BY title ").append(sortOrder).append(", rating ").append(sortOrder).append(" LIMIT ? ) as m ");
                } else if ("rating".equals(sortBy)) {
                    queryBuilder.append("ORDER BY rating ").append(sortOrder).append(", title ").append(sortOrder).append(" LIMIT ? ) as m ");
                }

                queryBuilder.append("INNER JOIN stars_in_movies sim ON m.id = sim.movieId ");
                queryBuilder.append("INNER JOIN stars s ON sim.starId = s.id ");
                queryBuilder.append("INNER JOIN genres_in_movies gim ON m.id = gim.movieId ");
                queryBuilder.append("INNER JOIN genres g ON gim.genreId = g.id ");
                queryBuilder.append("GROUP BY m.id, m.rating ");

                if ("title".equals(sortBy)) {
                    queryBuilder.append("ORDER BY m.title ").append(sortOrder).append(", m.rating ").append(sortOrder).append(";");
                } else if ("rating".equals(sortBy)) {
                    queryBuilder.append("ORDER BY m.rating ").append(sortOrder).append(", m.title ").append(sortOrder).append(";");
                }

                query = queryBuilder.toString();

                int paramIndex = 1;
                statement = conn.prepareStatement(query);
                if (searchTitle != null && !searchTitle.isEmpty()) {
                    statement.setString(paramIndex++, "%" + searchTitle + "%");
                }
                if (searchYear != null && !searchYear.isEmpty()) {
                    statement.setInt(paramIndex++, Integer.parseInt(searchYear));
                }
                if (searchDirector != null && !searchDirector.isEmpty()) {
                    statement.setString(paramIndex++, "%" + searchDirector + "%");
                }
                if (searchStarName != null && !searchStarName.isEmpty()) {
                    statement.setString(paramIndex++, "%" + searchStarName + "%");
                }
                statement.setInt(paramIndex, max_movies);
            } else {
                System.out.println("Browse Mode");
                String g = request.getParameter("genre");
                if (g != null && !g.trim().isEmpty()){
                    genre = "'" + g + "'";
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
                queryBuilder = new StringBuilder();
                queryBuilder.append("SELECT m.id, m.title, m.year, m.director, ")
                        .append("SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name DESC SEPARATOR ', '), ',', 3) as genres, ")
                        .append("SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.name DESC SEPARATOR ', '), ',', 3) as stars, ")
                        .append("SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.name DESC SEPARATOR ', '), ',', 3) as stars_id, m.rating ")
                        .append("FROM (select distinct movies.id, title, year, director, rating from movies ")
                        .append("LEFT JOIN ratings as r ON movies.id = r.movieId ")
                        .append("INNER JOIN genres_in_movies gim ON movies.id = gim.movieId ")
                        .append("INNER JOIN genres g ON gim.genreId = g.id ");
                if (g != null && !g.trim().isEmpty()) {
                    queryBuilder.append("and g.name = ? ");
                }
                if (sw != null && !sw.trim().isEmpty()) {
                    if ("*".equals(sw)) {
                        queryBuilder.append("and title not regexp '^[A-Za-z0-9].*$' ");
                    } else {
                        queryBuilder.append("and movies.title LIKE ? ");
                    }
                }

//                queryBuilder.append("ORDER BY rating DESC LIMIT ? ) as m ");
                if ("title".equals(sortBy)) {
                    queryBuilder.append("ORDER BY title ").append(sortOrder).append(", rating ").append(sortOrder).append(" LIMIT ? ) as m ");
                } else if ("rating".equals(sortBy)) {
                    queryBuilder.append("ORDER BY rating ").append(sortOrder).append(", title ").append(sortOrder).append(" LIMIT ? ) as m ");
                }

                queryBuilder.append("INNER JOIN stars_in_movies sim ON m.id = sim.movieId ")
                        .append("INNER JOIN stars s ON sim.starId = s.id ")
                        .append("INNER JOIN genres_in_movies gim ON m.id = gim.movieId ")
                        .append("INNER JOIN genres g ON gim.genreId = g.id ")
                        .append("GROUP BY m.id, m.rating ");

                if ("title".equals(sortBy)) {
                    queryBuilder.append("ORDER BY m.title ").append(sortOrder).append(", m.rating ").append(sortOrder).append(";");
                } else if ("rating".equals(sortBy)) {
                    queryBuilder.append("ORDER BY m.rating ").append(sortOrder).append(", m.title ").append(sortOrder).append(";");
                }

                query = queryBuilder.toString();


//                String query = queryBuilder.toString();
//                query = "SELECT m.id, m.title, m.year, m.director, " +
//                        "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name DESC SEPARATOR ', '), ',', 3) as genres, " +
//                        "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.name DESC SEPARATOR ', '), ',', 3) as stars, " +
//                        "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.name DESC SEPARATOR ', '), ',', 3) as stars_id, m.rating " +
//                        "FROM (select distinct movies.id, title, year, director, rating from movies " +
//                        "LEFT JOIN ratings as r ON movies.id = r.movieId " +
//                        "INNER JOIN genres_in_movies gim ON movies.id = gim.movieId " +
//                        "INNER JOIN genres g ON gim.genreId = g.id and g.name = " + genre + " and movies.title LIKE " + startsWith +  " " +
//                        "ORDER BY rating DESC LIMIT " + max_movies + " ) as m " +
//                        "INNER JOIN stars_in_movies sim ON m.id = sim.movieId " +
//                        "INNER JOIN stars s ON sim.starId = s.id " +
//                        "INNER JOIN genres_in_movies gim ON m.id = gim.movieId " +
//                        "INNER JOIN genres g ON gim.genreId = g.id " +
//                        "GROUP BY m.id, m.rating " +
//                        "ORDER BY m.rating desc;\n";
                statement = conn.prepareStatement(query);
                int paramIndex = 1;
                if (g != null && !g.trim().isEmpty()) {
                    statement.setString(paramIndex++, g);
                }
                if (sw != null && !"*".equals(sw)) {
                    statement.setString(paramIndex++, sw + "%");
                }
                statement.setInt(paramIndex, max_movies);
            }

            //String query = "SELECT m.id, m.title, m.year, m.director, SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT m.name ORDER BY m.name DESC SEPARATOR ', '), ',', 3) as genres, SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.name DESC SEPARATOR ', '), ',', 3) as stars, SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.name DESC SEPARATOR ', '), ',', 3) as stars_id, m.rating FROM (select movies.id, title, year, director, rating, genreId, name from movies LEFT JOIN ratings as r ON movies.id = r.movieId INNER JOIN genres_in_movies gim ON movies.id = gim.movieId INNER JOIN genres g ON gim.genreId = g.id and g.name = " + genre + " ORDER BY rating DESC LIMIT " + max_movies + " ) as m INNER JOIN stars_in_movies sim ON m.id = sim.movieId INNER JOIN stars s ON sim.starId = s.id GROUP BY m.id, m.rating ORDER BY \tm.rating desc;";
            // Declare our statement
            //statement.setString(1, genre);

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
