package org.blorp;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static java.nio.charset.StandardCharsets.UTF_8;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import org.minhttp.Handlers;

public class Templates {
    private final Logger logger=LoggerFactory.getLogger(getClass());
    private String[] headerFooter;
    private Resources resources;

    public Templates(Resources r) {
        resources=r;
        headerFooter=getHeaderFooter();
    }
    public Handlers.MyHandler getHandler() {
        return (req, resp, elems)-> {
            String path="/ui/"+elems.stream().collect(Collectors.joining("/"));
            try {
                if (path.endsWith(".html"))
                    wrapFile(req, resp, path);
                else
                    printFile(req, resp, path);
            } catch (java.io.FileNotFoundException e) {
                logger.warn("Apparently nonexistent: "+path+" -> "+e);
                resp.setStatus(404);
                wrap(resp, w->w.write("No such path: "+path));
            }
        };
    }

    public void wrap(
            HttpServletResponse response, Except.ExceptionalConsumer<PrintWriter> r
        ) {
        try {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter writer=response.getWriter();
            head(response, writer);
            r.accept(writer);
            foot(writer);
            writer.flush();
        } catch (org.eclipse.jetty.io.EofException e) {
            logger.warn("Encountered EOF, probably abandoned request: {}", e.toString());
        } catch (Exception e) {
            logger.warn("Something went wrong...");
            if (checkEOF(e))
                logger.warn("Encountered EOF, probably abandoned request: {}", e.toString());
            else
                throw new RuntimeException(e);
        }
    }

    private boolean checkEOF(Throwable e) {
        if (e==null)
            return false;
        if (e instanceof org.eclipse.jetty.io.EofException)
            return true;
        return checkEOF(e.getCause());
    }
    public void wrapFile(
            HttpServletRequest req, HttpServletResponse resp, String filename
        ) throws Exception {
        printFile(req, resp, filename, true);
    }
    public void printFile(
            HttpServletRequest req, HttpServletResponse resp, String filename
        ) throws Exception {
        printFile(req, resp, filename, false);
    }
    public void printFile(PrintWriter writer, String filename) throws Exception {
        try (InputStream istr=resources.getURL(filename).openStream()) {
            printFile(writer, istr);
        }
    }

    private void printFile(
          HttpServletRequest req, HttpServletResponse resp,
          String filename, boolean decorate
        ) throws Exception {
        resources.ifNeeded(req, resp, filename, inStream->{
            resp.setContentType("text/html; charset=UTF-8");
            if (decorate)
                wrap(resp, writer->printFile(writer, inStream));
            else
                printFile(resp.getWriter(), inStream);
        });
    }
    private void printFile(PrintWriter writer, InputStream istr) throws Exception {
        try (InputStreamReader reader=new InputStreamReader(istr, UTF_8)) {
            char[] buf=new char[1024 * 8];
            int didRead=reader.read(buf, 0, buf.length);
            while (didRead!=-1) {
                writer.write(buf, 0, didRead);
                writer.flush();
                didRead=reader.read(buf, 0, buf.length);
            }
        }
    }

    private void head(HttpServletResponse response, PrintWriter writer) throws Exception {
        writer.append(headerFooter[0]);
        writer.flush();
    }
    private void foot(Appendable writer) throws IOException {
        writer.append(headerFooter[1]);
    }
    private String[] getHeaderFooter() {
        String[] results=new String[2];
        try (
            InputStream istr = resources.getURL("/main.html").openStream();
            InputStreamReader reader=new InputStreamReader(istr, UTF_8);
            BufferedReader br=new BufferedReader(reader);
            ) {
            StringBuilder header=new StringBuilder();
            String s=null;
            while ((s=br.readLine())!=null) {
                if (s.contains("---- ---- ----"))
                    break;
                else
                    header.append(s).append("\n");
            }
            results[0]=header.toString();
            StringBuilder footer=new StringBuilder();
            while ((s=br.readLine())!=null) {
                footer.append(s).append("\n");
            }
            results[1]=footer.toString();
        } catch (Exception e) {
            logger.error("Could not read main template: "+e);
            results[0]="";
            results[1]="";
        }
        return results;
    }

}
