package org.naw.utest.process.activity;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.naw.process.DefaultProcessContext;
import org.naw.process.Process;
import org.naw.process.ProcessState;
import org.naw.process.activity.Activity;
import org.naw.process.activity.Merge;
import org.naw.process.pipeline.DefaultPipeline;
import org.naw.process.pipeline.Pipeline;

public class MergeTest {

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

        Merge act = new Merge(name);
        assertEquals(name, act.getName());
    }

    @Test
    public void testInit() throws Exception {
        Merge act = new Merge("a");
        act.setMergeFromVariable("from");
        act.setMergeToVariable("to");

        newPipeline(act).init().destroy();
    }

    @Test
    public void testExecuteIfFromVariableIsNotFound() throws Exception {
        Merge act = new Merge("a");
        act.setMergeFromVariable("from");
        act.setMergeToVariable("to");

        Pipeline pipeline = newPipeline(act).init();

        Map<String, Object> mapTo = Collections.singletonMap("abcd", (Object) "1234");

        Process process = pipeline.getProcessContext().newProcess();
        process.getMessage().set("to", mapTo);

        pipeline.execute(process);

        assertEquals(act, process.getActivity());
        assertEquals(ProcessState.AFTER_ACTIVITY, process.getState());

        assertEquals(mapTo, process.getMessage().get("to"));
        assertEquals(1, process.getMessage().getDeclaredVariables().size());

        pipeline.destroy();
    }

    @Test
    public void testExecuteIfToVariableIsNotFound() throws Exception {
        Merge act = new Merge("a");
        act.setMergeFromVariable("from");
        act.setMergeToVariable("to");

        Pipeline pipeline = newPipeline(act).init();

        Map<String, Object> mapFrom = Collections.singletonMap("abcd", (Object) "1234");

        Process process = pipeline.getProcessContext().newProcess();
        process.getMessage().set("from", mapFrom);

        pipeline.execute(process);

        assertEquals(act, process.getActivity());
        assertEquals(ProcessState.AFTER_ACTIVITY, process.getState());

        assertEquals(mapFrom, process.getMessage().get("from"));
        assertEquals(mapFrom, process.getMessage().get("to"));

        assertEquals(2, process.getMessage().getDeclaredVariables().size());

        pipeline.destroy();
    }

    @Test
    public void testExecuteIfValuesAreNotEquals() throws Exception {
        Merge act = new Merge("a");
        act.setMergeFromVariable("from");
        act.setMergeToVariable("to");

        Pipeline pipeline = newPipeline(act).init();

        Map<String, Object> mapFrom = Collections.singletonMap("abcd", (Object) "1234");

        Map<String, Object> mapTo = new HashMap<String, Object>();
        mapTo.put("abcde", "2222");

        Map<String, Object> mapr = new HashMap<String, Object>();
        mapr.put("abcde", "2222");
        mapr.put("abcd", "1234");

        Process process = pipeline.getProcessContext().newProcess();
        process.getMessage().set("from", mapFrom);
        process.getMessage().set("to", mapTo);

        pipeline.execute(process);

        assertEquals(act, process.getActivity());
        assertEquals(ProcessState.AFTER_ACTIVITY, process.getState());

        assertEquals(mapFrom, process.getMessage().get("from"));
        assertEquals(mapr, process.getMessage().get("to"));
        assertEquals(2, process.getMessage().getDeclaredVariables().size());

        pipeline.destroy();
    }

    @Test
    public void testExecuteIfValuesAreEquals() throws Exception {
        Merge act = new Merge("a");
        act.setMergeFromVariable("from");
        act.setMergeToVariable("to");

        Pipeline pipeline = newPipeline(act).init();

        Map<String, Object> mapFrom = Collections.singletonMap("abcd", (Object) "1234");

        Map<String, Object> mapTo = new HashMap<String, Object>();
        mapTo.put("abcd", "2222");

        Map<String, Object> mapr = new HashMap<String, Object>();
        mapr.put("abcd", "1234");

        Process process = pipeline.getProcessContext().newProcess();
        process.getMessage().set("from", mapFrom);
        process.getMessage().set("to", mapTo);

        pipeline.execute(process);

        assertEquals(act, process.getActivity());
        assertEquals(ProcessState.AFTER_ACTIVITY, process.getState());

        assertEquals(mapFrom, process.getMessage().get("from"));
        assertEquals(mapr, process.getMessage().get("to"));
        assertEquals(2, process.getMessage().getDeclaredVariables().size());

        pipeline.destroy();
    }

    @Test
    public void testDestroyBeforeInit() throws Exception {
        Merge act = new Merge("a");
        act.destroy();
    }

    @Test
    public void testDestroyAfterInit() throws Exception {
        Merge act = new Merge("a");
        act.setMergeFromVariable("from");
        act.setMergeToVariable("to");

        newPipeline(act).init().destroy();
    }

    @Test
    public void testDoubleDestroyAfterInit() throws Exception {
        Merge act = new Merge("a");
        act.setMergeFromVariable("from");
        act.setMergeToVariable("to");

        newPipeline(act).init().destroy().destroy();
    }
}
