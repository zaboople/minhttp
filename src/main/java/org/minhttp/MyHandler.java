package org.minhttp;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Implement this to create an object that receives HTTP requests for a given path.
 */
public @FunctionalInterface interface MyHandler {
    /**
     * Handle an HTTP request
     * @param req The HTTP request
     * @param resp The HTTP response
     * @param pathValues May be empty; only contains path values that were designated as "wildcards"
     * @throws Exception If the handler wants to throw any kind of exception it can do so; refer to HTree
     *   for how exceptions are handled.
     */
    public void handle(HttpServletRequest req, HttpServletResponse resp, List<String> pathValues) throws Exception;
}