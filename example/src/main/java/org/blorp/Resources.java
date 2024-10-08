package org.blorp;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.Writer;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.function.Consumer;
import static java.nio.charset.StandardCharsets.UTF_8;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class Resources {
    private final Logger logger=LoggerFactory.getLogger(getClass());
    private final String baseDir;
    public Resources(String directory) {
        if (directory!=null) {
            File f=new File(directory);
            if (!f.exists())
                throw new RuntimeException("Does not exist: "+directory);
            if (!f.isDirectory())
                throw new RuntimeException("Not a directory: "+directory);
        }
        this.baseDir=directory;
        logger.info("Using base directory: {}", baseDir);
    }
    public void ifNeeded(
            HttpServletRequest req, HttpServletResponse response, String file,
            Except.ExceptionalConsumer<InputStream> reader
        ) throws Exception {
        final URL url = getURL(file);
        final URLConnection conn = url.openConnection();
        final long myLastMod=conn.getLastModified();
        final String myETag = String.valueOf(myLastMod)+"R";
        final String theirETag=req.getHeader("If-None-Match")+"";
        final long theirLastMod=req.getDateHeader("If-Modified-Since");
        if (theirETag.contains(myETag)) {
            logger.info("ETag match: {}", file);
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        if (theirLastMod >= myLastMod) {
            logger.info("Last-modified match: {}", file);
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        response.setHeader("ETag", myETag);
        response.setDateHeader("Last-Modified", myLastMod);
        //response.setHeader("Cache-Control", "max-age=60, public, must-revalidate");
        response.setHeader("Cache-Control", "max-age=60, public");
        logger.info("Opening: {}", file);
        try (InputStream istr=url.openStream()) {
            reader.accept(istr);
        }
    }
    public URL getURL(String name) {
        return Except.get(()->{
            if (baseDir!=null)
                return new File(baseDir+name).toURI().toURL();
            else
                return getClass().getResource(name);
        });
    }


}