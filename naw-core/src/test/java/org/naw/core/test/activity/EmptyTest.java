package org.naw.core.test.activity;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.naw.activities.Activity;
import org.naw.engine.DefaultProcessContext;
import org.naw.engine.ProcessInstance;
import org.naw.engine.RelativePosition;
import org.naw.engine.pipeline.DefaultPipeline;
import org.naw.engine.pipeline.Pipeline;
import org.naw.tasks.Empty;

public class EmptyTest {

    private static DefaultPipeline newPipeline(Activity... activities) {
        DefaultPipeline pipeline = new DefaultPipeline();
        pipeline.setActivities(activities);
        pipeline.setNawProcess(new DefaultProcessContext("bb"));
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
        p.initialize();
    }

    @Test
    public void testExecute() throws Exception {
        Empty act = new Empty("a");

        Pipeline p = newPipeline(act);
        p.initialize();

        Map<String, Object> map = Collections.singletonMap("abcd", (Object) "1234");

        ProcessInstance process = p.getNawProcess().newProcess();
        process.getMessage().set("abcde", map);

        act.next(process);

        assertEquals(act, process.getActivity());
        assertEquals(RelativePosition.AFTER, process.getState());

        assertEquals(map, process.getMessage().get("abcde"));
        assertEquals(1, process.getMessage().getVariables().size());

        p.shutdown();
    }

    @Test
    public void testDestroyBeforeInit() throws Exception {
        Pipeline p = newPipeline(new Empty("a"));
        p.shutdown();
    }

    @Test
    public void testDestroyAfterInit() throws Exception {
        Pipeline p = newPipeline(new Empty("a"));
        p.initialize();
        p.shutdown();
    }

    @Test
    public void testDoubleDestroyAfterInit() throws Exception {
        Pipeline p = newPipeline(new Empty("a"));
        p.initialize();
        p.shutdown();
        p.shutdown();
    }
}
