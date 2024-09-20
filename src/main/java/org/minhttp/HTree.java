package org.minhttp;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


/**
 * Acts as the root note of your service tree. Once created, you can call
 * add(path, MyHandler) or put(path, MyHandler) to create a handler for a given
 * path, including add("", MyHandler) to handle the / path.
 */
public class HTree extends HTreeNode {
    private final static Logger logger=LoggerFactory.getLogger(HTree.class);
    private final static MyHandler defaultNotFoundHandler=(req, resp, pathValues)->{
        resp.setStatus(404);
        resp.getWriter().append("Not found");
        logger.warn("No handler for: "+pathValues);
    };

    private MyExceptionHandler exceptionHandler=null;
    private MyHandler filter=null;

    /**
     * Creates a new HTree without a 404-not-found handler or an Exception handler.
     */
    public HTree() {
        super("", false, defaultNotFoundHandler, null);
    }

    /**
     * @param notFoundHandler Acts as the automatic handler at all levels for 404 issues.
     */
    public HTree(MyHandler notFoundHandler) {
        super("", false, notFoundHandler, null);
    }

    /**
     * @param notFoundHandler Acts as the automatic handler at all levels for 404 issues.
     * @param exceptionHandler Acts as the automatic handler for exceptions that happen
     *   during processing by handle(), including exceptions from nested MyHandlers.
     * @param filter Called for every request before passing it on to other MyHandlers,
     *   including notFoundHandler.
     */
    public HTree(
            MyHandler notFoundHandler,
            MyExceptionHandler exceptionHandler,
            MyHandler filter
        ) {
        super("", false, notFoundHandler, null);
        this.exceptionHandler= exceptionHandler;
        this.filter = filter;
    }

    /**
     * @param exceptionHandler Acts as the automatic handler for exceptions that happen
     *   during processing by handle(), including exceptions from nested MyHandlers.
     * @return self
     */
    public HTree setExceptionHandler(MyExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    /**
     * @param filter Called for every request before passing it on to other MyHandlers,
     *   including notFoundHandler.
     * @return self
     */
    public HTree setFilter(MyHandler filter) {
        this.filter = filter;
        return this;
    }

    /**
     * This is the main entry point for HTTP requests into the HTree.
     * @param req A standard HttpServletRequest
     * @param resp A standard HttpServletRequest
     * @param path The path of the request (technically obtainable from req already)
     */
    public void handle(HttpServletRequest req, HttpServletResponse resp, String path) {
        try {
            logger.info("{}: {}", req.getMethod(), path);
            List<String> list=new ArrayList<>(4);
            if (path.equals("") || path.equals("/")) {
                list.add("");
                super.handle(req, resp, list, null, filter);
                return;
            }
            int index=1;
            int pathLen=path.length();
            while (index < pathLen) {
                int temp=path.indexOf("/", index);
                if (temp==-1)
                    temp=pathLen;
                String found=path.substring(index, temp);
                if (!found.equals(""))
                    list.add(found);
                index=temp+1;
            }
            if (list.size()==0)
                list.add("");
            super.handle(req, resp, list, null, filter);
        } catch (Exception e) {
            if (exceptionHandler!=null)
                exceptionHandler.handle(req, resp, path, e);
            else
                throw new RuntimeException("Unhandled error", e);
        }
    }
}