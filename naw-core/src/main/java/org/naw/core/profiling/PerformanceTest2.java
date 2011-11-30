package org.naw.core.profiling;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.naw.core.DefaultProcessContext;
import org.naw.core.Process;
import org.naw.core.activity.Receive;
import org.naw.core.activity.Script;
import org.naw.core.activity.Script.Handler;
import org.naw.core.partnerLink.MessageEvent;
import org.naw.core.partnerLink.PartnerLinkListener;
import org.naw.core.test.MockPartnerLink;

public class PerformanceTest2 {

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

	private static Script createScript() {
		Script act = new Script("script");
		act.setHandler(new Handler() {

			public void handle(Process process) throws Exception {
				Map<String, Object> msg = new HashMap<String, Object>();
				msg.put("responseCode", "00");

				process.getMessage().set("response-1", msg);
			}
		});

		return act;
	}

	private static Script createScript2() {
		Script act = new Script("script2");
		act.setHandler(new Handler() {

			public void handle(Process process) throws Exception {
				Map<String, Object> msg = new HashMap<String, Object>();
				msg.put("responseCode", "99");

				process.getMessage().set("response-2", msg);
			}
		});

		return act;
	}

	private static Script createScript3(final AtomicInteger receiveCount,
			final CountDownLatch latch) {
		Script act = new Script("script3");
		act.setHandler(new Handler() {

			public void handle(Process process) throws Exception {
				receiveCount.incrementAndGet();
				latch.countDown();
			}
		});

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

		executor = null; // Executors.newCachedThreadPool();

		partnerLink = new MockPartnerLink();
		partnerLink.setExecutorService(executor);

		partnerLink.subscribe("process_callback", new PartnerLinkListener() {
			public void messageReceived(MessageEvent e) {
				Map<String, Object> msg = e.getMessage();
				msg.put("/data/endTransactionTime/text()",
						Long.valueOf(System.currentTimeMillis()));

			}
		});
	}

	public void after() throws Exception {
		if (executor != null) {
			executor.shutdown();
		}
	}

	public void receiveScriptScript2Script3Test(final int n) throws Exception {
		latch = new CountDownLatch(n);

		DefaultProcessContext processctx = new DefaultProcessContext("test3");
		processctx.addPartnerLink("mock", partnerLink);

		processctx.setActivities(createFirstReceive(true), createScript(),
				createScript2(), createScript3(receiveCount, latch));

		processctx.init();

		progressMonitorBegin.countDown();

		globalBegin = System.currentTimeMillis();
		
		Map<String, Object> sharedMsg = new HashMap<String, Object>();

		for (int i = 0; i < n; ++i) {
			partnerLink.publish("requestResponseSimpleTest()", "process", sharedMsg);
			sendCount.incrementAndGet();
		}

		latch.await();

		long globalEnd = System.currentTimeMillis();

		processctx.destroy();

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
		PerformanceTest2 pt = new PerformanceTest2();
		pt.before();

		pt.receiveScriptScript2Script3Test(args.length == 0 ? 1000 : Integer
				.parseInt(args[0]));

		pt.after();

		System.out.println("halted!");
	}
}
