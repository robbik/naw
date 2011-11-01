package org.naw.process.activity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.naw.process.Process;
import org.naw.process.ProcessContext;
import org.naw.process.pipeline.DefaultPipeline;
import org.naw.process.pipeline.Pipeline;
import org.naw.process.pipeline.Sink;

public class Flow implements Activity, Sink {

    private final String name;

    private final String attributeName;

    private final AtomicBoolean destroyed;

    private ExecutorService executorService;

    private ActivityContext ctx;

    private Activity[] activities;

    private DefaultPipeline[] pipelines;

    private int npipelines;

    public Flow(String name) {
        this.name = name;

        attributeName = "FLOW$" + name + "#counter";

        destroyed = new AtomicBoolean(false);
    }

    public String getName() {
        return name;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void setActivities(Activity... activities) {
        this.activities = activities;
    }

    public void init(ActivityContext ctx) throws Exception {
        this.ctx = ctx;

        if (activities == null) {
            pipelines = null;
        } else {
            Pipeline parent = ctx.getPipeline();
            ProcessContext procctx = ctx.getProcessContext();

            npipelines = activities.length;
            pipelines = new DefaultPipeline[npipelines];

            for (int i = 0; i < npipelines; ++i) {
                DefaultPipeline pipeline = new DefaultPipeline(parent);
                pipeline.setActivities(activities[i]);
                pipeline.setProcessContext(procctx);
                pipeline.setSink(this);

                pipelines[i] = pipeline;
            }

            activities = null;
        }
    }

    public void execute(Process process) throws Exception {
        if (pipelines != null) {
            process.setAttribute(attributeName, new AtomicInteger(0));

            for (int i = npipelines - 1; i >= 0; --i) {
                executorService.submit(new CompletionHandler(pipelines[i], process));
            }
        }
    }

    public void sunk(Pipeline pipeline, Process process) {
        AtomicInteger c = process.getAttribute(attributeName, AtomicInteger.class);

        if (c.incrementAndGet() == npipelines) {
            ctx.execute(process);
        }
    }

    public void destroy() {
        if (!destroyed.compareAndSet(false, true)) {
            return;
        }

        // destroy pipelines
        if (pipelines != null) {
            for (int i = 0; i < npipelines; ++i) {
                pipelines[i].destroy();
            }
        }

        // gc works
        ctx = null;
        activities = null;
        pipelines = null;
    }

    private static class CompletionHandler implements Runnable {

        private final Pipeline pipeline;

        private final Process process;

        public CompletionHandler(Pipeline pipeline, Process process) {
            this.pipeline = pipeline;
            this.process = process;
        }

        public void run() {
            try {
                pipeline.execute(process);
            } catch (Throwable t) {
                // do nothing
            }
        }
    }
}
