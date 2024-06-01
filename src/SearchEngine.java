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

@WebServlet(name = "SearchEngine", urlPatterns = "/api/search")
public class SearchEngine extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try (Connection conn = dataSource.getConnection()){
            JsonArray jsonArray = new JsonArray();
            String queryTitle = request.getParameter("query");
            // Split the queryTitle into individual keywords
            String[] keywords = queryTitle.split("\\s+");
            // Prepend each keyword with the '+' operator
            String searchQuery = "+" + String.join(" +", keywords);
            int dist = (queryTitle.length() + 1) / 4;
            String sqlQuery = "SELECT id, title, " +
                    "MATCH (title) AGAINST (? IN BOOLEAN MODE) as relevance " +
                    "FROM movies " +
                    "WHERE (MATCH (title) AGAINST (? IN BOOLEAN MODE) " +
                    "OR title LIKE ? " +
                    "OR edth(title, ?, ?)) " +
                    "ORDER BY CASE " +
                    "WHEN title = ? THEN 0 " +
                    "WHEN title LIKE ? THEN 1 " +
                    "WHEN title LIKE ? THEN 2 " +
                    "ELSE 3 END, " +
                    "relevance DESC;";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, searchQuery);
            statement.setString(2, searchQuery);
            statement.setString(3, '%' + queryTitle + '%');
            statement.setString(4, queryTitle);
            statement.setInt(5, dist);
            statement.setString(6, queryTitle);
            statement.setString(7, queryTitle + " %");
            statement.setString(8, "% " + queryTitle + '%');
            ResultSet resultSet = statement.executeQuery();

            int returnedMovies = 0;
            while (returnedMovies < 10 && resultSet.next()) {
                String movieId = resultSet.getString("id");
                String movieTitle = resultSet.getString("title");
                jsonArray.add(generateJsonObject(movieId, movieTitle));
                ++returnedMovies;
            }
            String results = jsonArray.toString();
            response.getWriter().write(results);
        } catch (Exception e) {
            System.out.println(e);
            response.sendError(500, e.getMessage());
        }
    }

    private static JsonObject generateJsonObject(String movieId, String movieTitle) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", movieTitle);

        JsonObject additionalDataJsonObject = new JsonObject();
        additionalDataJsonObject.addProperty("movieId", movieId);

        jsonObject.add("data", additionalDataJsonObject);
        return jsonObject;
    }
}
