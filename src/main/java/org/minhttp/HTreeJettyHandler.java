package org.minhttp;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/** This just wraps HTree in a jetty AbstractHandler so it's jetty-compatible. */
public class HTreeJettyHandler extends AbstractHandler {

    private final HTree tree;

    /**
     * @param tree The HTree that will receive requests.
     */
    public HTreeJettyHandler(HTree tree) {
        super();
        this.tree = tree;
    }
    /**
     * Creates a new instance with its own default HTree, which
     * can be obtained via getTree().
     */
    public HTreeJettyHandler() {
        this(new HTree());
    }

    /**
     * @return The HTree this was created with.
     */
    public HTree getTree() {
        return tree;
    }

    /**
     * Overrides the Jetty AbstractHandler's method
     */
    public @Override void handle(
            String target, Request baseRequest,
            HttpServletRequest request, HttpServletResponse response
        ) throws IOException, ServletException {
        tree.handle(request, response, target);
    }
}