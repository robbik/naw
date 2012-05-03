package org.naw.core.test.activity;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.naw.activities.Activity;
import org.naw.engine.DefaultProcessContext;
import org.naw.engine.ProcessInstance;
import org.naw.engine.RelativePosition;
import org.naw.engine.pipeline.DefaultPipeline;
import org.naw.engine.pipeline.Pipeline;
import org.naw.tasks.Merge;

public class MergeTest {

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

        Merge act = new Merge(name);
        assertEquals(name, act.getName());
    }

    @Test
    public void testInit() throws Exception {
        Merge act = new Merge("a");
        act.setFrom("from");
        act.setTo("to");

        newPipeline(act).initialize().shutdown();
    }

    @Test
    public void testExecuteIfFromVariableIsNotFound() throws Exception {
        Merge act = new Merge("a");
        act.setFrom("from");
        act.setTo("to");

        Pipeline pipeline = newPipeline(act).initialize();

        Map<String, Object> mapTo = Collections.singletonMap("abcd", (Object) "1234");

        ProcessInstance process = pipeline.getNawProcess().newProcess();
        process.getMessage().set("to", mapTo);

        pipeline.next(process);

        assertEquals(act, process.getActivity());
        assertEquals(RelativePosition.AFTER, process.getState());

        assertEquals(mapTo, process.getMessage().get("to"));
        assertEquals(1, process.getMessage().getVariables().size());

        pipeline.shutdown();
    }

    @Test
    public void testExecuteIfToVariableIsNotFound() throws Exception {
        Merge act = new Merge("a");
        act.setFrom("from");
        act.setTo("to");

        Pipeline pipeline = newPipeline(act).initialize();

        Map<String, Object> mapFrom = Collections.singletonMap("abcd", (Object) "1234");

        ProcessInstance process = pipeline.getNawProcess().newProcess();
        process.getMessage().set("from", mapFrom);

        pipeline.next(process);

        assertEquals(act, process.getActivity());
        assertEquals(RelativePosition.AFTER, process.getState());

        assertEquals(mapFrom, process.getMessage().get("from"));
        assertEquals(mapFrom, process.getMessage().get("to"));

        assertEquals(2, process.getMessage().getVariables().size());

        pipeline.shutdown();
    }

    @Test
    public void testExecuteIfValuesAreNotEquals() throws Exception {
        Merge act = new Merge("a");
        act.setFrom("from");
        act.setTo("to");

        Pipeline pipeline = newPipeline(act).initialize();

        Map<String, Object> mapFrom = Collections.singletonMap("abcd", (Object) "1234");

        Map<String, Object> mapTo = new HashMap<String, Object>();
        mapTo.put("abcde", "2222");

        Map<String, Object> mapr = new HashMap<String, Object>();
        mapr.put("abcde", "2222");
        mapr.put("abcd", "1234");

        ProcessInstance process = pipeline.getNawProcess().newProcess();
        process.getMessage().set("from", mapFrom);
        process.getMessage().set("to", mapTo);

        pipeline.next(process);

        assertEquals(act, process.getActivity());
        assertEquals(RelativePosition.AFTER, process.getState());

        assertEquals(mapFrom, process.getMessage().get("from"));
        assertEquals(mapr, process.getMessage().get("to"));
        assertEquals(2, process.getMessage().getVariables().size());

        pipeline.shutdown();
    }

    @Test
    public void testExecuteIfValuesAreEquals() throws Exception {
        Merge act = new Merge("a");
        act.setFrom("from");
        act.setTo("to");

        Pipeline pipeline = newPipeline(act).initialize();

        Map<String, Object> mapFrom = Collections.singletonMap("abcd", (Object) "1234");

        Map<String, Object> mapTo = new HashMap<String, Object>();
        mapTo.put("abcd", "2222");

        Map<String, Object> mapr = new HashMap<String, Object>();
        mapr.put("abcd", "1234");

        ProcessInstance process = pipeline.getNawProcess().newProcess();
        process.getMessage().set("from", mapFrom);
        process.getMessage().set("to", mapTo);

        pipeline.next(process);

        assertEquals(act, process.getActivity());
        assertEquals(RelativePosition.AFTER, process.getState());

        assertEquals(mapFrom, process.getMessage().get("from"));
        assertEquals(mapr, process.getMessage().get("to"));
        assertEquals(2, process.getMessage().getVariables().size());

        pipeline.shutdown();
    }

    @Test
    public void testDestroyBeforeInit() throws Exception {
        Merge act = new Merge("a");
        act.shutdown();
    }

    @Test
    public void testDestroyAfterInit() throws Exception {
        Merge act = new Merge("a");
        act.setFrom("from");
        act.setTo("to");

        newPipeline(act).initialize().shutdown();
    }

    @Test
    public void testDoubleDestroyAfterInit() throws Exception {
        Merge act = new Merge("a");
        act.setFrom("from");
        act.setTo("to");

        Pipeline p = newPipeline(act).initialize();
        p.shutdown();
        p.shutdown();
    }
}
