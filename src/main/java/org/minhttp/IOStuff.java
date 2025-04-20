package org.minhttp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import static java.nio.charset.StandardCharsets.UTF_8;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/** These are shortcuts to common IO problems. */
public class IOStuff {
    private final static Logger logger = LoggerFactory.getLogger(IOStuff.class);

    public static interface MyInputStreamReader {
        public void read(InputStream istr) throws IOException;
    }
    /** This allows you to read a binary file via reader from url if cache miss happens,
        ostensible to write it back out */
    public static void ifNeeded(
            HttpServletRequest req, HttpServletResponse response, URL url,
            MyInputStreamReader reader
        ) throws java.io.IOException {
        final URLConnection conn = url.openConnection();
        final long myLastMod=conn.getLastModified();
        final String myETag = String.valueOf(myLastMod)+"R";
        final String theirETag=req.getHeader("If-None-Match")+"";
        final long theirLastMod=req.getDateHeader("If-Modified-Since");
        if (theirETag.contains(myETag)) {
            logger.info("ETag match: {}", url);
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        if (theirLastMod >= myLastMod) {
            logger.info("Last-modified match: {}", url);
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        response.setHeader("ETag", myETag);
        response.setDateHeader("Last-Modified", myLastMod);
        //response.setHeader("Cache-Control", "max-age=60, public, must-revalidate");
        response.setHeader("Cache-Control", "max-age=60, public");
        logger.info("Opening: {}", url);
        try (InputStream istr=url.openStream()) {
            reader.read(istr);
        }
    }

    public static String getAsString(URL url) throws IOException {
        if (url==null)
            throw new IllegalArgumentException("URL is null");
        try (InputStream istr = url.openStream()) {
            return getAsString(istr);
        }
   }

   public static String getAsString(InputStream stream) {
        StringBuilder writer = new StringBuilder();
        try (InputStreamReader reader=new InputStreamReader(stream, UTF_8)) {
            boolean hasData=false;
            char[] buffer=new char[1024];
            int readed=0;
            while ((readed=reader.read(buffer, 0, buffer.length))>0){
                String s=new String(buffer, 0, readed);
                writer.append(s);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read: "+e, e);
        }
        return writer.toString();
    }

    public interface ByteBufferReader {
        public void take(byte[] buffer, int bytesRead) throws Exception;
    }
    public static void read(InputStream instr, int bufSize, ByteBufferReader bbr) {
        try {
            byte[] buff = new byte[bufSize];
            int read=-1;
            while ((read=instr.read(buff, 0, buff.length))!=-1)
                bbr.take(buff, read);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read: "+e, e);
        }
    }

    public interface CharBufferReader {
        public void take(char[] buffer, int bytesRead) throws Exception;
    }
    public static void readChars(InputStream istr, int bufSize, CharBufferReader bbr) {
        try (InputStreamReader reader=new InputStreamReader(istr, UTF_8)) {
            char[] buff = new char[bufSize];
            int read=-1;
            while ((read=reader.read(buff, 0, buff.length))!=-1)
                bbr.take(buff, read);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read: "+e, e);
        }
    }

    public interface BufferedReaderReader {
        public void read(BufferedReader br) throws Exception;
    }
    public static void withBufferedReader(URL url, BufferedReaderReader brr) {
        try (
            InputStream istr = url.openStream();
            InputStreamReader reader=new InputStreamReader(istr, UTF_8);
            BufferedReader br=new BufferedReader(reader);
            ) {
            brr.read(br);
        } catch (Exception e) {
            throw new RuntimeException("Failed read: "+e, e);
        }
    }
}