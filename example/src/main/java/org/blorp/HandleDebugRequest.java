package org.blorp;
import java.io.IOException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
    Prints information about the HTTP request back to the browser. Attempts to avoid
    printing sensitive information such as the Authorization & Cookie headers.
 */
public class HandleDebugRequest {
    private final static String rtn="<br>\n";
    private final Templates templates;
    public HandleDebugRequest(Templates templates) {this.templates=templates;}

    public void handle(
            HttpServletRequest req, HttpServletResponse res, List<String> pathElems
        ) {
        templates.wrap(res, writer -> print(req, writer));
    }

    private static void print(HttpServletRequest request, Writer writer) throws Exception {
        writer.append("<h2>HTTP request info</h2>");
        //writer.append("Path: ").append(target).append(rtn);
        writer.append("<b>Method: </b>")
            .append(request.getMethod()).append(rtn);

        writer.append(rtn+"<b>Headers:</b>"+rtn);
        List<String> headerNames=Collections.list(request.getHeaderNames());
        Collections.sort(headerNames);
        for (String name: headerNames)
            for (String value: Collections.list(request.getHeaders(name)))
                writer.append("&nbsp;&nbsp;").append(name).append(": ")
                    .append("<code>").append(value).append("</code>")
                    .append(rtn);

        debugParameterMap(request, writer);

        // Multipart:
        String contentType=request.getContentType();
        if (contentType!=null && contentType.startsWith("multipart/form-data")) {
            writer.append(rtn+"<b>Multipart data:</b>"+rtn);
            char[] buffer=new char[1024];
            final String dblIndent="&nbsp;&nbsp&nbsp;&nbsp";
            for (Part part: request.getParts()) {
                writer.append(dblIndent+"Part: ").append(part.getName())
                    .append(rtn);
                writer.append(dblIndent+"Name: ")
                    .append(""+part.getSubmittedFileName())
                    .append(rtn);
                writer.append(dblIndent+"Content type: ")
                    .append(part.getContentType())
                    .append(rtn);
                writer.append(dblIndent+"Content: "+rtn);
                writer.append("<pre>");
                try (InputStreamReader reader=new InputStreamReader(part.getInputStream(), StandardCharsets.UTF_8)) {
                    int readed=0;
                    while ((readed=reader.read(buffer, 0, buffer.length))>0){
                        String s=new String(buffer, 0, readed)
                            .replaceAll("&", "&amp;")
                            .replaceAll(">", "&gt;")
                            .replaceAll("<", "&lt;");
                        writer.append(s);
                    }
                }
                writer.append("</pre>");
            }
        } else {
            try (InputStreamReader reader=new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8)) {
                boolean hasData=false;
                char[] buffer=new char[1024];
                int readed=0;
                while ((readed=reader.read(buffer, 0, buffer.length))>0){
                    if (!hasData) {
                        writer.append("<br><pre>\n");
                        hasData=true;
                    }
                    String s=new String(buffer, 0, readed);
                    writer.append(s);
                }
                if (hasData)
                    writer.append("</pre><br>\n");
            }
        }
    }

    public static void debugParameterMap(HttpServletRequest request, Appendable writer) throws IOException {
        writer.append(rtn+"<b>Parameters:</b>"+rtn);
        for (Map.Entry<String, String[]> param: request.getParameterMap().entrySet())
            for (String value: param.getValue()) {
                value=value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
                writer.append("&nbsp;&nbsp;").append(param.getKey()).append(": ")
                    .append(value).append(rtn);
            }
    }


}
