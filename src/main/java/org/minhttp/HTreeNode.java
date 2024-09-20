package org.minhttp;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Use the create() method to add intermediate path nodes.
 * Use the put() method to add a "leaf node", a handler at the bottom of its own path tree.
 * The only real difference between the two is that create() returns a NEW handler (for the created) node,
 * so that you can add lower-level ones; put returns the original handler, not the one created.
 */
public class HTreeNode {
    private final static Logger logger=LoggerFactory.getLogger(HTreeNode.class);

    private final String label;
    private final MyHandler myHandler;
    private final MyHandler notFoundHandler;
    private final Map<String, HTreeNode> map=new HashMap<>();
    private final boolean doAll;
    private HTreeNode wildcarder=null;

    HTreeNode(String label, MyHandler notFoundHandler) {
        this(label, false, notFoundHandler, null);
    }
    HTreeNode(String label, boolean doAll, MyHandler notFoundHandler, MyHandler myHandler) {
        this.label=label;
        this.doAll=doAll;
        this.notFoundHandler=notFoundHandler;
        this.myHandler=myHandler;
    }

    /** @return A description of this handler in terms of its path in the overall tree. */
    public @Override String toString() {return label;}

    /**
     * Maps pathElem to handler.
     * @param pathElem A path element below the current one
     * @param handler Handles the HTTP request to pathElem
     * @return The original node, so that more things can be mapped at the same level.
     */
    public HTreeNode put(String pathElem, MyHandler handler) {
        create(pathElem, handler);
        return this;
    }

    /**
     * Maps pathElem to handler. This is the same as put(), but returns
     * the <em>new</em> node instead of its parent.
     *
     * @param pathElem A path element below the current one
     * @param handler Handles the HTTP request to pathElem
     * @return A new tree node, which may be used to add path handlers
     *   for paths below pathElem.
     */
    public HTreeNode create(String pathElem, MyHandler handler) {
        return createInternal(pathElem, handler);
    }

    /**
     * Creates a new node for pathElem, but without any handler mapped to it,
     * because there's nothing useful to be done at that level.
     * More nodes can be added to the new node for paths below it.
     * @param pathElem A path element below the current one
     * @return A new tree node, which may be used to add path handlers
     *   for paths below pathElem.
     */
    public HTreeNode create(String pathElem) {
        return createInternal(pathElem, null);
    }


    /**
     * This creates a new wildcard node, which will absorb anything in the next path element
     * down. That path element will be passed in the path input list to handler. This path
     * element will also be in the list of values passed to handlers attached to nodes further
     * down in the path hierarchy, if any exist.
     *
     * Note that wildcard handlers only handle a single path element; they don't automatically
     * handle other nodes further down.
     *
     * @param handler Handles the HTTP request to the wildcard path.
     * @return A new tree node, which may be used to add more path handlers, but
     *   probably shouldn't.
     */
    public HTreeNode create(MyHandler handler) {
        return createInternal(null, handler);
    }

    /**
     * Does the same as create(MyHandler), but without any handler associated with the wildcard
     * path element itself; after this one would create/put against the newly created node, so
     * that lower-level handler(s) receive the path element of the wildcard.
     *
     * So you could do new HTree().create("people").create().create("dogs").create(handlerX) and
     * then The URI /people/Bob/dogs/Sparky would result in handlerX receiving ["Bob", "Sparky"]
     * in its input list of path elements.
     *
     * @return A new tree node, which may be used to add path handlers.
     *   for paths below pathElem.
     */
    public HTreeNode create() {
        return createInternal(null, null);
    }

    /**
     * Creates a wildcard node (like create(MyHandler)) that *also* handles all paths below it.
     * An example usage would be creating a handler that maps URLs onto a directory of files.
     * @param handler Target that receives HTTP requests
     * @return The node, which is probably not useful to you.
     */
    public HTreeNode all(MyHandler handler) {
        return createInternal(null, true, handler);
    }


    private HTreeNode createInternal(String pathElem, MyHandler handler) {
        return createInternal(pathElem, false, handler);
    }
    private HTreeNode createInternal(String pathElem, boolean doAll, MyHandler handler) {
        HTreeNode mt=new HTreeNode(
            label+"/"+(pathElem==null ?"*" :pathElem), doAll, notFoundHandler, handler
        );
        if (pathElem!=null) {
            if (pathElem.contains("/"))
                throw new IllegalArgumentException("Should not be a \"/\" in: "+pathElem);
            if (map.containsKey(pathElem))
                throw new IllegalArgumentException("Already mapped: "+pathElem);
            map.put(pathElem, mt);
        }
        else
        if (wildcarder!=null)
            throw new RuntimeException("Already have a wildcard node");
        else
            wildcarder=mt;
        return mt;
    }

    /**
     * Requests are delegated to here from HTree.
     */
    void handle(
            HttpServletRequest req,
            HttpServletResponse resp,
            List<String> list,
            List<String> pathValues,
            MyHandler filter
        ) throws Exception {
        if (pathValues==null)
            pathValues=new ArrayList<>(list.size());
        if (list.size()==0) {
            if (myHandler!=null) {
                logger.debug("{}: Found", label);
                withFilter(req, resp, pathValues, filter, myHandler);
            } else {
                logger.warn("{}: Wildcard without handler", label);
                withFilter(req, resp, pathValues, filter, notFoundHandler);
            }
            return;
        }

        String elem=list.remove(0);
        HTreeNode mht=map.get(elem);
        if (mht!=null) {
            logger.debug("{}: matched node", elem);
            mht.handle(req, resp, list, pathValues, filter);
        } else if (wildcarder!=null) {
            logger.debug("{}: pass to wildcard node", label);
            pathValues.add(elem);
            wildcarder.handle(req, resp, list, pathValues, filter);
        } else if (doAll) {
            logger.debug("{}: doAll handler found", elem);
            pathValues.add(elem);
            for (String s: list)
                pathValues.add(s);
            withFilter(req, resp, pathValues, filter, myHandler);
        } else {
            logger.debug(label+"/["+elem+"]: Not found");
            withFilter(req, resp, pathValues, filter, notFoundHandler);
        }
    }

    private void withFilter(
            HttpServletRequest req,
            HttpServletResponse resp,
            List<String> pathValues,
            MyHandler filter,
            MyHandler handler
        ) throws Exception {
        if (filter!=null);
            filter.handle(req, resp, pathValues);
        handler.handle(req, resp, pathValues);
    }

}
