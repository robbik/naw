package org.naw.core.test.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.naw.core.DefaultProcessContext;
import org.naw.core.Process;
import org.naw.core.ProcessContext;
import org.naw.core.ProcessState;
import org.naw.core.activity.AbstractActivity;
import org.naw.core.activity.Activity;
import org.naw.core.activity.Receive;
import org.naw.core.compensation.CompensationHandler;
import org.naw.core.exchange.Message;
import org.naw.core.pipeline.DefaultPipeline;
import org.naw.core.test.MockPartnerLink;

public class ReceiveTest {

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

	private static Receive newActivity(boolean createInstance, boolean oneWay) {
		Receive act = new Receive("ab");

		act.setPartnerLink("xx");
		act.setOperation("xx");
		act.setVariable("data");
		act.setCorrelationAttribute("processId");
		act.setCreateInstance(createInstance);
		act.setOneWay(oneWay);

		return act;
	}

	private static Activity newFinalActivity(final CountDownLatch latch,
			final AtomicReference<Message> msg,
			final AtomicReference<String> source) {

		Activity act = new AbstractActivity("abcd") {

			public void execute(Process process) throws Exception {
				if (msg != null) {
					msg.set(process.getMessage());
				}

				if (source != null) {
					source.set(process
							.getAttribute("EXCHANGE$xx", String.class));
				}

				if (latch != null) {
					latch.countDown();
				}

				ctx.execute(process);
			}
		};

		return act;
	}

	private static Activity newErrorActivity() {
		Activity act = new AbstractActivity("abcd2") {

			public void execute(Process process) throws Exception {
				throw new Exception("FAILURE");
			}
		};

		return act;
	}

	@Test
	public void testGetName() {
		String name = UUID.randomUUID().toString();

		Receive act = new Receive(name);
		assertEquals(name, act.getName());
	}

