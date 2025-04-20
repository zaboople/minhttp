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

import org.minhttp.IOStuff;

public class Resources {
    private final static Logger logger=LoggerFactory.getLogger(Resources.class);
    private final String baseDir;

    public Resources() {
        this(null);
    }
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

    public URL getURL(String name) {
        return Except.get(()->{
            if (baseDir!=null)
                return new File(baseDir+name).toURI().toURL();
            else
                return getClass().getResource(name);
        });
    }


}