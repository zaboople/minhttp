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
import org.minhttp.HTree;
import org.minhttp.HTreeJettyHandler;

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
        AbstractHandler treeHandler=new HTreeJettyHandler(getTree());
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

    /** Builds the tree of path->handler relations. */
    private HTree getTree() {
        HTree tree=new HTree(
            (req, resp, path)->
                handleNotFound(resp),
            (req, resp, path, exception)->
                handleInternalError(req, resp, exception),
            (req, resp, path)->
                filter(req, resp, path)
        );
        tree.create("ui").all(templates.getHandler());
        tree
            .put("",              (req, resp, path)-> templates.wrapFile(req, resp, "/menu.html"))
            .put("splode",        (req, resp, path)-> {throw new Exception("On purpose");})
            .put("cookies",       new HandleCookie(templates).getHandler())
            .put("memory",        new HandleDebugMemory(templates).getHandler())
            .put("x-favicon.ico", new HandleFavIcon(resources).getHandler())
            .put("favicon.ico",   new HandleFavIcon(resources).getHandler())
            .put("json",          new HandleJson().getHandler())
            .put("headers",       new HandleDebugRequest().getHandler(templates))
            ;
        HandleMathLoad.addTo(tree.create("math"), templates);
        new HandleDataInPath(tree.create("data"), templates);
        return tree;
    }

    private void handleNotFound(HttpServletResponse resp) throws Exception {
        resp.setStatus(404);
        templates.wrap(resp, w->w.println("Not found!"));
    }
    private void handleInternalError(HttpServletRequest req, HttpServletResponse resp, Exception e){
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
        logger.info("Filtering...");
    }
}
