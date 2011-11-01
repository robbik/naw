package org.naw.process.activity;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.naw.process.Process;
import org.naw.process.ProcessState;
import org.naw.process.pipeline.DefaultPipeline;
import org.naw.process.pipeline.Pipeline;
import org.naw.process.pipeline.Sink;
import org.naw.util.Timeout;
import org.naw.util.Timer;
import org.naw.util.TimerTask;

public class PickOnAlarm implements TimerTask, Sink {

    private final Pick parent;

    private final AtomicBoolean destroyed;

    private String name;

    private boolean createInstance;

    private long deadline;

    private long duration;

    private Activity[] activities;

    private ActivityContext ctx;

    private Timer timer;

    private DefaultPipeline pipeline;

    private Timeout timeout;

    public PickOnAlarm(Pick parent) {
        this.parent = parent;

        destroyed = new AtomicBoolean(false);
    }

    public void setName(String name) {
        this.name = parent.getName() + "$" + name;
    }

    public String getName() {
        return name;
    }

    public void setDuration(long duration, TimeUnit unit) {
        this.duration = unit.toMillis(duration);
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public void setActivity(Activity... activities) {
        this.activities = activities;
    }

    public void init(ActivityContext ctx) throws Exception {
        this.ctx = ctx;

        if (activities == null) {
            pipeline = null;
        } else {
            pipeline = new DefaultPipeline(ctx.getPipeline());
            pipeline.setActivities(activities);
            pipeline.setProcessContext(ctx.getProcessContext());
            pipeline.setSink(this);

            activities = null;
        }

        timer = ctx.getProcessContext().getTimer();

        createInstance = parent.isCreateInstance();

        if (createInstance) {
            if (deadline > 0) {
                timeout = timer.newTimeout(this, deadline, null, name);
            } else {
                timeout = timer.newTimeout(this, duration, TimeUnit.MILLISECONDS, null, name);
            }
        }
    }

    public void execute(Process process) throws Exception {
        if (!createInstance) {
            Timeout to;

            if (deadline > 0) {
                to = timer.newTimeout(this, deadline, process.getProcessId(), name);
            } else {
                to = timer.newTimeout(this, duration, TimeUnit.MILLISECONDS, process.getProcessId(), name);
            }

            if (to != null) {
                process.addAlarm(to);
            }
        }
    }

    public void run(Timeout timeout) throws Exception {
        Process process = null;

        if (createInstance) {
            process = ctx.getProcessContext().newProcess();
        } else {
            process = ctx.getProcessContext().getProcess(timeout.getProcessId());
        }

        if (process == null) {
            return;
        }

        if (createInstance
                || process.compareAndSet(ProcessState.BEFORE_ACTIVITY, parent, ProcessState.AFTER_ACTIVITY, parent)) {
            parent.afterExecute(process);

            if (pipeline == null) {
                ctx.execute(process);
            } else {
                pipeline.execute(process);
            }
        }
    }

    public void sunk(Pipeline pipeline, Process process) {
        ctx.execute(process);
    }

    public void destroy() {
        if (!destroyed.compareAndSet(false, true)) {
            return;
        }

        // destroy per process definition timer
        if (timeout != null) {
            timeout.cancel();

            timeout = null;
        }

        // destroy pipeline
        if (pipeline != null) {
            pipeline.destroy();
        }

        // gc works
        activities = null;
        ctx = null;
        timer = null;
        pipeline = null;
    }
}
