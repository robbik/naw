package org.naw.process.activity;

import org.naw.process.Process;

public class Checkpoint implements Activity {

    private final String name;

    public Checkpoint(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void init(ActivityContext ctx) throws Exception {
        // do nothing
    }

    public void execute(Process process) throws Exception {
        process.getProcessContext().dehydrate(process.getProcessId());
    }

    public void destroy() {
        // do nothing
    }
}
