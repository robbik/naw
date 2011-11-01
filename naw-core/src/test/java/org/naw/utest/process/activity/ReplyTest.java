package org.naw.utest.process.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.naw.partnerLink.MessageEvent;
import org.naw.partnerLink.PartnerLinkListener;
import org.naw.process.DefaultProcessContext;
import org.naw.process.Process;
import org.naw.process.activity.Activity;
import org.naw.process.activity.Reply;
import org.naw.process.pipeline.DefaultPipeline;
import org.naw.test.MockPartnerLink;

public class ReplyTest {

    private static DefaultProcessContext newProcessContext() {
        DefaultProcessContext dpctx = new DefaultProcessContext("bb");
        dpctx.addPartnerLink("xx", new MockPartnerLink());

        return dpctx;
    }

    private static DefaultPipeline newPipeline(Activity... activities) {
        DefaultPipeline pipeline = new DefaultPipeline();
        pipeline.setActivities(activities);
        pipeline.setProcessContext(newProcessContext());
        pipeline.setSink(null);

        return pipeline;
    }

    private static Reply newActivity(boolean createInstance, boolean oneWay) {
        Reply act = new Reply("ab");

        act.setPartnerLink("xx");
        act.setOperation("op");
        act.setVariable("data");

        return act;
    }

    @Test
    public void testGetName() {
        String name = UUID.randomUUID().toString();

        Reply act = new Reply(name);
        assertEquals(name, act.getName());
    }

    @Test
    public void testInit() throws Exception {
        Reply act = newActivity(true, true);

        DefaultPipeline pipeline = newPipeline(act);
        pipeline.init();

        pipeline.destroy();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInitIfPartnerLinkNotFound() throws Exception {
        Reply act = newActivity(true, true);
        act.setPartnerLink("zz");

        newPipeline(act).init();
    }

    @Test
    public void testExecuteIfExchangeIsFound() throws Exception {
        Reply act = newActivity(true, true);

        DefaultPipeline pipeline = newPipeline(act);
        pipeline.init();

        final AtomicReference<Map<String, Object>> ref = new AtomicReference<Map<String, Object>>();
        final CountDownLatch latch = new CountDownLatch(1);

        pipeline.getProcessContext().getPartnerLink("xx").subscribe("op_callback", new PartnerLinkListener() {

            public void messageReceived(MessageEvent e) {
                ref.set(e.getValues());

                latch.countDown();
            }
        });

        Process process = pipeline.getProcessContext().newProcess();
        process.getMessage().declare("data");
        process.getMessage().setValue("data", "initial", "77");

        process.setAttribute("EXCHANGE$op", "zz");

        act.execute(process);

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        assertNotNull(ref.get());

        assertEquals(1, ref.get().size());
        assertEquals("77", ref.get().get("initial"));

        pipeline.destroy();
    }

    @Test
    public void testExecuteIfExchangeIsNotFound() throws Exception {
        Reply act = newActivity(true, true);

        DefaultPipeline pipeline = newPipeline(act);
        pipeline.init();

        final AtomicReference<Map<String, Object>> ref = new AtomicReference<Map<String, Object>>();
        final CountDownLatch latch = new CountDownLatch(1);

        pipeline.getProcessContext().getPartnerLink("xx").subscribe("op_callback", new PartnerLinkListener() {

            public void messageReceived(MessageEvent e) {
                ref.set(e.getValues());

                latch.countDown();
            }
        });

        Process process = pipeline.getProcessContext().newProcess();
        process.getMessage().declare("data");
        process.getMessage().setValue("data", "initial", "77");

        act.execute(process);

        assertFalse(latch.await(5, TimeUnit.SECONDS));

        assertNull(ref.get());

        pipeline.destroy();
    }

    @Test
    public void testDestroyBeforeInit() throws Exception {
        newActivity(true, true).destroy();
    }

    @Test
    public void testDestroyAfterInit() throws Exception {
        newPipeline(newActivity(true, true)).init().destroy();
    }

    @Test
    public void testDoubleDestroyAfterInit() throws Exception {
        newPipeline(newActivity(true, true)).init().destroy().destroy();
    }
}
