package org.naw.process.activity;

import org.naw.process.Process;

public class Empty implements Activity {

    private final String name;

    private ActivityContext ctx;

    public Empty(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void init(ActivityContext ctx) throws Exception {
        this.ctx = ctx;
    }

    public void execute(Process process) throws Exception {
        ctx.execute(process);
    }

    public void destroy() {
        ctx = null;
    }
}
