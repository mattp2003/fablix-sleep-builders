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
            queryTitle = queryTitle.replace(" ", "* ");
            queryTitle += "*";
            int dist = (queryTitle.length()+1)/2;
            String sqlQuery = "SELECT id, title FROM movies WHERE (MATCH (title) AGAINST ('" + queryTitle + "' IN BOOLEAN MODE) OR title LIKE \"%" + request.getParameter("query") + "%\" OR edth(title, \"" + request.getParameter("query") + "\", " + dist +"));";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            ResultSet resultSet = statement.executeQuery();

            int returnedMovies = 0;
            while (returnedMovies < 10 && resultSet.next()) {
                String movieId = resultSet.getString("id");
                String movieTitle = resultSet.getString("title");
                jsonArray.add(generateJsonObject(movieId, movieTitle));
                ++returnedMovies;
            }
            String results = jsonArray.toString();
            System.out.println(results);
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
