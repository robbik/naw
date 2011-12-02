package org.naw.core.test.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.naw.core.ProcessState.AFTER;
import static org.naw.core.ProcessState.BEFORE;
import static org.naw.core.ProcessState.ON;
import static org.naw.core.ProcessState.SLEEP;
import static org.naw.core.listener.LifeCycleListener.Category.PROCESS_CONTEXT_DESTROYED;
import static org.naw.core.listener.LifeCycleListener.Category.PROCESS_CONTEXT_INITIALIZED;
import static org.naw.core.listener.LifeCycleListener.Category.PROCESS_CREATED;
import static org.naw.core.listener.LifeCycleListener.Category.PROCESS_STATE_CHANGE;
import static org.naw.core.listener.LifeCycleListener.Category.PROCESS_TERMINATED;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.naw.core.DefaultProcessContext;
import org.naw.core.Process;
import org.naw.core.activity.Invoke;
import org.naw.core.activity.Merge;
import org.naw.core.activity.Receive;
import org.naw.core.activity.Reply;
import org.naw.core.listener.AutoSaveProcess;
import org.naw.core.partnerLink.MessageEvent;
import org.naw.core.partnerLink.PartnerLinkListener;
import org.naw.core.storage.FileStorage;
import org.naw.core.storage.InMemoryStorage;
import org.naw.core.storage.Storage;
import org.naw.core.test.MockActivity;
import org.naw.core.test.MockLifeCycleListener;
import org.naw.core.test.MockPartnerLink;

public class StorageTest {

	private MockPartnerLink partnerLink;

	private MockLifeCycleListener mock;

	private Storage fileStorage;

	private Storage inMemoryStorage;

	private volatile String source;

