package org.blorp;
import java.io.Writer;
import java.io.IOException;
import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.function.LongFunction;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.minhttp.Handlers;

/** Attempts to get some halfway decent memory stats. */
public class HandleDebugMemory {
    private final Templates templates;
    public HandleDebugMemory(Templates t){
        this.templates = t;
    }

    public Handlers.MyHandler getHandler() {
        return (req, resp, path)-> templates.wrap(resp, w -> print(w));
    }

    private void print(Writer writer) throws Exception {
        writer.append(
            "<h2>Memory stats in kilobytes</h2>"
            +"<table><tr>"
            +"<th>Name<th>Initial<th>Used<th>Committed<th>Max</th>"
            +"</tr>"
        );
        MemoryMXBean mainMemBean=ManagementFactory.getMemoryMXBean();
        print(writer, "Heap Usage", mainMemBean.getHeapMemoryUsage());
        print(writer, "Non-Heap Usage", mainMemBean.getNonHeapMemoryUsage());
        ManagementFactory.getMemoryPoolMXBeans().stream()
            .sorted((MemoryPoolMXBean mb1, MemoryPoolMXBean mb2) ->
                mb1.getName().compareTo(mb2.getName())
            )
            .forEach((MemoryPoolMXBean mem) ->
                print(writer, mem.getName(), mem.getUsage())
            );
        writer.append("</table><br>");
        writer.append("<b>Buffer pool(s): </b><br>");
        for (BufferPoolMXBean b: ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class))
            writer.append(
                String.format("Buffer pool - %s: %,d <br>\n", b.getName(), b.getMemoryUsed()/1024)
            );
        getThreads(writer);
    }

    private static void getThreads(Writer writer) throws Exception {
        writer.append("<br><h2>Threads</h2>\n");
        ThreadGroup root = null, temp=Thread.currentThread().getThreadGroup();
        while ((temp=temp.getParent())!=null)
            root=temp;
        getThreads(
            writer, root,
            new ThreadGroup[root.activeGroupCount()], new Thread[root.activeCount()],
            ""
        );
    }
    private static void getThreads(
            Writer writer, ThreadGroup group,
            ThreadGroup[] groupBuf, Thread[] threadBuf,
            String indent
        ) throws Exception {
        writer.append(indent).append("ThreadGroup ").append(group.getName()).append("<br>\n");
        for (int i=0; i<groupBuf.length; i++) groupBuf[i]=null;
        for (int i=0; i<threadBuf.length; i++) threadBuf[i]=null;
        indent+="&nbsp&nbsp&nbsp;";
        group.enumerate(threadBuf, false);
        printThreads(writer, indent, threadBuf);
        group.enumerate(groupBuf, false);
        List<ThreadGroup> groups=Arrays.asList(groupBuf);
        for (ThreadGroup tg: groups)
            if (tg!=null)
                getThreads(writer, tg, groupBuf, threadBuf, indent);
    }
    private static void printThreads(Writer writer, String indent, Thread[] threadBuf)
        throws Exception {
        Stream.of(threadBuf)
            .filter(__ -> __ != null)
            .sorted((a, b)->a.getName().compareTo(b.getName()))
            .forEach((Thread t)->Except.run(()->{
                writer.append(indent).append("Thread: ").append(t.getName()).append("<br>\n");
            }));
    }
    private static void print(Writer writer, String name, MemoryUsage mu) {
        Except.run(()->{
            writer.append("<tr><td>").append(name);
            final long init=mu.getInit(), used=mu.getUsed(),
                commit=mu.getCommitted(), max=mu.getMax();
            writer.append(
                String.format(
                    "<td> %,d <td> %,d <td> %,d <td> %,d </tr>",
                    ifdef(
                        number -> number==-1
                            ?-1
                            :number/1024L,
                        init, used, commit, max
                    )
                )
            );
        });
    }

    private static Object[] ifdef(LongFunction<Long> lf, long... nums) {
        Object[] result=new Object[nums.length];
        for (int i=0; i<nums.length; i++)
            result[i]=lf.apply(nums[i]);
        return result;
    }
}
