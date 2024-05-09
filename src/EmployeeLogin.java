import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@WebServlet(name = "EmployeeLoginServlet", urlPatterns = "/api/employeeLogin")
public class EmployeeLogin extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
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
        response.setContentType("application/json"); // Response mime type
        JsonObject responseJsonObject = new JsonObject();

        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);

        } catch (Exception e) {
            responseJsonObject.addProperty("status", "fail");
            request.getServletContext().log("Login failed");
            responseJsonObject.addProperty("message", "recaptcha-failed");
            response.getWriter().write(responseJsonObject.toString());
            return;
        }


        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()){
            String query = "SELECT password from employees where email=?;";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();

            boolean user_exists = rs.next();

            if (!user_exists){
                responseJsonObject.addProperty("status", "fail");
                request.getServletContext().log("Login failed");
                responseJsonObject.addProperty("message", "user with email " + email + " doesn't exist");
            }
            else{
                String queriedPassword = rs.getString("password");
                if (password.equals(queriedPassword)){
                    request.getSession().setAttribute("user", new User(email));
                    request.getSession().setAttribute("isEmployee", true);
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                }
                else{
                    responseJsonObject.addProperty("status", "fail");
                    request.getServletContext().log("Login failed");
                    responseJsonObject.addProperty("message", "incorrect password");
                }
            }
            response.getWriter().write(responseJsonObject.toString());
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

    }
}
