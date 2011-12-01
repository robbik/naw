package org.naw.core.test.activity;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.naw.core.DefaultProcessContext;
import org.naw.core.Process;
import org.naw.core.ProcessState;
import org.naw.core.activity.Activity;
import org.naw.core.activity.Empty;
import org.naw.core.pipeline.DefaultPipeline;
import org.naw.core.pipeline.Pipeline;

public class EmptyTest {

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

        Empty empty = new Empty(name);
        assertEquals(name, empty.getName());
    }

    @Test
    public void testInit() throws Exception {
        Pipeline p = newPipeline(new Empty("a"));
        p.init();
    }

    @Test
    public void testExecute() throws Exception {
        Empty act = new Empty("a");

        Pipeline p = newPipeline(act);
        p.init();

        Map<String, Object> map = Collections.singletonMap("abcd", (Object) "1234");

        Process process = p.getProcessContext().newProcess();
        process.getMessage().set("abcde", map);

        act.execute(process);

        assertEquals(act, process.getActivity());
        assertEquals(ProcessState.AFTER, process.getState());

        assertEquals(map, process.getMessage().get("abcde"));
        assertEquals(1, process.getMessage().getVariables().size());

        p.destroy();
    }

    @Test
    public void testDestroyBeforeInit() throws Exception {
        Pipeline p = newPipeline(new Empty("a"));
        p.destroy();
    }

    @Test
    public void testDestroyAfterInit() throws Exception {
        Pipeline p = newPipeline(new Empty("a"));
        p.init();
        p.destroy();
    }

    @Test
    public void testDoubleDestroyAfterInit() throws Exception {
        Pipeline p = newPipeline(new Empty("a"));
        p.init();
        p.destroy();
        p.destroy();
    }
}
