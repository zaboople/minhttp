package org.blorp;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.format.DateTimeFormatter;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Sends the traditional favicon.ico icon, trying very hard (usually to no avail) to get browsers to cache
 * as well as validate, and of course they are uncooperative
 */
public class HandleFavIcon {

    private final Resources resources;

    public HandleFavIcon(Resources resources) {
        this.resources=resources;
    }

    public void handle(HttpServletRequest req, HttpServletResponse response, List<String> path) throws Exception {
        resources.ifNeeded(req, response, "/favicon.ico", inStream->{
            OutputStream ostr=response.getOutputStream();
            byte[] buff=new byte[2048];
            int read=-1;
            while ((read=inStream.read(buff, 0, buff.length))!=-1)
                ostr.write(buff, 0, read);
            ostr.flush();
        });
    }
}
