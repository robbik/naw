package org.naw.core.test.activity;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.naw.activities.Activity;
import org.naw.core.utils.Selectors;
import org.naw.engine.DefaultProcessContext;
import org.naw.engine.NawProcess;
import org.naw.engine.ProcessInstance;
import org.naw.engine.RelativePosition;
import org.naw.engine.test.MockActivity;
import org.naw.engine.test.MockLifeCycleListener;
import org.naw.engine.test.MockPartnerLink;
import org.naw.tasks.Empty;
import org.naw.tasks.Pick;
import org.naw.tasks.PickOnMessage;

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

		NawProcess procctx = newProcessContext(act);
		procctx.initialize();

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

		procctx.initialize();
	}

	@Test
	public void testMessageReceivedOnBranch1IfCreateInstanceIsTrueAndOneWayIsTrue()
			throws Exception {
		MockLifeCycleListener mock = new MockLifeCycleListener();

		NawProcess procctx = newProcessContext(newActivity(true, true));
		procctx.getSelector().add(mock, Selectors.ALL_SELECTIONS);

		procctx.initialize();

		mock.expectProcessCreated();
		mock.expectProcessStateChanged(RelativePosition.BEFORE, "EMPTY_xx1");
		mock.expectProcessStateChanged(RelativePosition.AFTER, "EMPTY_xx1");
		mock.expectProcessStateChanged(RelativePosition.AFTER, "ab");
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

		NawProcess procctx = newProcessContext(newActivity(true, true));
		procctx.getSelector().add(mock, Selectors.ALL_SELECTIONS);

		procctx.initialize();

		mock.expectProcessCreated();
		mock.expectProcessStateChanged(RelativePosition.BEFORE, "EMPTY_xx2");
		mock.expectProcessStateChanged(RelativePosition.AFTER, "EMPTY_xx2");
		mock.expectProcessStateChanged(RelativePosition.AFTER, "ab");
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

		NawProcess procctx = newProcessContext(newActivity(false, true));
		procctx.getSelector().add(mock, Selectors.ALL_SELECTIONS);

		procctx.initialize();

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

		NawProcess procctx = newProcessContext(newActivity(false, true));
		procctx.getSelector().add(mock, Selectors.ALL_SELECTIONS);

		procctx.initialize();

		ProcessInstance proc = procctx.newProcess();
		proc.noFireEventUpdate(RelativePosition.BEFORE, new MockActivity(
				"EMPTY_xx1"));

		MockPartnerLink mpl = (MockPartnerLink) procctx.findPartnerLink("xx");

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("response", "OK");
		data.put("processId", proc.getId());

		mock.expectProcessStateChanged(RelativePosition.AFTER, "EMPTY_xx1");

		mpl.publish("testUnit", "xx1", data);

		mock.assertNotExpected(3, TimeUnit.SECONDS);

		procctx.shutdownNow();
	}

	@Test
	public void testMessageReceivedOnBranch1IfCreateInstanceIsFalseAndProcessFoundAndStateIsValid()
			throws Exception {
		MockLifeCycleListener mock = new MockLifeCycleListener();

		NawProcess procctx = newProcessContext(newActivity(false, true));
		procctx.getSelector().add(mock, Selectors.ALL_SELECTIONS);

		procctx.initialize();

		ProcessInstance proc = procctx.newProcess();
		proc.noFireEventUpdate(RelativePosition.SLEEP, new MockActivity("ab"));

		MockPartnerLink mpl = (MockPartnerLink) procctx.findPartnerLink("xx");

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("response", "OK");
		data.put("processId", proc.getId());

		mock.expectProcessStateChanged(RelativePosition.BEFORE, "EMPTY_xx1");
		mock.expectProcessStateChanged(RelativePosition.AFTER, "EMPTY_xx1");
		mock.expectProcessStateChanged(RelativePosition.AFTER, "ab");
		mock.expectProcessTerminated();

		mpl.publish("testUnit", "xx1", data);

		mock.assertExpected(3, TimeUnit.SECONDS);

		procctx.shutdown();
	}
}
