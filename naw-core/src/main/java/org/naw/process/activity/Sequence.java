package org.naw.process.activity;

import java.util.concurrent.atomic.AtomicBoolean;

import org.naw.process.Process;
import org.naw.process.pipeline.DefaultPipeline;
import org.naw.process.pipeline.Pipeline;
import org.naw.process.pipeline.Sink;

public class Sequence implements Activity, Sink {

    private final String name;

    private DefaultPipeline pipeline;

    private ActivityContext ctx;

    private Activity[] activities;

    private final AtomicBoolean destroyed;

    public Sequence(String name) {
        this.name = name;

        destroyed = new AtomicBoolean(false);
    }

    public String getName() {
        return name;
    }

    public void setActivities(Activity... activities) {
        this.activities = activities;
    }

    public void init(ActivityContext ctx) throws Exception {
        this.ctx = ctx;

        pipeline = new DefaultPipeline(ctx.getPipeline());
        pipeline.setActivities(activities);
        pipeline.setProcessContext(ctx.getProcessContext());
        pipeline.setSink(this);

        activities = null;

        pipeline.init();
    }

    public void execute(Process process) throws Exception {
        pipeline.execute(process);
    }

    public void sunk(Pipeline pipeline, Process process) {
        ctx.execute(process);
    }

    public void destroy() {
        if (!destroyed.compareAndSet(false, true)) {
            return;
        }

        // destroy pipeline
        pipeline.destroy();

        // let gc do its work
        pipeline = null;
        ctx = null;
        activities = null;
    }
}
