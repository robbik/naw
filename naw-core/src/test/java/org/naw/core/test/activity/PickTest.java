package org.naw.core.test.activity;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.naw.core.DefaultProcessContext;
import org.naw.core.Process;
import org.naw.core.ProcessContext;
import org.naw.core.ProcessState;
import org.naw.core.activity.Activity;
import org.naw.core.activity.Empty;
import org.naw.core.activity.Pick;
import org.naw.core.activity.PickOnMessage;
import org.naw.core.test.MockActivity;
import org.naw.core.test.MockLifeCycleListener;
import org.naw.core.test.MockPartnerLink;
import org.naw.core.util.Selectors;

public class PickTest {

	private static DefaultProcessContext newProcessContext(
			Activity... activities) {
		DefaultProcessContext dpctx = new DefaultProcessContext("bb");
		dpctx.addPartnerLink("xx", new MockPartnerLink());
		dpctx.setActivities(activities);

		return dpctx;
	}

	private static PickOnMessage newPickOnMessage(Pick parent, boolean oneWay,
			String operation) {
		PickOnMessage pom = new PickOnMessage(parent);
		pom.setOneWay(oneWay);
		pom.setCorrelationAttribute("processId");
		pom.setPartnerLink("xx");
		pom.setVariable("data");
		pom.setOperation(operation);
		pom.setActivities(new Empty("EMPTY_" + operation));

		return pom;
	}

	private static Pick newActivity(boolean createInstance, boolean oneWay) {
		Pick act = new Pick("ab");

		act.setCreateInstance(createInstance);
		act.setOnMessages(Arrays.asList(newPickOnMessage(act, oneWay, "xx1"),
				newPickOnMessage(act, oneWay, "xx2")));

		return act;
	}

	@Test
	public void testInit() throws Exception {
		Activity act = newActivity(true, true);

		ProcessContext procctx = newProcessContext(act);
		procctx.init();

		assertEquals(1,
				((MockPartnerLink) procctx.findPartnerLink("xx"))
						.subscriptions("xx1"));
		assertEquals(1,
				((MockPartnerLink) procctx.findPartnerLink("xx"))
						.subscriptions("xx2"));

		procctx.shutdown();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInitIfPartnerLinkNotFound() throws Exception {
		Activity act = newActivity(true, true);

		DefaultProcessContext procctx = newProcessContext(act);
		procctx.removePartnerLink("xx");

		procctx.init();
	}

	@Test
	public void testMessageReceivedOnBranch1IfCreateInstanceIsTrueAndOneWayIsTrue()
			throws Exception {
		MockLifeCycleListener mock = new MockLifeCycleListener();

		ProcessContext procctx = newProcessContext(newActivity(true, true));
		procctx.getSelector().add(mock, Selectors.ALL_SELECTIONS);

		procctx.init();

		mock.expectProcessCreated();
		mock.expectProcessStateChanged(ProcessState.BEFORE, "EMPTY_xx1");
		mock.expectProcessStateChanged(ProcessState.AFTER, "EMPTY_xx1");
		mock.expectProcessStateChanged(ProcessState.AFTER, "ab");
		mock.expectProcessTerminated();

		MockPartnerLink mpl = (MockPartnerLink) procctx.findPartnerLink("xx");
		mpl.publish("testUnit", "xx1",
				Collections.singletonMap("response", (Object) "OK"));

		mock.assertExpected(3, TimeUnit.SECONDS);

		procctx.shutdown();
	}

	@Test
	public void testMessageReceivedOnBranch2IfCreateInstanceIsTrueAndOneWayIsTrue()
			throws Exception {
		MockLifeCycleListener mock = new MockLifeCycleListener();

		ProcessContext procctx = newProcessContext(newActivity(true, true));
		procctx.getSelector().add(mock, Selectors.ALL_SELECTIONS);

		procctx.init();

		mock.expectProcessCreated();
		mock.expectProcessStateChanged(ProcessState.BEFORE, "EMPTY_xx2");
		mock.expectProcessStateChanged(ProcessState.AFTER, "EMPTY_xx2");
		mock.expectProcessStateChanged(ProcessState.AFTER, "ab");
		mock.expectProcessTerminated();

		MockPartnerLink mpl = (MockPartnerLink) procctx.findPartnerLink("xx");
		mpl.publish("testUnit", "xx2",
				Collections.singletonMap("response", (Object) "OK"));

		mock.assertExpected(3, TimeUnit.SECONDS);

		procctx.shutdown();
	}

	@Test
	public void testMessageReceivedOnBranch1IfCreateInstanceIsFalseAndProcessNotFound()
			throws Exception {
		MockLifeCycleListener mock = new MockLifeCycleListener();

		ProcessContext procctx = newProcessContext(newActivity(false, true));
		procctx.getSelector().add(mock, Selectors.ALL_SELECTIONS);

		procctx.init();

		mock.expectProcessCreated();

		MockPartnerLink mpl = (MockPartnerLink) procctx.findPartnerLink("xx");

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("response", "OK");
		data.put("processId", "-1");

		mpl.publish("testUnit", "xx1", data);

		mock.assertNotExpected(3, TimeUnit.SECONDS);
		assertEquals(0, procctx.findAllProcesses().size());

		procctx.shutdown();
	}

	@Test
	public void testMessageReceivedOnBranch1IfCreateInstanceIsFalseAndProcessFoundButStateIsNotValid()
			throws Exception {
		MockLifeCycleListener mock = new MockLifeCycleListener();

		ProcessContext procctx = newProcessContext(newActivity(false, true));
		procctx.getSelector().add(mock, Selectors.ALL_SELECTIONS);

		procctx.init();

		Process proc = procctx.newProcess();
		proc.noFireEventUpdate(ProcessState.BEFORE, new MockActivity(
				"EMPTY_xx1"));

		MockPartnerLink mpl = (MockPartnerLink) procctx.findPartnerLink("xx");

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("response", "OK");
		data.put("processId", proc.getId());

		mock.expectProcessStateChanged(ProcessState.AFTER, "EMPTY_xx1");

		mpl.publish("testUnit", "xx1", data);

		mock.assertNotExpected(3, TimeUnit.SECONDS);

		procctx.shutdownNow();
	}

	@Test
	public void testMessageReceivedOnBranch1IfCreateInstanceIsFalseAndProcessFoundAndStateIsValid()
			throws Exception {
		MockLifeCycleListener mock = new MockLifeCycleListener();

		ProcessContext procctx = newProcessContext(newActivity(false, true));
		procctx.getSelector().add(mock, Selectors.ALL_SELECTIONS);

		procctx.init();

		Process proc = procctx.newProcess();
		proc.noFireEventUpdate(ProcessState.SLEEP, new MockActivity("ab"));

		MockPartnerLink mpl = (MockPartnerLink) procctx.findPartnerLink("xx");

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("response", "OK");
		data.put("processId", proc.getId());

		mock.expectProcessStateChanged(ProcessState.BEFORE, "EMPTY_xx1");
		mock.expectProcessStateChanged(ProcessState.AFTER, "EMPTY_xx1");
		mock.expectProcessStateChanged(ProcessState.AFTER, "ab");
		mock.expectProcessTerminated();

		mpl.publish("testUnit", "xx1", data);

		mock.assertExpected(3, TimeUnit.SECONDS);

		procctx.shutdown();
	}
}
