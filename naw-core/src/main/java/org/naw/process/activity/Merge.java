package org.naw.process.activity;

import java.util.Map;

import org.naw.exchange.Message;
import org.naw.process.Process;

public class Merge implements Activity {

    private final String name;

    private ActivityContext ctx;

    private String mergeFromVariable;

    private String mergeToVariable;

    public Merge(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setMergeFromVariable(String mergeFromVariable) {
        this.mergeFromVariable = mergeFromVariable;
    }

    public void setMergeToVariable(String mergeToVariable) {
        this.mergeToVariable = mergeToVariable;
    }

    public void init(ActivityContext ctx) throws Exception {
        this.ctx = ctx;
    }

    public void execute(Process process) throws Exception {
        Message message = process.getMessage();

        Map<String, Object> from = message.get(mergeFromVariable);

        if (from != null) {
            Map<String, Object> to = message.get(mergeToVariable);

            if (to == null) {
                message.set(mergeToVariable, from);
            } else {
                to.putAll(from);
            }
        }

        ctx.execute(process);
    }

    public void destroy() {
        ctx = null;
    }
}
