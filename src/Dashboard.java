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
import java.sql.*;
import java.util.*;

@WebServlet(name = "Dashboard", urlPatterns = "/api/dashboard")
public class Dashboard extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    // Implement doPost to handle POST requests
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Set the response content type to JSON
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String type = request.getParameter("type");
        try {
            JsonObject resultMessage;
            if ("addStar".equals(type)) {
                String starName = request.getParameter("starName");
                String birthYearString = request.getParameter("birthYear");
                Integer birthYear = null;
                System.out.println("Parameters: " + starName + " " + birthYearString);
                if (birthYearString != null && !birthYearString.trim().isEmpty()) {
                    birthYear = Integer.parseInt(birthYearString);
                }
                resultMessage = addStarToDatabase(starName, birthYear);
            } else if ("addMovie".equals(type)) {
                String movieName = request.getParameter("movieName");
                String movieYearString = request.getParameter("movieYear");
                Integer movieYear = null;
                if (movieYearString != null && !movieYearString.trim().isEmpty()) {
                    movieYear = Integer.parseInt(movieYearString);
                }
                String director = request.getParameter("director");
                String starName = request.getParameter("starName");
                String genre = request.getParameter("genre");
                System.out.println("Update: " + movieName + " " + movieYear + " " + director + " " + starName + " " + genre);
                resultMessage = addMovieToDatabase(movieName, movieYear, director, starName, genre);
            } else {
                // Handle other types or throw an error
                resultMessage = new JsonObject();
                resultMessage.addProperty("message", "Invalid type specified.");
            }
            out.write(resultMessage.toString());
            response.setStatus(200); // OK status
        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObjectError = new JsonObject();
            jsonObjectError.addProperty("errorMessage", e.getMessage());
            out.write(jsonObjectError.toString());
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
    }

    private String getNewStarId(Connection connection) throws SQLException {
        String query = "SELECT id FROM stars ORDER BY id DESC LIMIT 1;";
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        String lastId = resultSet.getString("id");
        String prefix = "nm";
        int numericPart = Integer.parseInt(lastId.substring(prefix.length()));
        numericPart++;
        String newId = prefix + String.format("%07d", numericPart);
        System.out.println("New Star's ID: " + newId);
        return newId;
    }

    private String getNewMovieId(Connection connection) throws SQLException {
        String query = "SELECT id FROM movies ORDER BY id DESC LIMIT 1;";
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        String lastId = resultSet.getString("id");
        String prefix = "tt";
        int numericPart = Integer.parseInt(lastId.substring(prefix.length()));
        numericPart++;
        String newId = prefix + String.format("%07d", numericPart);
        System.out.println("New movie's ID: " + newId);
        return newId;
    }

    private Integer getNewGenreId(Connection connection) throws SQLException {
        String query = "SELECT MAX(id) + 1 AS id FROM genres;";
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        Integer newId = Integer.parseInt(resultSet.getString("id"));
        System.out.println("New Genre's ID: " + newId);
        return newId;
    }

    private JsonObject addStarToDatabase(String starName, Integer birthYear) {
        JsonObject jsonObject = new JsonObject();
        try (Connection conn = dataSource.getConnection()) {
            // Check if star already exists
            String starExistsQuery = "SELECT name, birthYear FROM stars WHERE name = '" + starName + "' AND birthYear = " + birthYear +";";
            PreparedStatement checkStatement = conn.prepareStatement(starExistsQuery);
            ResultSet resultSet = checkStatement.executeQuery();
            boolean doesExist = resultSet.next();
            if (doesExist) {
                jsonObject.addProperty("success", false);
                jsonObject.addProperty("message", "Star already exists in the database.");
            } else {
                String starQuery = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
                String newStarId = getNewStarId(conn);
                PreparedStatement statement = conn.prepareStatement(starQuery);
                if ("-1".equals(newStarId)) return new JsonObject();
                statement.setString(1, newStarId);
                statement.setString(2, starName);
                if (birthYear != null) {
                    statement.setInt(3, birthYear);
                } else {
                    statement.setNull(3, Types.INTEGER);
                }
                int rowsAffected = statement.executeUpdate();
                jsonObject.addProperty("success", rowsAffected > 0);
            }
            return jsonObject;
        } catch (Exception e) {
            jsonObject.addProperty("message", e.getMessage());
            return jsonObject;
        }
    }

    private JsonObject addMovieToDatabase(String movieName, Integer movieYear, String director, String starName, String genre) {
        JsonObject jsonObject = new JsonObject();
        try (Connection conn = dataSource.getConnection()) {
            String sqlCall = "{CALL add_movie(?, ?, ?, ?, ?, ?, ?, ?)}";
            try (CallableStatement statement = conn.prepareCall(sqlCall)) {
                String newMovieId = getNewMovieId(conn);
                statement.setString(1, newMovieId);
                statement.setString(2, movieName);
                if (movieYear != null) {
                    statement.setInt(3, movieYear);
                } else {
                    statement.setNull(3, Types.INTEGER);
                }
                statement.setString(4, director);
                String newStarId = getNewStarId(conn);
                statement.setString(5, newStarId);
                statement.setString(6, starName);
                int newGenreId = getNewGenreId(conn);
                statement.setInt(7, newGenreId);
                statement.setString(8, genre);
                int rowsAffected = statement.executeUpdate();
                jsonObject.addProperty("success", rowsAffected > 0);
                return jsonObject;
            } catch (Exception e) {
                jsonObject.addProperty("message", "Procedure's Error: " + e.getMessage());
                return jsonObject;
            }
        } catch (Exception e) {
            jsonObject.addProperty("message", e.getMessage());
            return jsonObject;
        }
    }
}
