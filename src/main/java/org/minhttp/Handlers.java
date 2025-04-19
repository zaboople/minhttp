package org.minhttp;
import java.util.*;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Handlers extends AbstractHandler {

    public @FunctionalInterface static interface MyHandler {
        public void handle(HttpServletRequest req, HttpServletResponse resp, List<String> pathValues) throws Exception;
    }
    public @FunctionalInterface static interface ErrorHandler {
        public void handle(HttpServletRequest req, HttpServletResponse resp, String path, Exception ex);
    }

    private final TreeNode rootNode = new TreeNode();
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private ErrorHandler errorHandler;
    private static class TreeNode {
        Map<String, TreeNode> children;
        Map<String, MyHandler> handlers;
        TreeNode wildcard;
        boolean allChildren = false;
    }

    ////////////
    // SETUP: //
    ////////////

    /**
        For path, you can use "*" for wildcards and "**" for a wildcard that handles
        all paths below the specified one
    */
    public Handlers add(String method, String path, MyHandler mh) {
        for (String meth: method.split(","))
            doAdd(meth, path, mh);
        return this;
    }
    private Handlers doAdd(String method, String path, MyHandler mh) {
        if (mh==null)
            throw new RuntimeException("Null not permitted");
        method = method.toUpperCase().trim();
        String[] paths = path.split("/");
        TreeNode current = rootNode;
        for (String p: paths)
            if (!p.equals("")) {
                if (p.equals("*")) {
                    if (current.wildcard==null)
                        current.wildcard = new TreeNode();
                    current = current.wildcard;
                } else if (p.equals("**")) {
                    if (current.wildcard!=null)
                        throw new RuntimeException("Already exists: "+method+" "+path);
                    current.wildcard = new TreeNode();
                    current.wildcard.allChildren = true;
                    current = current.wildcard;
                    break;
                } else {
                    if (current.children == null)
                        current.children = new HashMap<>();
                    current = current.children.computeIfAbsent(p, __ -> new TreeNode());
                }
            }
        if (current.handlers==null)
            current.handlers = new HashMap<>();
        if (current.handlers.get(method)!=null)
            throw new RuntimeException("Already exists: "+method+" "+path);
        logger.info("ADD: "+method+" "+path);
        current.handlers.put(method, mh);
        return this;
    }

    public void setErrorHandler(ErrorHandler eh) {
        this.errorHandler = eh;
    }

    ////////////////////
    // HTTP HANDLING: //
    ////////////////////

    /**
     * Overrides the Jetty AbstractHandler's method
     */
    public @Override void handle(
            String target, Request baseRequest,
            HttpServletRequest request, HttpServletResponse response
        ) throws IOException, ServletException {
        handle(request, response, target);
    }
    private void handle(
            HttpServletRequest req, HttpServletResponse resp, String path
        ) throws IOException, ServletException {
        try {
            final String method = req.getMethod().toUpperCase().trim();
            logger.info("Request: " + method + " "+path);
            final List<String> elems = new ArrayList<>();
            final String[] paths = path.split("/");
            TreeNode curr = rootNode;
            for (String p: paths)
                if (!p.equals("")) {
                    if (curr.allChildren)
                        elems.add(p);
                    else if (curr.wildcard!=null) {
                        curr = curr.wildcard;
                        elems.add(p);
                    } else {
                        curr = curr.children == null
                            ?null
                            :curr.children.get(p);
                        if (curr == null) {
                            fail(resp, "No mapping for path");
                            return;
                        }
                    }
                }
            final MyHandler handler = curr.handlers != null
                ? curr.handlers.get(method)
                : null;
            if (handler==null)
                fail(resp, "No mapping for request method: "+method);
            else
                handler.handle(req, resp, elems);
        } catch (Exception e) {
            if (errorHandler!=null)
                errorHandler.handle(req, resp, path, e);
            else
                throw new RuntimeException("Failed", e);
        }
    }
    private void fail(HttpServletResponse res, String msg) throws Exception {
        logger.warn("Bad request: "+msg);
        java.io.Writer writer = res.getWriter();
        res.setStatus(404);
        writer.write(msg);
        writer.flush();
    }
}
