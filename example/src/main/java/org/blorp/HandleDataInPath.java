package org.blorp;
import java.util.*;
import java.io.Writer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.minhttp.Handlers;

/**
 * This tests out my wildcard path values on http://host/A/_/B/_/C/
 * No handler at A, but handlers at both B & C levels.
 * Wildcards are passed down, so that at B, we have two, and at C, three:
 */
public class HandleDataInPath {
    private final static String[] nodes={"A", "B", "C", "D", "E", "F", "G", "H"};
    private final Templates templates;
    public HandleDataInPath(Handlers tree, Templates t){
        this.templates=t;
        tree.add("GET", "/data/A/*/B/*", this::print)
            .add("GET", "/data/A/*/B/*/C/*", this::print)
            .add("GET", "/data/A/*/B/*/C/*/D/*", this::print)
            .add("GET", "/data/A/*/B/*/C/*/D/*/E/*", this::print)
            .add("GET", "/data/A/*/B/*/C/*/D/*/E/*/F/*", this::print)
            .add("GET", "/data/A/*/B/*/C/*/D/*/E/*/F/*/G/*", this::print)
            .add("GET", "/data/A/*/B/*/C/*/D/*/E/*/F/*/G/*/H/*", this::print)
            ;
    }

    private final void print(HttpServletRequest req, HttpServletResponse r, List<String> elems) {
        templates.wrap(r, writer->{
            int len=elems.size();
            if (elems.size() > 2)
                writer.append("<br>")
                    .append("<a href='../../"
                        +elems.get(elems.size()-2)
                        +"'>up</a><br>");
            else
                writer.append("<br><br>");
            for (int i=0; i<len; i++)
                writer.append("<br>").append(nodes[i]).append(": ")
                    .append(elems.get(i));
            writer.append("<br><br>");
            if (len<nodes.length)
                writer
                    .append("<a href='")
                    .append(elems.get(len-1))
                    .append("/")
                    .append(nodes[len])
                    .append("/")
                    .append(UUID.randomUUID().toString().substring(0,8))
                    .append("'>down<a><br>");
        });
    }


}