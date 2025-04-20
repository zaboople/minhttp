package org.blorp;
import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.minhttp.IOStuff;

public class Templates {
    private final Logger logger=LoggerFactory.getLogger(getClass());
    private String[] headerFooter;
    private Resources resources;

    public Templates(Resources r) {
        resources=r;
        headerFooter=getHeaderFooter();
    }

    public void handleRootPath(
            HttpServletRequest req, HttpServletResponse resp, List<String> pathElems
        ) throws Exception {
        wrapFile(req, resp, "/menu.html");
    }

    public void handleUIPath(
            HttpServletRequest req, HttpServletResponse resp, List<String> pathElems
        ) throws Exception {
        String path="/ui/"+pathElems.stream().collect(Collectors.joining("/"));
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
    }

    public void wrap(
            HttpServletResponse response, Except.ExceptionalConsumer<PrintWriter> r
        ) {
        try {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter writer=response.getWriter();
            writer.append(headerFooter[0]);
            writer.flush();
            r.accept(writer);
            writer.append(headerFooter[1]);
            writer.flush();
        } catch (Exception e) {
            logger.warn("Something went wrong...");
            if (checkEOF(e))
                logger.warn("Encountered EOF, probably abandoned request: {}", e.toString());
            else
                throw new RuntimeException(e);
        }
    }
    public void printFile(PrintWriter writer, String filename) throws IOException {
        try (InputStream istr=resources.getURL(filename).openStream()) {
            printFile(writer, istr);
        }
    }

    ////////////////
    // INTERNALS: //
    ////////////////

    private boolean checkEOF(Throwable e) {
        if (e==null)
            return false;
        if (e instanceof org.eclipse.jetty.io.EofException)
            return true;
        return checkEOF(e.getCause());
    }

    private void wrapFile(
            HttpServletRequest req, HttpServletResponse resp, String filename
        ) throws IOException {
        printOrWrapFile(req, resp, filename, true);
    }
    private void printFile(
            HttpServletRequest req, HttpServletResponse resp, String filename
        ) throws IOException {
        printOrWrapFile(req, resp, filename, false);
    }
    private void printOrWrapFile(
          HttpServletRequest req, HttpServletResponse resp,
          String filename, boolean decorate
        ) throws IOException {
        IOStuff.ifNeeded(req, resp, resources.getURL(filename), inStream -> {
            resp.setContentType("text/html; charset=UTF-8");
            if (decorate)
                wrap(resp, writer -> printFile(writer, inStream));
            else
                printFile(resp.getWriter(), inStream);
        });
    }
    private void printFile(PrintWriter writer, InputStream istr) throws IOException {
        IOStuff.readChars(
            istr, 1024 * 8,
            (char[] buffer, int charsRead) -> {
                writer.write(buffer, 0, charsRead);
                writer.flush();
            }
        );
    }

    private String[] getHeaderFooter() {
        final String[] results=new String[2];
        IOStuff.withBufferedReader(resources.getURL("/main.html"), br->{
            StringBuilder header=new StringBuilder();
            String s=null;
            while ((s=br.readLine())!=null) {
                if (s.contains("---- ---- ----"))
                    break;
                header.append(s).append("\n");
            }
            results[0]=header.toString();
            StringBuilder footer=new StringBuilder();
            while ((s=br.readLine())!=null)
                footer.append(s).append("\n");
            results[1]=footer.toString();
        });
        return results;
    }

}
