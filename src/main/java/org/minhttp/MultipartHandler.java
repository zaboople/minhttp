package org.minhttp;

import java.io.IOException;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.http.MultiPartFormInputStream;

/**
 * This enables multipart handling with Jetty. Doesn't depend on anything else. Mainly
 * this jumps through hoops that Jetty puts in the way and take all day to figure out
 * on your own.
 */
public class MultipartHandler extends AbstractHandler {

    private final AbstractHandler nextHandler;
    private final MultipartConfigElement myConfig;


    /**
     * @param nextHandler Requests will be passed to this after MultipartHandler does its part
     *   to take care of things.
     * @param tmpDir The place to store temporary file data.
     */
    public MultipartHandler(AbstractHandler nextHandler, String tmpDir) {
        super();
        this.nextHandler=nextHandler;
        this.myConfig = new MultipartConfigElement(tmpDir);
    }

    /**
     * For convenience, calls
     *   this(nextHandler, System.getProperty("java.io.tmpdir"))
     * @param nextHandler Requests will be passed to this after MultipartHandler does its part
     *   to take care of things.
     */
    public MultipartHandler(AbstractHandler nextHandler) {
        this(nextHandler, System.getProperty("java.io.tmpdir"));
    }

    public @Override void handle(
            String target, Request baseRequest,
            HttpServletRequest request, HttpServletResponse response
        ) throws IOException, ServletException {

        // Jetty needs a temporary file location for file upload:
        final String contentType = request.getContentType();
        final boolean isMultiPart=
            HttpMethod.POST.is(request.getMethod()) &&
            contentType != null &&
            contentType.startsWith("multipart/form-data");
        if (isMultiPart)
            request.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, myConfig);
        baseRequest.setHandled(true);
        try {
            nextHandler.handle(target, baseRequest, request, response);
        } finally {
            if (!isMultiPart)
                return;
            MultiPartFormInputStream multipartInputStream = (MultiPartFormInputStream)
                request.getAttribute(
                    "org.eclipse.jetty.servlet.MultiPartFile.multiPartInputStream"
                );
            if (multipartInputStream != null)
                multipartInputStream.deleteParts();
        }
    }

}
