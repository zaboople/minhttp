package org.blorp;

import java.io.Writer;
import java.util.*;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Evil. Will actually exit the application because you called a url!
 * Also convenient.
 */
public class HandleExit {
    private final Logger logger=LoggerFactory.getLogger(getClass());

    public static void handle(
            HttpServletRequest req, HttpServletResponse resp, List<String> path
        ) throws Exception {
        Writer w = resp.getWriter();
        w.write("*** Hi! I am Exiting... bye... ***");
        w.flush();
        w.close();
        System.exit(0);
    }


}