	@Test
	public void testInit() throws Exception {
		Receive act = newActivity(true, true);

		DefaultPipeline pipeline = newPipeline(act);
		pipeline.init();

		assertTrue(((MockPartnerLink) pipeline.getProcessContext()
				.findPartnerLink("xx")).subscribed("xx", act));

		pipeline.destroy();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInitIfPartnerLinkNotFound() throws Exception {
		Receive act = newActivity(true, true);
		act.setPartnerLink("zz");

		newPipeline(act).init();
	}

	@Test
	public void testExecute() throws Exception {
		Receive act = newActivity(true, true);

		DefaultPipeline pipeline = newPipeline(act);
		pipeline.init();

		Process process = pipeline.getProcessContext().newProcess();
		process.getMessage().declare("data");

		act.execute(process);

		assertNull(process.getActivity());

		assertEquals(0, process.getMessage().get("data").size());
		assertEquals(1, process.getMessage().getVariables().size());

		pipeline.destroy();
	}

	@Test
	public void testMessageReceivedCreateInstanceIsTrueAndOneWayIsTrue()
			throws Exception {
		CountDownLatch latch = new CountDownLatch(1);
		AtomicReference<Message> msgref = new AtomicReference<Message>();
		AtomicReference<String> sourceref = new AtomicReference<String>();

		DefaultPipeline pipeline = newPipeline(newActivity(true, true),
				newFinalActivity(latch, msgref, sourceref));

		pipeline.init();

		MockPartnerLink mpl = (MockPartnerLink) pipeline.getProcessContext()
				.findPartnerLink("xx");
		mpl.publish("testUnit", "xx",
				Collections.singletonMap("response", (Object) "OK"));

		assertTrue(latch.await(5, TimeUnit.SECONDS));

		Message msg = msgref.get();
		assertNotNull(msg);

		assertEquals(1, msg.getVariables().size());
		assertEquals(1, msg.get("data").size());
		assertEquals("OK", msg.get("data").get("response"));

		assertNull(sourceref.get());

		pipeline.destroy();
	}

	@Test
	public void testMessageReceivedCreateInstanceIsTrueAndOneWayIsFalse()
			throws Exception {
		CountDownLatch latch = new CountDownLatch(1);
		AtomicReference<Message> msgref = new AtomicReference<Message>();
		AtomicReference<String> sourceref = new AtomicReference<String>();

		DefaultPipeline pipeline = newPipeline(newActivity(true, false),
				newFinalActivity(latch, msgref, sourceref));

		pipeline.init();

		MockPartnerLink mpl = (MockPartnerLink) pipeline.getProcessContext()
				.findPartnerLink("xx");
		mpl.publish("testUnit", "xx",
				Collections.singletonMap("response", (Object) "OK"));

		assertTrue(latch.await(5, TimeUnit.SECONDS));

		Message msg = msgref.get();
		assertNotNull(msg);

		assertEquals(1, msg.getVariables().size());
		assertEquals(1, msg.get("data").size());
		assertEquals("OK", msg.get("data").get("response"));

		assertEquals("testUnit", sourceref.get());

		pipeline.destroy();
	}

	@Test
	public void testMessageReceivedCreateInstanceIsFalseAndInstanceNotFound()
			throws Exception {
		CountDownLatch latch = new CountDownLatch(1);

		DefaultPipeline pipeline = newPipeline(newActivity(false, false),
				newFinalActivity(latch, null, null));
		pipeline.init();

		Map<String, Object> map = new ConcurrentHashMap<String, Object>();
		map.put("response", "OK");
		map.put("processId", "-1");

		MockPartnerLink mpl = (MockPartnerLink) pipeline.getProcessContext()
				.findPartnerLink("xx");
		mpl.publish("testUnit", "xx", map);

		assertFalse(latch.await(5, TimeUnit.SECONDS));

		pipeline.destroy();
	}

	@Test
	public void testMessageReceivedCreateInstanceIsFalseAndOneWayIsTrueAndInstanceIsFoundAndStateIsValid()
			throws Exception {
		CountDownLatch latch = new CountDownLatch(1);
		AtomicReference<Message> msgref = new AtomicReference<Message>();
		AtomicReference<String> sourceref = new AtomicReference<String>();

		Activity act = newActivity(false, true);

		DefaultPipeline pipeline = newPipeline(act,
				newFinalActivity(latch, msgref, sourceref));

		pipeline.init();

		Process proc = pipeline.getProcessContext().newProcess();
		proc.getMessage().declare("data2");
		proc.getMessage().get("data2").put("initial", "312");

		proc.update(ProcessState.BEFORE, act);

		Map<String, Object> map = new ConcurrentHashMap<String, Object>();
		map.put("response", "OK");
		map.put("processId", proc.getId());

		MockPartnerLink mpl = (MockPartnerLink) pipeline.getProcessContext()
				.findPartnerLink("xx");
		mpl.publish("testUnit", "xx", map);

		assertTrue(latch.await(5, TimeUnit.SECONDS));

		Message msg = msgref.get();
		assertNotNull(msg);

		assertEquals(2, msg.getVariables().size());
		assertEquals(msg.get("data").toString(), 2, msg.get("data").size());
		assertEquals("312", msg.get("data2").get("initial"));
		assertEquals("OK", msg.get("data").get("response"));
		assertEquals(proc.getId(), msg.get("data").get("processId"));

		assertNull(sourceref.get());

		pipeline.destroy();
	}

	@Test
	public void testMessageReceivedCreateInstanceIsFalseAndOneWayIsFalseAndInstanceIsFoundAndStateIsValid()
			throws Exception {
		CountDownLatch latch = new CountDownLatch(1);
		AtomicReference<Message> msgref = new AtomicReference<Message>();
		AtomicReference<String> sourceref = new AtomicReference<String>();

		Activity act = newActivity(false, false);

		DefaultPipeline pipeline = newPipeline(act,
				newFinalActivity(latch, msgref, sourceref));

		pipeline.init();

		Process proc = pipeline.getProcessContext().newProcess();
		proc.getMessage().declare("data2");
		proc.getMessage().get("data2").put("initial", "312");

		proc.update(ProcessState.BEFORE, act);

		Map<String, Object> map = new ConcurrentHashMap<String, Object>();
		map.put("response", "OK");
		map.put("processId", proc.getId());

		MockPartnerLink mpl = (MockPartnerLink) pipeline.getProcessContext()
				.findPartnerLink("xx");
		mpl.publish("testUnit", "xx", map);

		assertTrue(latch.await(5, TimeUnit.SECONDS));

		Message msg = msgref.get();
		assertNotNull(msg);

		assertEquals(2, msg.getVariables().size());
		assertEquals(msg.get("data").toString(), 2, msg.get("data").size());
		assertEquals("312", msg.get("data2").get("initial"));
		assertEquals("OK", msg.get("data").get("response"));
		assertEquals(proc.getId(), msg.get("data").get("processId"));

		assertEquals("testUnit", sourceref.get());

		pipeline.destroy();
	}

	@Test
	public void testMessageReceivedCreateInstanceIsFalseAndOneWayIsTrueAndInstanceIsFoundAndStateIsInvalid()
			throws Exception {
		CountDownLatch latch = new CountDownLatch(1);
		AtomicReference<Message> msgref = new AtomicReference<Message>();
		AtomicReference<String> sourceref = new AtomicReference<String>();

		Activity act = newActivity(false, true);

		DefaultPipeline pipeline = newPipeline(act,
				newFinalActivity(latch, msgref, sourceref));

		pipeline.init();

		Process proc = pipeline.getProcessContext().newProcess();
		proc.getMessage().declare("data2");
		proc.getMessage().get("data2").put("initial", "312");

		proc.update(ProcessState.INIT, null);

		Map<String, Object> map = new ConcurrentHashMap<String, Object>();
		map.put("response", "OK");
		map.put("processId", proc.getId());

		MockPartnerLink mpl = (MockPartnerLink) pipeline.getProcessContext()
				.findPartnerLink("xx");
		mpl.publish("testUnit", "xx", map);

		assertFalse(latch.await(5, TimeUnit.SECONDS));

		assertNull(msgref.get());
		assertNull(sourceref.get());

		pipeline.destroy();
	}

	@Test
	public void testMessageReceivedCreateInstanceIsFalseAndOneWayIsFalseAndInstanceIsFoundAndStateIsInvalid()
			throws Exception {
		CountDownLatch latch = new CountDownLatch(1);
		AtomicReference<Message> msgref = new AtomicReference<Message>();
		AtomicReference<String> sourceref = new AtomicReference<String>();

		Activity act = newActivity(false, false);

		DefaultPipeline pipeline = newPipeline(act,
				newFinalActivity(latch, msgref, sourceref));

		pipeline.init();

		Process proc = pipeline.getProcessContext().newProcess();
		proc.getMessage().declare("data2");
		proc.getMessage().get("data2").put("initial", "312");

		proc.update(ProcessState.INIT, null);

		Map<String, Object> map = new ConcurrentHashMap<String, Object>();
		map.put("response", "OK");
		map.put("processId", proc.getId());

		MockPartnerLink mpl = (MockPartnerLink) pipeline.getProcessContext()
				.findPartnerLink("xx");
		mpl.publish("testUnit", "xx", map);

		assertFalse(latch.await(5, TimeUnit.SECONDS));

		assertNull(msgref.get());
		assertNull(sourceref.get());

		pipeline.destroy();
	}

	@Test
	public void testMessageReceivedifNextProcessIsFailed() throws Exception {
		CountDownLatch latch = new CountDownLatch(1);
		AtomicReference<Message> msgref = new AtomicReference<Message>();
		AtomicReference<String> sourceref = new AtomicReference<String>();

		final AtomicReference<Throwable> errorRef = new AtomicReference<Throwable>(
				null);

		DefaultPipeline pipeline = newPipeline(newActivity(true, false),
				newFinalActivity(latch, msgref, sourceref), newErrorActivity());
		pipeline.register(new CompensationHandler() {

			public void compensate(Process process, Throwable error) {
				errorRef.set(error);
			}
		});

		pipeline.init();

		MockPartnerLink mpl = (MockPartnerLink) pipeline.getProcessContext()
				.findPartnerLink("xx");
		mpl.publish("testUnit", "xx",
				Collections.singletonMap("response", (Object) "OK"));

		assertTrue(latch.await(5, TimeUnit.SECONDS));

		Thread.sleep(2000);

		Message msg = msgref.get();
		assertNotNull(msg);

		assertEquals(1, msg.getVariables().size());
		assertEquals(1, msg.get("data").size());
		assertEquals("OK", msg.get("data").get("response"));

		assertEquals("testUnit", sourceref.get());

		assertNotNull(errorRef.get());

		pipeline.destroy();
	}

	@Test(expected = NullPointerException.class)
	public void testDestroyBeforeInit() throws Exception {
		Receive act = newActivity(true, true);
		act.destroy();
	}

	@Test
	public void testDestroyAfterInit() throws Exception {
		Receive act = newActivity(true, true);

		DefaultPipeline pipeline = newPipeline(act);
		ProcessContext procctx = pipeline.getProcessContext();

		pipeline.init();
		pipeline.destroy();

		assertFalse(((MockPartnerLink) procctx.findPartnerLink("xx"))
				.subscribed("xx", act));
	}

	@Test
	public void testDoubleDestroyAfterInit() throws Exception {
		Receive act = newActivity(true, true);

		DefaultPipeline pipeline = newPipeline(act);

		pipeline.init();
		pipeline.destroy();

		act.destroy();
	}
}
