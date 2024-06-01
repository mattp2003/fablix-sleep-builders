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
    String contextPath;
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    String getBaseUrl(HttpServletRequest request) {
        String baseUrl = request.getRequestURL().substring(0, request.getRequestURL().length() - request.getRequestURI().length()) + request.getContextPath();
        System.out.println(request.getRequestURI());
        System.out.println(request.getContextPath());
        return baseUrl;
    }
    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("LoginFilter: " + httpRequest.getContextPath());
        System.out.println(getBaseUrl(httpRequest));

        contextPath = httpRequest.getContextPath();

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }
        // Redirect to login page if the "user" attribute doesn't exist in session
        if (httpRequest.getSession().getAttribute("user") == null) {

            httpResponse.sendRedirect(httpRequest.getContextPath() + "/main/main.html");


        // Otherwise is logged in
        } else {
            boolean isEmployee = (boolean) httpRequest.getSession().getAttribute("isEmployee");

            System.out.println("You are accessing: " + httpRequest.getRequestURI());
            //accessing employee only paths
            if (httpRequest.getRequestURI().contains("_dashboard")){
                if (isEmployee){
                    chain.doFilter(request, response);
                }
                else{
                    System.out.println("You are a user and attempting to access the dashboard, redirecting ... ");
                    httpResponse.sendRedirect(httpRequest.getContextPath() + "/index.html");
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
        String path = requestURI.substring(contextPath.length()+1);
        System.out.println(path);
        return allowedURIs.contains(path);
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("main/main.html");
        allowedURIs.add("main/main.css");
        allowedURIs.add("main/main.js");

        allowedURIs.add("login.html");
        allowedURIs.add("login.js");
        allowedURIs.add("Asset/login.css");

        allowedURIs.add("_dashboard/login.html");
        allowedURIs.add("api/login");
    }

    public void destroy() {
        // ignored.
    }

}
