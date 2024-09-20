package org.minhttp;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Implement this interface and pass instance to MyHandlerTree
 * to act as your exception handler.
 */
public @FunctionalInterface interface MyExceptionHandler {
    /** Handle an exception that occurred while processing a request
     * @param req The http request
     * @param resp The http response, which may be writeable
     * @param path The path that the request was sent to
     * @param e The exception that was thrown.
    */
    public void handle(HttpServletRequest req, HttpServletResponse resp, String path, Exception e);
}
