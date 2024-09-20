package org.blorp;
import java.util.*;
import java.io.Writer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.minhttp.HTreeNode;

/**
 * This tests out my wildcard path values on http://host/A/_/B/_/C/
 * No handler at A, but handlers at both B & C levels.
 * Wildcards are passed down, so that at B, we have two, and at C, three:
 */
public class HandleDataInPath {
    private final static String[] nodes={"A", "B", "C", "D", "E", "F", "G", "H"};
    private final Templates templates;
    public HandleDataInPath(HTreeNode tree, Templates t){
        this.templates=t;
        tree.create("A")
            .create()
            .create("B")
            .create((req, resp, elems)-> print(resp, elems))
            .create("C")
            .create((req, resp, elems)-> print(resp, elems))
            .create("D")
            .create((req, resp, elems)-> print(resp, elems))
            .create("E")
            .create((req, resp, elems)-> print(resp, elems))
            .create("F")
            .create((req, resp, elems)-> print(resp, elems))
            .create("G")
            .create((req, resp, elems)-> print(resp, elems))
            .create("H")
            .create((req, resp, elems)-> print(resp, elems))
            ;
    }

    private final void print(HttpServletResponse r, List<String> elems) {
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