package org.naw.process.activity;

import org.naw.process.Process;

public class Script implements Activity {

    private final String name;

    private ActivityContext ctx;

    private Handler handler;

    public Script(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void init(ActivityContext ctx) throws Exception {
        this.ctx = ctx;
    }

    public void execute(Process process) throws Exception {
        handler.handle(process);
        ctx.execute(process);
    }

    public void destroy() {
        handler = null;
        ctx = null;
    }

    public static interface Handler {
        void handle(Process process) throws Exception;
    }
}
