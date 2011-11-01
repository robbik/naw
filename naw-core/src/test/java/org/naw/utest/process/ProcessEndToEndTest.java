package org.naw.utest.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.naw.partnerLink.MessageEvent;
import org.naw.partnerLink.PartnerLinkListener;
import org.naw.process.DefaultProcessContext;
import org.naw.process.activity.Invoke;
import org.naw.process.activity.Merge;
import org.naw.process.activity.Receive;
import org.naw.process.activity.Reply;
import org.naw.test.MockPartnerLink;

public class ProcessEndToEndTest {

    private MockPartnerLink partnerLink;

    private volatile MessageEvent received;

    private CountDownLatch latch;

    private static Receive createReceive() {
        Receive act = new Receive("receive");
        act.setOperation("process");
        act.setPartnerLink("mock");
        act.setOneWay(false);
        act.setCreateInstance(true);
        act.setCorrelationAttribute("rrn");
        act.setVariable("data");

        return act;
    }

    private static Merge createMerge() {
        Merge act = new Merge("merge");
        act.setMergeFromVariable("response-1");
        act.setMergeToVariable("data");

        return act;
    }

    private static Invoke createInvoke() {
        Invoke act = new Invoke("reply");
        act.setOperation("3rd_party");
        act.setPartnerLink("mock");
        act.setRequestVariable("request-1");
        act.setResponseVariable("response-1");

        return act;
    }

    private static Invoke createInvoke2() {
        Invoke act = new Invoke("reply");
        act.setOperation("3rd_party2");
        act.setPartnerLink("mock");
        act.setRequestVariable("request-2");
        act.setResponseVariable("response-2");

        return act;
    }

    private static Merge createMerge2() {
        Merge act = new Merge("merge2");
        act.setMergeFromVariable("response-2");
        act.setMergeToVariable("data");

        return act;
    }

    private static Reply createReply() {
        Reply act = new Reply("reply");
        act.setOperation("process");
        act.setPartnerLink("mock");
        act.setVariable("data");

        return act;
    }

    @Before
    public void before() throws Exception {
        partnerLink = new MockPartnerLink();

        partnerLink.subscribe("process_callback", new PartnerLinkListener() {
            public void messageReceived(MessageEvent e) {
                received = e;

                if (latch != null) {
                    latch.countDown();
                }
            }
        });

        partnerLink.subscribe("3rd_party", new PartnerLinkListener() {
            public void messageReceived(MessageEvent e) {
                Map<String, Object> msg = new HashMap<String, Object>();
                msg.put("responseCode", "00");

                partnerLink.send("3rd_party_callback", "3rd_party", e.getSource(), msg);
            }
        });

        partnerLink.subscribe("3rd_party2", new PartnerLinkListener() {
            public void messageReceived(MessageEvent e) {
                Map<String, Object> msg = new HashMap<String, Object>();
                msg.put("responseCode", "99");

                partnerLink.send("3rd_party2_callback", "3rd_party2", e.getSource(), msg);
            }
        });
    }

    @After
    public void after() throws Exception {
        // do nothing
    }

    @Test
    public void requestResponseTest() throws Exception {
        DefaultProcessContext processctx = new DefaultProcessContext("test1");
        processctx.addPartnerLink("mock", partnerLink);
        processctx.setActivities(createReceive(), createReply());

        processctx.init();

        Map<String, Object> msg = new HashMap<String, Object>();
        msg.put("/data/xxx/text()", "abcdef");

        received = null;
        latch = new CountDownLatch(1);

        partnerLink.publish("process", "requestResponseSimpleTest()", msg);

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        Thread.sleep(1000);
        assertTrue(processctx.getProcesses().isEmpty());

        processctx.destroy();

        assertNotNull(received);
        assertEquals("requestResponseSimpleTest()", received.getDestination());
        assertEquals(msg, received.getValues());
    }

    @Test
    public void oneWayTest() throws Exception {
        Receive act = new Receive("receive");
        act.setOperation("process");
        act.setPartnerLink("mock");
        act.setOneWay(true);
        act.setCreateInstance(true);
        act.setCorrelationAttribute("rrn");
        act.setVariable("data");

        DefaultProcessContext processctx = new DefaultProcessContext("test2");
        processctx.addPartnerLink("mock", partnerLink);
        processctx.setActivities(act, createReply());

        processctx.init();

        Map<String, Object> msg = new HashMap<String, Object>();
        msg.put("/data/xxx/text()", "azsw");

        received = null;

        partnerLink.publish("process", "requestResponseSimpleTest()", msg);

        Thread.sleep(5000);

        assertTrue(processctx.getProcesses().isEmpty());

        processctx.destroy();

        assertNull(received);
    }

    @Test
    public void requestResponseInvokeTest() throws Exception {
        DefaultProcessContext processctx = new DefaultProcessContext("test3");
        processctx.addPartnerLink("mock", partnerLink);
        processctx.setActivities(createReceive(), createInvoke(), createMerge(), createReply());

        processctx.init();

        Map<String, Object> msg = new HashMap<String, Object>();
        msg.put("/data/xxx/text()", "zzzz");

        received = null;
        latch = new CountDownLatch(1);

        partnerLink.publish("process", "requestResponseSimpleTest()", msg);

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        Thread.sleep(1000);
        assertTrue(processctx.getProcesses().isEmpty());

        processctx.destroy();

        assertNotNull(received);
        assertEquals("requestResponseSimpleTest()", received.getDestination());
        assertEquals(msg, received.getValues());
        assertEquals("00", received.getValues().get("responseCode"));
    }

    @Test
    public void requestResponseInvokeInvoke2Test() throws Exception {
        DefaultProcessContext processctx = new DefaultProcessContext("test3");
        processctx.addPartnerLink("mock", partnerLink);
        processctx.setActivities(createReceive(), createInvoke(), createMerge(), createInvoke2(), createMerge2(),
                createReply());

        processctx.init();

        Map<String, Object> msg = new HashMap<String, Object>();
        msg.put("/data/xxx/text()", "aaa");

        received = null;
        latch = new CountDownLatch(1);

        partnerLink.publish("process", "requestResponseSimpleTest()", msg);

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        Thread.sleep(1000);
        assertTrue(processctx.getProcesses().isEmpty());

        processctx.destroy();

        assertNotNull(received);
        assertEquals("requestResponseSimpleTest()", received.getDestination());
        assertEquals(msg, received.getValues());
        assertEquals("99", received.getValues().get("responseCode"));
    }
}
