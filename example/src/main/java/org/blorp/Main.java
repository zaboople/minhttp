package org.blorp;
import java.io.File;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 *  This starts up the application.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        int port = 80;
        String dir = null;
        for (int i=0; i<args.length; i++) {
            final String arg = args[i];
            if (arg.startsWith("-p") || arg.startsWith("--p"))
                port=Integer.parseInt(args[++i]);
            else
            if (arg.startsWith("-d") || arg.startsWith("--dir"))
                dir=args[++i];
            else
            if (arg.startsWith("-h") || arg.startsWith("--help")) {
                System.out.println(
                    "Usage: [-p <port>] [-d <dir>] [-h]\n"
                    +"Example: -p 8080 -d mydirectory\n"
                    +"  -p <port>: Sets the port for your web server \n"
                    +"  -d <dir>:  Sets the directory for templates files etc. - \n"
                    +"     Normally these are compiled into the build so you \n"
                    +"     don't need to care.\n"
                );
                return;
            }
        }
        ((Logger)LoggerFactory.getLogger("ROOT")).setLevel(Level.INFO);
        ((Logger)LoggerFactory.getLogger("org.minhttp")).setLevel(Level.DEBUG);
        new Root(port, dir).start();
    }
}
