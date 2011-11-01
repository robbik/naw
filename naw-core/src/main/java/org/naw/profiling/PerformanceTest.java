package org.naw.profiling;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.naw.partnerLink.MessageEvent;
import org.naw.partnerLink.PartnerLinkListener;
import org.naw.process.DefaultProcessContext;
import org.naw.process.activity.Invoke;
import org.naw.process.activity.Receive;
import org.naw.process.activity.Reply;
import org.naw.test.MockPartnerLink;

public class PerformanceTest {

    private MockPartnerLink partnerLink;

    private CountDownLatch latch;

    private ExecutorService executor;

    private CountDownLatch progressMonitorBegin;

    private volatile long globalBegin;

    private AtomicInteger sendCount;

    private AtomicInteger receiveCount;

    private static Receive createFirstReceive(boolean oneWay) {
        Receive act = new Receive("receive");
        act.setOperation("process");
        act.setPartnerLink("mock");
        act.setOneWay(oneWay);
        act.setCreateInstance(true);
        act.setCorrelationAttribute("rrn");
        act.setVariable("data");

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

    private static Reply createReply() {
        Reply act = new Reply("reply");
        act.setOperation("process");
        act.setPartnerLink("mock");
        act.setVariable("data");

        return act;
    }

    public void before() throws Exception {
        sendCount = new AtomicInteger(0);
        receiveCount = new AtomicInteger(0);

        progressMonitorBegin = new CountDownLatch(1);

        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    progressMonitorBegin.await();
                } catch (InterruptedException e) {
                    return;
                }

                while (true) {
                    long elapsed = System.currentTimeMillis() - globalBegin;
                    int n = receiveCount.get();

                    if (elapsed > 0) {
                        System.out.println("send: " + sendCount.get() + ", receive: " + n + ", elapsed: "
                                + (elapsed / 1000.0f) + " sec, throughput: " + (n * 1000.0f / elapsed) + " tps       ");
                    } else {
                        System.out.println("send: " + sendCount.get() + ", receive: " + n
                                + ", elapsed: N/A  sec, throughput: N/A tps       ");
                    }

                    try {
                        Thread.sleep(600000);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        t.setName("ProgressMonitor");
        t.setDaemon(true);

        t.start();

        executor = Executors.newCachedThreadPool();

        partnerLink = new MockPartnerLink();
        partnerLink.setExecutorService(executor);

        partnerLink.subscribe("process_callback", new PartnerLinkListener() {
            public void messageReceived(MessageEvent e) {
                Map<String, Object> msg = e.getValues();
                msg.put("/data/endTransactionTime/text()", Long.valueOf(System.currentTimeMillis()));

                receiveCount.incrementAndGet();
                latch.countDown();
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

    public void after() throws Exception {
        executor.shutdown();
    }

    public void requestResponseInvokeInvoke2Test() throws Exception {
        DefaultProcessContext processctx = new DefaultProcessContext("test3");
        processctx.addPartnerLink("mock", partnerLink);
        processctx.setActivities(createFirstReceive(false), createInvoke(), createInvoke2(), createReply());

        processctx.init();

        final int n = 100;

        latch = new CountDownLatch(n);

        progressMonitorBegin.countDown();

        globalBegin = System.currentTimeMillis();

        for (int i = 0; i < n; ++i) {
            Map<String, Object> msg = new HashMap<String, Object>();
            msg.put("/data/number/text()", Integer.valueOf(i));
            msg.put("/data/beginTransactionTime/text()", Long.valueOf(System.currentTimeMillis()));

            partnerLink.publish("process", "requestResponseSimpleTest()", msg);

            sendCount.incrementAndGet();
        }

        latch.await();

        long globalEnd = System.currentTimeMillis();

        processctx.destroy();

        System.out.println();
        System.out.println();
        System.out.println("SUMMARY");
        System.out.println("===========================");
        System.out.println("[x] Number of Data Samples  = sent " + n + ", received " + receiveCount.get());
        System.out.println("[x] Times Elapsed           = " + ((globalEnd - globalBegin) / 1000.0f) + " sec");
        System.out.println("[x] Throughput              = " + (n * 1000.0f / (globalEnd - globalBegin)) + " tps");
        System.out.println();
    }

    public static void main(String[] args) throws Exception {
        PerformanceTest pt = new PerformanceTest();
        pt.before();

        pt.requestResponseInvokeInvoke2Test();

        pt.after();

        System.out.println("halted!");
    }
}
