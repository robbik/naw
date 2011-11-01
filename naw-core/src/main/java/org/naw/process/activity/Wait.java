package org.naw.process.activity;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.naw.process.Process;
import org.naw.process.ProcessState;
import org.naw.util.Timeout;
import org.naw.util.Timer;
import org.naw.util.TimerTask;

public class Wait implements TimerTask, Activity {

    private final String name;

    private ActivityContext ctx;

    private Timer timer;

    private long deadline;

    private long duration;

    private final AtomicBoolean destroyed;

    public Wait(String name) {
        this.name = name;

        destroyed = new AtomicBoolean(false);
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

    public void init(ActivityContext ctx) throws Exception {
        this.ctx = ctx;

        timer = ctx.getProcessContext().getTimer();
    }

    public void execute(Process process) throws Exception {
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

    public void run(Timeout timeout) throws Exception {
        Process process = ctx.getProcessContext().getProcess(timeout.getProcessId());
        if (process == null) {
            return;
        }

        if (process.compareAndSet(ProcessState.BEFORE_ACTIVITY, this, ProcessState.AFTER_ACTIVITY, this)) {
            ctx.execute(process);
        }
    }

    public void destroy() {
        if (!destroyed.compareAndSet(false, true)) {
            return;
        }

        ctx = null;
        timer = null;
    }
}
