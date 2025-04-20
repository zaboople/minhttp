package org.blorp;
import java.io.Writer;
import java.io.IOException;
import java.security.SecureRandom;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import org.minhttp.Handlers;

/** This was made primarily for load testing by doing some kind of silly "work". */
public class HandleMathLoad {

    private final static Logger logger=LoggerFactory.getLogger(HandleMathLoad.class);
    private final static SecureRandom randomizer=new SecureRandom();

    public static void addTo(Handlers hdl, Templates templates, String rootPath) {
        hdl.add(
                "GET", rootPath + "/some",
                (req, resp, elems)-> templates.wrap(resp, w->print(req, w, 32, 64))
            )
            .add(
                "GET", rootPath + "/more",
                (req, resp, elems)-> templates.wrap(resp, w->print(req, w))
            )
            .add("GET", rootPath + "/lots",
                (req, resp, elems)-> templates.wrap(resp, w->printIframes(w))
            );
    }

    private static void print(
            HttpServletRequest request, Appendable writer
        ) throws Exception {
        String swide = request.getParameter("wide"),
            slong = request.getParameter("length");
        print(request, writer, Integer.parseInt(swide), Integer.parseInt(slong));
    }


    private static void print(
            HttpServletRequest request, Appendable writer,
            int wide, int length
        ) throws Exception {
        int halfWide = wide/2, halfLong = length/2;
        if (halfWide == 0) halfWide = 1;
        if (halfLong == 0) halfLong = 1;
        writer
            .append("<a href='/'>Home</a><br><br>\n")
            .append("<a href='./more?wide="+(wide*2)+"&length="+(length*2)+"'>More digits</a>\n")
            .append("&nbsp;&nbsp;&nbsp;")
            .append("<a href='./more?wide="+halfWide+"&length="+halfLong+"'>Less digits</a><br>\n")
            .append("<br><a href='./lots'>A LOT more...</a><br>\n");
        final long start=System.nanoTime();
        writer.append("Random arithmetic: <p style=\"font-size:6px\">");
        StringBuilder buffer=new StringBuilder();
        int bufIndex=0, result=0;
        for (int line=0; line<wide; line++) {
            while (buffer.length()<length)
                buffer.append(Math.abs(result ^= randomizer.nextInt()));
            writer.append(buffer.substring(0, length));
            buffer.delete(0,length);
            writer.append("<br>");
        }
        writer.append("</p>\n");
        logger.info("Complete: {}", request.getParameter("index"));
    }

    public static void printIframes(Appendable writer) throws IOException {
        int count=150;
        writer.append(String.format("<h2>This is the /math page running in %d iframes</h2>", count))
            .append("<p>One way to test concurrency...")
            .append("<a href=\"..\">Home</a></p>");
        for (int i=0; i<count; i++)
            writer.append("<iframe src='./some?index=").append(""+(i+1)).append("'></iframe>");
    }
}