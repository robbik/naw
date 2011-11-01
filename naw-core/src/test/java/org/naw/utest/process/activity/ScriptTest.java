package org.naw.utest.process.activity;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Test;
import org.naw.process.DefaultProcessContext;
import org.naw.process.Process;
import org.naw.process.ProcessState;
import org.naw.process.activity.Activity;
import org.naw.process.activity.Script;
import org.naw.process.activity.Script.Handler;
import org.naw.process.pipeline.DefaultPipeline;
import org.naw.process.pipeline.Pipeline;

public class ScriptTest {

    private static DefaultPipeline newPipeline(Activity... activities) {
        DefaultPipeline pipeline = new DefaultPipeline();
        pipeline.setActivities(activities);
        pipeline.setProcessContext(new DefaultProcessContext("bb"));
        pipeline.setSink(null);

        return pipeline;
    }

    @Test
    public void testGetName() {
        String name = UUID.randomUUID().toString();

        Script act = new Script(name);
        assertEquals(name, act.getName());
    }

    @Test
    public void testInit() throws Exception {
        Script act = new Script("a");
        newPipeline(act).init();
    }

    @Test
    public void testExecute() throws Exception {
        Script act = new Script("a");
        act.setHandler(new Handler() {
            public void handle(Process process) throws Exception {
                process.getMessage().setValue("data", "response", "OK");
            }
        });

        Pipeline p = newPipeline(act).init();

        Process process = p.getProcessContext().newProcess();
        process.getMessage().declare("data");

        p.execute(process);

        assertEquals(act, process.getActivity());
        assertEquals(ProcessState.AFTER_ACTIVITY, process.getState());

        assertEquals(1, process.getMessage().get("data").size());
        assertEquals("OK", process.getMessage().getValue("data", "response"));
        assertEquals(1, process.getMessage().getDeclaredVariables().size());

        p.destroy();
    }

    @Test
    public void testDestroyBeforeInit() throws Exception {
        Script act = new Script("a");
        act.destroy();
    }

    @Test
    public void testDestroyAfterInit() throws Exception {
        Script act = new Script("a");
        newPipeline(act).init().destroy();
    }

    @Test
    public void testDoubleDestroyAfterInit() throws Exception {
        Script act = new Script("a");
        newPipeline(act).init().destroy().destroy();
    }
}
