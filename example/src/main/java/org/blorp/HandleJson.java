package org.blorp;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.io.*;
import static java.nio.charset.StandardCharsets.UTF_8;

import org.json.*;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class HandleJson {
    private final static Logger logger = LoggerFactory.getLogger(HandleJson.class);
    static int count=0;

    public static void handle(
          HttpServletRequest req, HttpServletResponse resp, List<String> path
        ) throws Exception {
        final String method=req.getMethod();
        for (String value: Collections.list(req.getHeaders("User-Agent")))
            logger.info("Agent: "+value);
        resp.setContentType("application/json;charset=UTF-8");
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
            try {
                JSONTokener tkn=new JSONTokener(
                    new InputStreamReader(req.getInputStream(), UTF_8)
                );
                char c=tkn.next();
                tkn.back();
                if (c=='[') {
                    JSONArray j=new JSONArray(tkn);
                    logger.info("Input: {}", j);
                    handle(resp, j);
                }
                else
                if (c=='{') {
                    JSONObject j=new JSONObject(tkn);
                    logger.info("Input: {}", j);
                    handle(resp, j);
                }
                else {
                    logger.info("Invalid request");
                    JSONObject j=new JSONObject();
                    j.put("Status", "Invalid");
                    handle(resp, j);
                }
            } catch (Exception e) {
                logger.warn("Invalid JSON input, apparently: "+e);
                JSONObject jo = new JSONObject();
                jo.put("Status", "Unexpected: "+e);
                resp.setStatus(400);
                handle(resp, jo);
            }
        } else if ("GET".equalsIgnoreCase(method)) {
            handle(resp, new JSONObject());
        } else {
            logger.info("Invalid method: {}", method);
            JSONObject j=new JSONObject();
            j.put("Status", "Invalid");
            handle(resp, j);
        }
    }
    private static void handle(HttpServletResponse resp, JSONObject jo) throws Exception {
        jo.put("number", ++count);
        jo.put("name", "janet"+(count));
        jo.write(resp.getWriter());

    }

    private static void handle(HttpServletResponse resp, JSONArray jo) throws Exception {
        jo.put(++count);
        jo.write(resp.getWriter());
    }

}