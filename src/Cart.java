import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

/**
 * This IndexServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/index.
 */
@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class Cart extends HttpServlet {

    /**
     * handles GET requests to store session information
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        long lastAccessTime = session.getLastAccessedTime();

        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("sessionID", sessionId);
        responseJsonObject.addProperty("lastAccessTime", new Date(lastAccessTime).toString());

        Map<String, Map<String, Double>> cart = (Map<String, Map<String, Double>>) session.getAttribute("cart");
        Map<String, String> ids = (Map<String, String>) session.getAttribute("ids");
        if (cart == null) {
            cart = new TreeMap<>();
            session.setAttribute("cart", cart);
        }
        if (ids == null){
            ids = new HashMap<>();
            session.setAttribute("ids", ids);
        }
        // Log to localhost log
        request.getServletContext().log("getting " + cart.size() + " items");
        JsonArray cartArray = new JsonArray();

        for (Map.Entry<String, Map<String, Double>> entry : cart.entrySet()) {
            JsonObject jsonItem = new JsonObject();
            jsonItem.addProperty("title", entry.getKey());
            jsonItem.addProperty("quantity", entry.getValue().get("quantity"));
            jsonItem.addProperty("price", entry.getValue().get("price"));
            jsonItem.addProperty("movieId", ids.get(entry.getKey()));
            cartArray.add(jsonItem);
            //System.out.println(jsonItem);
        }

        //System.out.println(cartArray);
        responseJsonObject.add("cart", cartArray);
        System.out.println(responseJsonObject);
        // write all the data into the jsonObject
        response.getWriter().write(responseJsonObject.toString());
    }

    /**
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        long lastAccessTime = session.getLastAccessedTime();

        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("sessionID", sessionId);
        responseJsonObject.addProperty("lastAccessTime", new Date(lastAccessTime).toString());

        String title = request.getParameter("title");
        String action = request.getParameter("action");
        String id = request.getParameter("id");

        Map<String, Map<String, Double>> cart = (Map<String, Map<String, Double>>) session.getAttribute("cart");
        Map<String, String> ids = (Map<String, String>) session.getAttribute("ids");
        if (cart == null) {
            cart = new TreeMap<>();
        }
        if (ids == null){
            ids = new HashMap<>();
        }

        synchronized (cart) {
            switch (action) {
                case "increase":
                    if (cart.containsKey(title)){
                        Map<String, Double> m = cart.get(title);
                        cart.get(title).put("quantity", m.get("quantity") + 1);
                    }
                    else{
                        Map<String, Double> m = new HashMap<>();
                        m.put("quantity", 1.0);
                        //System.out.println(title.hashCode());
                        m.put("price", (double) (Math.abs(title.hashCode()) % 9 + 1));
                        cart.put(title, m);
                        ids.put(title, id);
                    }
                    break;
                case "decrease":
                    Map<String, Double> m = cart.get(title);
                    cart.get(title).put("quantity", m.get("quantity") - 1);
                    if (cart.get(title).get("quantity") == 0) {
                        cart.remove(title);
                    }
                    break;
                case "delete":
                    cart.remove(title);
                    break;
            }
        }
        session.setAttribute("cart", cart);
        session.setAttribute("ids", ids);

        JsonArray cartArray = new JsonArray();

        for (Map.Entry<String, Map<String, Double>> entry : cart.entrySet()) {
            JsonObject jsonItem = new JsonObject();
            jsonItem.addProperty("title", entry.getKey());
            jsonItem.addProperty("quantity", entry.getValue().get("quantity"));
            jsonItem.addProperty("price", entry.getValue().get("price"));
            jsonItem.addProperty("movieId", ids.get(entry.getKey()));
            cartArray.add(jsonItem);
            //System.out.println(jsonItem);
        }
        //System.out.println(cartArray);
        responseJsonObject.add("cart", cartArray);

        response.getWriter().write(responseJsonObject.toString());
    }
}
