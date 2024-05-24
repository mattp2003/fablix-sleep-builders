import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        HttpSession session = request.getSession();
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        JsonObject JSONresult = new JsonObject();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            //load parameters from history
            Map<String, String> parameters;
            parameters = (Map<String,String>) session.getAttribute("parameters");

            //check if searched or browsed, means to reset the parameters
            boolean newQuery = request.getParameter("genre") != null || request.getParameter("startsWith") != null ||
                    request.getParameter("title") != null || request.getParameter("year") != null || request.getParameter("director") != null ||
                    request.getParameter("star") != null;

            //create default parameters if no parameters
            if (newQuery || parameters == null) {
                parameters = new HashMap<String, String>();

                //default parameters
                parameters.put("max_movies", "10");
                parameters.put("page", "1");
                parameters.put("sortBy", "rating");
                parameters.put("ratingOrder", "desc");
                parameters.put("titleOrder", "asc");

                session.setAttribute("parameters", parameters);
            }

            //first load all history parameters
            int max_movies = Integer.parseInt(parameters.get("max_movies"));
            int currentPage = Integer.parseInt(parameters.get("page"));
            String genre = parameters.get("genre"); //"g.name";
            String startsWith = parameters.get("startsWith");//"'%'";
            String searchTitle = parameters.get("searchTitle");
            String searchYear = parameters.get("searchYear");
            String searchDirector = parameters.get("searchDirector");
            String searchStarName = parameters.get("searchStarName");
            String sortBy = parameters.get("sortBy");
            String ratingOrder = parameters.get("ratingOrder");
            String titleOrder = parameters.get("titleOrder");
            int offset; //calculated later

            //replace parameters with the current request to override history
            String requestN = request.getParameter("n");
            if (requestN != null && !requestN.trim().isEmpty()){
                max_movies = Integer.parseInt(requestN);
            }
            String requestCurrentPage = request.getParameter("page");
            if (requestCurrentPage != null && !requestCurrentPage.trim().isEmpty()){
                currentPage = Integer.parseInt(requestCurrentPage);
                offset = max_movies * (currentPage - 1);
            }
            offset = max_movies * (currentPage - 1); //calculate offset after finding final values for N per page, and current page
            String requestGenre = request.getParameter("genre");
            if (requestGenre != null && !requestGenre.trim().isEmpty()){
                genre = requestGenre;
            }
            String requestStartsWith = request.getParameter("startsWith");
            if (requestStartsWith != null && !requestStartsWith.trim().isEmpty()){
                startsWith = requestStartsWith;
            }
            String requestSearchTitle = request.getParameter("title");
            if (requestSearchTitle != null && !requestSearchTitle.trim().isEmpty()){
                searchTitle = requestSearchTitle;
            }
            String requestSearchYear = request.getParameter("year");
            if (requestSearchYear != null && !requestSearchYear.trim().isEmpty()){
                searchYear = requestSearchYear;
            }
            String requestSearchDirector = request.getParameter("director");
            if (requestSearchDirector != null && !requestSearchDirector.trim().isEmpty()){
                searchDirector = requestSearchDirector;
            }
            String requestSearchStarName = request.getParameter("star");
            if (requestSearchStarName != null && !requestSearchStarName.trim().isEmpty()){
                searchStarName = requestSearchStarName;
            }
            String requestSortBy = request.getParameter("sortBy");
            if (requestSortBy != null && !requestSortBy.trim().isEmpty()){
                sortBy = requestSortBy;
            }
            String requestRatingOrder = request.getParameter("ratingOrder");
            if (requestRatingOrder != null && !requestRatingOrder.trim().isEmpty()){
                ratingOrder = requestRatingOrder;
            }
            String requestTitleOrder = request.getParameter("titleOrder");
            if (requestTitleOrder != null && !requestTitleOrder.trim().isEmpty()){
                titleOrder = requestTitleOrder;
            }

            parameters.put("max_movies", Integer.toString(max_movies));
            parameters.put("page", Integer.toString(currentPage));
            parameters.put("genre", genre);
            parameters.put("startsWith", startsWith);
            parameters.put("searchTitle", searchTitle);
            parameters.put("searchYear", searchYear);
            parameters.put("searchDirector", searchDirector);
            parameters.put("searchStarName", searchStarName);
            parameters.put("sortBy", sortBy);
            parameters.put("ratingOrder", ratingOrder);
            parameters.put("titleOrder", titleOrder);


            //build query
            String query;
            StringBuilder queryBuilder;

            queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT m.id, m.title, m.year, m.director, ");
            queryBuilder.append("SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name DESC SEPARATOR ', '), ',', 3) as genres, ");
            queryBuilder.append("SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.name DESC SEPARATOR ', '), ',', 3) as stars, ");
            queryBuilder.append("SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.name DESC SEPARATOR ', '), ',', 3) as stars_id, ");
            queryBuilder.append("m.rating ");
            queryBuilder.append("FROM (select distinct movies.id, title, year, director, rating from movies ");
            queryBuilder.append("LEFT JOIN ratings as r ON movies.id = r.movieId ");
            queryBuilder.append("LEFT JOIN genres_in_movies gim ON movies.id = gim.movieId ");
            queryBuilder.append("LEFT JOIN genres g ON gim.genreId = g.id ");
            queryBuilder.append("LEFT JOIN stars_in_movies sim ON movies.id = sim.movieId ");
            queryBuilder.append("LEFT JOIN stars s ON sim.starId = s.id ");
            queryBuilder.append("WHERE 1=1 ");


            //search parameters
            if (searchTitle != null && !searchTitle.isEmpty()) {
                queryBuilder.append("AND MATCH (movies.title) AGAINST (? IN BOOLEAN MODE) ");
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

            //browse parameters
            if (genre != null && !genre.trim().isEmpty()) {
                queryBuilder.append("AND g.name = \"").append(genre).append("\" ");
            }
            if (startsWith != null && !startsWith.trim().isEmpty()){
                if (startsWith.equals("*")){
                    startsWith += "AND  title not regexp '^[A-Za-z0-9].*$' ";
                }
                else{
                    startsWith = "AND title like '" + startsWith + "%' ";
                }
                queryBuilder.append(startsWith);
            }

            if ("title".equals(sortBy)) {
                queryBuilder.append("ORDER BY title ").append(titleOrder).append(", rating ").append(ratingOrder).append(" LIMIT ").append(max_movies + 1).append(" OFFSET ").append(offset).append(") as m ");
            } else if ("rating".equals(sortBy)) {
                queryBuilder.append("ORDER BY rating ").append(ratingOrder).append(", title ").append(titleOrder).append(" LIMIT ").append(max_movies + 1).append(" OFFSET ").append(offset).append(") as m ");
            }

            queryBuilder.append("LEFT JOIN stars_in_movies sim ON m.id = sim.movieId ");
            queryBuilder.append("LEFT JOIN stars s ON sim.starId = s.id ");
            queryBuilder.append("LEFT JOIN genres_in_movies gim ON m.id = gim.movieId ");
            queryBuilder.append("LEFT JOIN genres g ON gim.genreId = g.id ");
            queryBuilder.append("GROUP BY m.id, m.rating ");

            if ("title".equals(sortBy)) {
                queryBuilder.append("ORDER BY title ").append(titleOrder).append(", rating ").append(ratingOrder);
            } else if ("rating".equals(sortBy)) {
                queryBuilder.append("ORDER BY rating ").append(ratingOrder).append(", title ").append(titleOrder);
            }

            query = queryBuilder.toString();
            int paramIndex = 1;
            // PreparedStatement checked!
            PreparedStatement statement = conn.prepareStatement(query);
            if (searchTitle != null && !searchTitle.isEmpty()) {
                statement.setString(paramIndex, searchTitle);
                paramIndex++;
            }
            if (searchYear != null && !searchYear.isEmpty()) {
                statement.setInt(paramIndex, Integer.parseInt(searchYear));
                paramIndex++;
            }
            if (searchDirector != null && !searchDirector.isEmpty()) {
                statement.setString(paramIndex, "%" + searchDirector + "%");
                paramIndex++;
            }
            if (searchStarName != null && !searchStarName.isEmpty()) {
                statement.setString(paramIndex, "%" + searchStarName + "%");
            }
            //statement.setInt(paramIndex++, max_movies + 1); //add 1 to check if there are elements on page after
            //statement.setInt(paramIndex, offset);
            //finish build query

            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            int cReturnedMovies = 0;
            while (cReturnedMovies < max_movies && rs.next()) {
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
                cReturnedMovies+=1;
            }
            boolean hasNext = rs.next(); //has an extra element above maximum elements, and therefore has next page
            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            JSONresult.add("movies", jsonArray);
            JSONresult.addProperty("page", currentPage);
            JSONresult.addProperty("hasNext", hasNext);
            String front_end_rs = JSONresult.toString();
            response.getWriter().write(front_end_rs);
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
