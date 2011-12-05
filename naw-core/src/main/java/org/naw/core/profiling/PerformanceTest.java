package org.naw.core.profiling;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.naw.core.DefaultProcessContext;
import org.naw.core.activity.Invoke;
import org.naw.core.activity.Receive;
import org.naw.core.activity.Reply;
import org.naw.core.partnerLink.MessageEvent;
import org.naw.core.partnerLink.PartnerLinkListener;
import org.naw.core.test.MockPartnerLink;

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
		Invoke act = new Invoke("invoke");
		act.setOperation("3rd_party");
		act.setPartnerLink("mock");
		act.setRequestVariable("request-1");
		act.setResponseVariable("response-1");

		return act;
	}

	private static Invoke createInvoke2() {
		Invoke act = new Invoke("invoke2");
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
						System.out.println("send: " + sendCount.get()
								+ ", receive: " + n + ", elapsed: "
								+ (elapsed / 1000.0f) + " sec, throughput: "
								+ (n * 1000.0f / elapsed) + " tps       ");
					} else {
						System.out.println("send: "
								+ sendCount.get()
								+ ", receive: "
								+ n
								+ ", elapsed: N/A  sec, throughput: N/A tps       ");
					}

					try {
						Thread.sleep(60000);
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
				Map<String, Object> msg = e.getMessage();
				msg.put("/data/endTransactionTime/text()",
						Long.valueOf(System.currentTimeMillis()));

				receiveCount.incrementAndGet();
				latch.countDown();
			}
		});

		partnerLink.subscribe("3rd_party", new PartnerLinkListener() {
			public void messageReceived(MessageEvent e) {
				Map<String, Object> msg = new HashMap<String, Object>();
				msg.put("responseCode", "00");

				partnerLink.send("3rd_party", e.getSource(),
						"3rd_party_callback", msg);
			}
		});

		partnerLink.subscribe("3rd_party2", new PartnerLinkListener() {
			public void messageReceived(MessageEvent e) {
				Map<String, Object> msg = new HashMap<String, Object>();
				msg.put("responseCode", "99");

				partnerLink.send("3rd_party2", e.getSource(),
						"3rd_party2_callback", msg);
			}
		});
	}

	public void after() throws Exception {
		executor.shutdown();
	}

	public void receiveInvokeInvoke2ReplyTest(final int n) throws Exception {
		DefaultProcessContext processctx = new DefaultProcessContext("test3");
		processctx.addPartnerLink("mock", partnerLink);
		processctx.setActivities(createFirstReceive(false), createInvoke(),
				createInvoke2(), createReply());

		processctx.init();

		latch = new CountDownLatch(n);

		progressMonitorBegin.countDown();

		globalBegin = System.currentTimeMillis();

		for (int i = 0; i < n; ++i) {
			Map<String, Object> msg = new HashMap<String, Object>();
			msg.put("/data/number/text()", Integer.valueOf(i));
			msg.put("/data/beginTransactionTime/text()",
					Long.valueOf(System.currentTimeMillis()));

			partnerLink.publish("requestResponseSimpleTest()", "process", msg);

			sendCount.incrementAndGet();
		}

		latch.await();

		long globalEnd = System.currentTimeMillis();

		processctx.shutdown();

		System.out.println();
		System.out.println();
		System.out.println("SUMMARY");
		System.out.println("===========================");
		System.out.println("[x] Number of Data Samples  = sent " + n
				+ ", received " + receiveCount.get());
		System.out.println("[x] Times Elapsed           = "
				+ ((globalEnd - globalBegin) / 1000.0f) + " sec");
		System.out.println("[x] Throughput              = "
				+ (n * 1000.0f / (globalEnd - globalBegin)) + " tps");
		System.out.println();
	}

	public static void main(String[] args) throws Exception {
		PerformanceTest pt = new PerformanceTest();
		pt.before();

		pt.receiveInvokeInvoke2ReplyTest(args.length == 0 ? 1000 : Integer.parseInt(args[0]));

		pt.after();

		System.out.println("halted!");
	}
}
