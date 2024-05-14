import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    String base = "/cs122b-sleep-builders/";
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("LoginFilter: " + httpRequest.getRequestURI());

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }
        // Redirect to login page if the "user" attribute doesn't exist in session
        if (httpRequest.getSession().getAttribute("user") == null) {

//            System.out.println("redirection to:" );
            httpResponse.sendRedirect(base + "main/main.html");


        // Otherwise is logged in
        } else {
            boolean isEmployee = (boolean) httpRequest.getSession().getAttribute("isEmployee");

            System.out.println("You are accessing: " + httpRequest.getRequestURI());
            //accessing employee only paths
            if (httpRequest.getRequestURI().contains(base + "_dashboard")){
                if (isEmployee){
                    chain.doFilter(request, response);
                }
                else{
                    System.out.println("You are a user and attempting to access the dashboard, redirecting ... ");
                    httpResponse.sendRedirect(base);
                }
            }
            else{
                chain.doFilter(request, response);
            }
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        return allowedURIs.contains(requestURI);
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add(base + "main/main.html");
        allowedURIs.add(base + "main/main.css");
        allowedURIs.add(base + "main/main.js");

        allowedURIs.add(base + "login.html");
        allowedURIs.add(base + "login.js");
        allowedURIs.add(base + "api/login");

        allowedURIs.add(base + "_dashboard/login.html");
        allowedURIs.add(base + "_dashboard/login.js");
        allowedURIs.add(base + "api/employeeLogin");


        String base2 = "/cs122b_sleep_builders_war/";
        allowedURIs.add(base2 + "main/main.html");
        allowedURIs.add(base2 + "main/main.css");
        allowedURIs.add(base2 + "main/main.js");

        allowedURIs.add(base2 + "login.html");
        allowedURIs.add(base2 + "login.js");
        allowedURIs.add(base2 + "api/login");

        allowedURIs.add(base2 + "_dashboard/login.html");
        allowedURIs.add(base2 + "_dashboard/login.js");
        allowedURIs.add(base2 + "api/employeeLogin");
    }

    public void destroy() {
        // ignored.
    }

}
