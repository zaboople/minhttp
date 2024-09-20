package org.blorp;

import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import org.minhttp.HTree;
import org.minhttp.HTreeNode;
import org.minhttp.MyHandler;

/**
 * Sets up cookies
 */
public class HandleCookie {
    private final Logger logger=LoggerFactory.getLogger(getClass());
    private final Templates templates;
    public HandleCookie(Templates templates){
        this.templates=templates;
    }

    public MyHandler getHandler() {
        return (req, resp, elems)-> handle(req, resp);
    }

    private void handle(
            HttpServletRequest req, HttpServletResponse resp
        ) throws Exception {
        boolean post=req.getMethod().equals("POST");
        if (post){
            String name=req.getParameter("name"),
                value=req.getParameter("value"),
                clear=req.getParameter("clear");
            if (name!=null && value!=null && !name.equals("")) {
                setAndRedirect(resp, name, value);
                return;
            } else if (clear!=null && !clear.equals("")) {
                logger.info("Clear cookies!");
                Cookie[] cs=req.getCookies();
                if (cs!=null)
                    for (Cookie c: cs) {
                        c.setMaxAge(0);
                        resp.addCookie(c);
                    }
                resp.sendRedirect("./cookies");
                return;
            }
        } else {
            String random=req.getParameter("random");
            if (random!=null) {
                setAndRedirect(
                    resp,
                    "R"+UUID.randomUUID().toString().substring(0, 8),
                    UUID.randomUUID().toString()
                );
                return;
            }
        }
        templates.wrap(resp, writer->{
            Cookie[] cs=req.getCookies();
            final int len=cs==null ?0 :cs.length;
            writer.append("\n<br>Cookie count: "+len);
            if (cs!=null)
                for (Cookie c: cs)
                    writer.append("\n<br><b>Cookie: </b> <code>")
                        .append(c.getName())
                        .append(" -> ")
                        .append(c.getValue())
                        .append("</code>");
            templates.printFile(writer, "/cookie_form.html");
        });
    }

    private static void setAndRedirect(HttpServletResponse resp, String name, String value) throws Exception {
        Cookie cookie=new Cookie(name, value);
        cookie.setMaxAge(120);
        resp.addCookie(cookie);
        resp.sendRedirect("/cookies");
    }

}