	private static Receive createReceive(boolean oneWay) {
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

	private static Merge createMerge() {
		Merge act = new Merge("merge");
		act.setFromVariable("response-1");
		act.setToVariable("data");

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
		partnerLink.setExecutorService(Executors.newCachedThreadPool());

		partnerLink.subscribe("process_callback", new PartnerLinkListener() {
			public void messageReceived(MessageEvent e) {
				System.out.println("PROCESS DONE");
			}
		});

		partnerLink.subscribe("3rd_party", new PartnerLinkListener() {
			public void messageReceived(MessageEvent e) {
				source = e.getSource();
			}
		});

		mock = new MockLifeCycleListener();

		inMemoryStorage = new InMemoryStorage();
		fileStorage = new FileStorage("target/test-storage");
	}

	private DefaultProcessContext createProcessContext(boolean inMemory)
			throws Exception {
		DefaultProcessContext procctx = new DefaultProcessContext("test1");
		procctx.addPartnerLink("mock", partnerLink);

		procctx.setActivities(createReceive(false), createInvoke(),
				createMerge(), createReply());

		procctx.getSelector().add(mock, PROCESS_CONTEXT_INITIALIZED,
				PROCESS_CONTEXT_DESTROYED, PROCESS_CREATED,
				PROCESS_STATE_CHANGE, PROCESS_TERMINATED);

		if (inMemory) {
			procctx.setStorage(inMemoryStorage);
		} else {
			procctx.setStorage(fileStorage);
		}

		procctx.getSelector().add(new AutoSaveProcess(),
				PROCESS_STATE_CHANGE, PROCESS_TERMINATED);

		procctx.init();

		return procctx;
	}

	@Test
	public void testInvokeDownUpReceive() throws Exception {
		DefaultProcessContext procctx;

		// 1st] invoke
		procctx = createProcessContext(true);

		mock.expectProcessCreated();
		mock.expectProcessStateChanged(ON, new MockActivity("receive"));
		mock.expectProcessStateChanged(AFTER, new MockActivity("receive"));
		mock.expectProcessStateChanged(BEFORE, new MockActivity("invoke"));
		mock.expectProcessStateChanged(SLEEP, new MockActivity("invoke"));

		Map<String, Object> msg = new HashMap<String, Object>();
		msg.put("/data/xxx/text()", "abcdef");
		partnerLink.publish("#testInvokeDownUpReceive", "process", msg);

		mock.assertExpected(5, TimeUnit.SECONDS);

		// 2nd] down
		assertEquals(1, procctx.findAllProcesses().size());

		Process proc = procctx.findAllProcesses().iterator().next();
		String pid = proc.getId();

		// 3rd] destroy and force gc
		procctx.destroy();

		proc = null;
		procctx = null;

		System.gc();

		mock.reset();

		// 4th] sleep
		Thread.sleep(2000);

		// 5th] up
		procctx = createProcessContext(true);

		proc = procctx.getStorage().find(pid);
		assertNotNull(proc);

		procctx.activate(proc);

		assertEquals(1, procctx.findAllProcesses().size());

		// 6th] receive
		mock.expectProcessStateChanged(ON, new MockActivity("invoke"));
		mock.expectProcessStateChanged(AFTER, new MockActivity("invoke"));
		mock.expectProcessStateChanged(BEFORE, new MockActivity("merge"));
		mock.expectProcessStateChanged(AFTER, new MockActivity("merge"));
		mock.expectProcessStateChanged(BEFORE, new MockActivity("reply"));
		mock.expectProcessStateChanged(AFTER, new MockActivity("reply"));
		mock.expectProcessTerminated();

		msg = new HashMap<String, Object>();
		msg.put("responseCode", "00");
		partnerLink.send("3rd_party", source, "3rd_party_callback", msg);

		mock.assertExpected(5, TimeUnit.SECONDS);

		// 7th] destroy
		procctx.destroy();
	}

	@Ignore
	@Test
	public void testInvokeDown() throws Exception {
		DefaultProcessContext procctx;

		// 1st] invoke
		procctx = createProcessContext(false);

		mock.expectProcessCreated();
		mock.expectProcessStateChanged(ON, new MockActivity("receive"));
		mock.expectProcessStateChanged(AFTER, new MockActivity("receive"));
		mock.expectProcessStateChanged(BEFORE, new MockActivity("invoke"));
		mock.expectProcessStateChanged(SLEEP, new MockActivity("invoke"));

		Map<String, Object> msg = new HashMap<String, Object>();
		msg.put("/data/xxx/text()", "abcdef");
		partnerLink.publish("#testInvokeDownUpReceive", "process", msg);

		mock.assertExpected(5, TimeUnit.SECONDS);

		// 2nd] down
		assertEquals(1, procctx.findAllProcesses().size());

		// 3rd] destroy and force gc
		procctx.destroy();
	}

	@Ignore
	@Test
	public void testUpReceive() throws Exception {
		String pid = "5c43d44c98e849e3acd0bf4f563db8b7.1";
		DefaultProcessContext procctx;

		// 5th] up
		procctx = createProcessContext(false);

		Process proc = procctx.getStorage().find(pid);
		assertNotNull(proc);

		procctx.activate(proc);

		assertEquals(1, procctx.findAllProcesses().size());

		// 6th] receive
		mock.expectProcessStateChanged(ON, new MockActivity("invoke"));
		mock.expectProcessStateChanged(AFTER, new MockActivity("invoke"));
		mock.expectProcessStateChanged(BEFORE, new MockActivity("merge"));
		mock.expectProcessStateChanged(AFTER, new MockActivity("merge"));
		mock.expectProcessStateChanged(BEFORE, new MockActivity("reply"));
		mock.expectProcessStateChanged(AFTER, new MockActivity("reply"));
		mock.expectProcessTerminated();

		Map<String, Object> msg = new HashMap<String, Object>();
		msg.put("responseCode", "00");

		partnerLink.send("3rd_party", pid, "3rd_party_callback", msg);

		mock.assertExpected(5, TimeUnit.SECONDS);

		// 7th] destroy
		procctx.destroy();
	}
}
