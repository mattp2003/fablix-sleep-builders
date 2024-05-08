import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.transform.Result;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;

/**
 * This IndexServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/index.
 */
@WebServlet(name = "PayServlet", urlPatterns = "/api/pay")
public class Pay extends HttpServlet {
    /**
     * handles POST requests to add and show the item list information
     */
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        long lastAccessTime = session.getLastAccessedTime();

        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("sessionID", sessionId);
        responseJsonObject.addProperty("lastAccessTime", new Date(lastAccessTime).toString());

        PrintWriter out = response.getWriter();

        String fName = request.getParameter("firstName");
        String lName = request.getParameter("lastName");
        String creditCard = request.getParameter("creditCard");
        String date = request.getParameter("date");

        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        int day = today.getDayOfMonth();

        String d = year + "-" + month + "-" + day;

        try (Connection conn = dataSource.getConnection()){
            String query = "SELECT id, firstName, lastName, expiration from creditcards where id=?;";
            // PreparedStatement checked!
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, creditCard);
            ResultSet rs = statement.executeQuery();

            boolean ccExists = rs.next();

            Map<String, Map<String, Double>> cart = (Map<String, Map<String, Double>>) session.getAttribute("cart");
            Map<String, String> ids = (Map<String, String>) session.getAttribute("ids");
            if (cart == null) {
                cart = new TreeMap<>();
            }
            if (ids == null){
                ids = new HashMap<>();
            }

            if (cart.isEmpty()){
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "no items in cart");
            }
            else if (ccExists && rs.getString("firstName").equals(fName) && rs.getString("lastName").equals(lName) && rs.getString("expiration").equals(date)) {
                Statement s = conn.createStatement();
                for (Map.Entry<String, Map<String, Double>> entry : cart.entrySet()) {
                    String title = entry.getKey();
                    String movieId = ids.get(title);
                    String customerId = rs.getString("id");

                    for (int i = 0; i < entry.getValue().get("quantity"); i++){
                        s.addBatch("INSERT INTO sales(customerId, movieId, saleDate) values( \"" + customerId + "\" ,  \"" + movieId + "\" , \"" + d + "\")");
                    }
                }
                s.executeBatch();


                cart = new TreeMap<>();
                ids = new HashMap<>();

                session.setAttribute("cart", cart);
                session.setAttribute("ids", ids);

                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");
            }
            else{
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "incorrect credentials");
            }

            response.getWriter().write(responseJsonObject.toString());
        } catch (Exception e) {

            responseJsonObject.addProperty("status", "fail");
            request.getServletContext().log("Login failed");
            responseJsonObject.addProperty("message", "error querying movies");

            response.getWriter().write(responseJsonObject.toString());
        } finally {
            out.close();
        }

        response.getWriter().write(responseJsonObject.toString());
    }
}
