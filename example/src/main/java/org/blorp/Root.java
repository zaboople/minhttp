package org.blorp;
import java.io.File;
import java.security.PublicKey;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.minhttp.MultipartHandler;
import org.minhttp.Handlers;

/**
 *  This starts up a web server.
 */
public class Root {

    private final Logger logger=LoggerFactory.getLogger(getClass());
    private final Resources resources;
    private final Templates templates;
    private final Server server;

    /**
     * @param baseDirectory It's ok if baseDirectory is null.
     */
    public Root(int port, String baseDirectory) throws Exception {
        resources=new Resources(baseDirectory);
        templates=new Templates(resources);

        // Build up AbstractHandler layers in reverse, to create:
        // GzipHandler -> MultipartHandler -> HTreeJettyHandler
        AbstractHandler treeHandler=getHandlers();
        AbstractHandler multiPartHandler=new MultipartHandler(treeHandler);
        GzipHandler gzipHandler = new GzipHandler();
        gzipHandler.setIncludedMimeTypes(
            "text/html", "text/plain", "text/xml", "text/css",
            "text/javascript", "application/javascript"
        );
        gzipHandler.setHandler(multiPartHandler);

        // Start web server:
        server = new Server(port);
        server.setHandler(gzipHandler);
    }

    public void start() throws Exception {
        server.start();
        server.join();
    }

    private Handlers getHandlers() {
        Handlers hdl = new Handlers();
        hdl.add("GET",       "/",              templates::handleRootPath)
            .add("GET",      "/ui/**",         templates::handleUIPath)
            .add("GET",      "/x-favicon.ico", new HandleFavIcon(resources)::handle)
            .add("GET",      "/favicon.ico",   new HandleFavIcon(resources)::handle)
            .add("GET,POST", "/cookies",       new HandleCookie(templates)::handle)
            .add("GET",      "/memory",        new HandleDebugMemory(templates)::handle)
            .add("GET,POST", "/headers",       new HandleDebugRequest(templates)::handle)
            .add("GET,POST", "/json",          HandleJson::handle)
            .add("GET",      "/exit",          HandleExit::handle)
            .add("GET",      "/splode",        (req, resp, path)-> {throw new Exception("On purpose");})
            ;
        HandleMathLoad.addTo(hdl, templates, "/math");
        new HandleDataInPath(hdl, templates, "/data");
        hdl.setErrorHandler(this::handleInternalError);
        return hdl;
    }

    private void handleNotFound(HttpServletResponse resp) throws Exception {
        resp.setStatus(404);
        templates.wrap(resp, w->w.println("Not found!"));
    }
    private void handleInternalError(HttpServletRequest req, HttpServletResponse resp, String path, Exception e){
        try {
            logger.error("Internal error: "+e, e);
            resp.setStatus(500);
            templates.wrap(resp, w->{
                w.append("<pre>");
                e.printStackTrace(w);
                w.append("</pre>");
            });
        } catch (Exception e2) {
            e.printStackTrace(System.out);
        }
    }
    private void filter(HttpServletRequest req, HttpServletResponse resp, List<String> wildcards){
        //logger.info("Filtering...");
    }
}
