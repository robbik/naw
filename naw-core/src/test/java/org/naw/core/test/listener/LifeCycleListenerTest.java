package org.naw.core.test.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.naw.activities.Activity;
import org.naw.engine.DefaultProcessContext;
import org.naw.engine.RelativePosition;
import org.naw.engine.listener.LifeCycleListener;
import org.naw.engine.test.MockLifeCycleListener;
import org.naw.engine.test.MockPartnerLink;
import org.naw.links.MessageEvent;
import org.naw.links.PartnerLinkListener;
import org.naw.tasks.Receive;
import org.naw.tasks.Reply;

public class LifeCycleListenerTest {

	private MockPartnerLink partnerLink;

	private MockLifeCycleListener mock;

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
				// do nothing
			}
		});

		mock = new MockLifeCycleListener();
	}

	@Test
	public void requestResponseTest() throws Exception {
		Activity receive = createReceive(false);
		Activity reply = createReply();

		DefaultProcessContext processctx = new DefaultProcessContext("test1");
		processctx.addPartnerLink("mock", partnerLink);
		processctx.setActivities(receive, reply);

		processctx.getSelector().add(mock,
				LifeCycleListener.Category.PROCESS_CONTEXT_INITIALIZED,
				LifeCycleListener.Category.PROCESS_CONTEXT_SHUTDOWN,
				LifeCycleListener.Category.PROCESS_CREATED,
				LifeCycleListener.Category.PROCESS_STATE_CHANGE,
				LifeCycleListener.Category.PROCESS_TERMINATED);

		mock.expectProcessContextInitialized(processctx);

		processctx.initialize();

		mock.assertExpected(5, TimeUnit.SECONDS);

		mock.expectProcessCreated();
		mock.expectProcessStateChanged(RelativePosition.AFTER, receive);
		mock.expectProcessStateChanged(RelativePosition.BEFORE, reply);
		mock.expectProcessStateChanged(RelativePosition.AFTER, reply);
		mock.expectProcessTerminated();

		Map<String, Object> msg = new HashMap<String, Object>();
		msg.put("/data/xxx/text()", "abcdef");

		partnerLink.publish("requestResponseSimpleTest()", "process", msg);

		mock.assertExpected(5, TimeUnit.SECONDS);

		mock.expectProcessContextDestroyed(processctx);

		processctx.shutdown();

		mock.assertExpected(5, TimeUnit.SECONDS);
	}

	@Test
	public void oneWayTest() throws Exception {
		Receive receive = createReceive(true);

		DefaultProcessContext processctx = new DefaultProcessContext("test2");
		processctx.addPartnerLink("mock", partnerLink);
		processctx.setActivities(receive);

		processctx.getSelector().add(mock,
				LifeCycleListener.Category.PROCESS_CONTEXT_INITIALIZED,
				LifeCycleListener.Category.PROCESS_CONTEXT_SHUTDOWN,
				LifeCycleListener.Category.PROCESS_CREATED,
				LifeCycleListener.Category.PROCESS_STATE_CHANGE,
				LifeCycleListener.Category.PROCESS_TERMINATED);

		mock.expectProcessContextInitialized(processctx);

		processctx.initialize();

		mock.assertExpected(5, TimeUnit.SECONDS);

		mock.expectProcessCreated();
		mock.expectProcessStateChanged(RelativePosition.AFTER, receive);
		mock.expectProcessTerminated();

		Map<String, Object> msg = new HashMap<String, Object>();
		msg.put("/data/xxx/text()", "azsw");

		partnerLink.publish("requestResponseSimpleTest()", "process", msg);

		mock.assertExpected(5, TimeUnit.SECONDS);

		mock.expectProcessContextDestroyed(processctx);

		processctx.shutdown();

		mock.assertExpected(5, TimeUnit.SECONDS);
	}
}
