package org.naw.core.test.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.naw.core.DefaultProcessContext;
import org.naw.core.ProcessState;
import org.naw.core.activity.Activity;
import org.naw.core.activity.Receive;
import org.naw.core.activity.Reply;
import org.naw.core.listener.LifeCycleListener;
import org.naw.core.partnerLink.MessageEvent;
import org.naw.core.partnerLink.PartnerLinkListener;
import org.naw.core.test.MockLifeCycleListener;
import org.naw.core.test.MockPartnerLink;

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
		partnerLink.setExecutorService(Executors.newCachedThreadPool());

		partnerLink.subscribe("process_callback", new PartnerLinkListener() {
			public void messageReceived(MessageEvent e) {
				// do nothing
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

		mock = new MockLifeCycleListener();
	}

	@After
	public void after() throws Exception {
		// do nothing
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
				LifeCycleListener.Category.PROCESS_CONTEXT_DESTROYED,
				LifeCycleListener.Category.PROCESS_CREATED,
				LifeCycleListener.Category.PROCESS_STATE_CHANGE,
				LifeCycleListener.Category.PROCESS_TERMINATED);

		mock.expectProcessContextInitialized(processctx);

		processctx.init();

		mock.assertExpected(5, TimeUnit.SECONDS);

		mock.expectProcessCreated();
		mock.expectProcessStateChanged(ProcessState.ON, receive);
		mock.expectProcessStateChanged(ProcessState.AFTER, receive);
		mock.expectProcessStateChanged(ProcessState.BEFORE, reply);
		mock.expectProcessStateChanged(ProcessState.AFTER, reply);
		mock.expectProcessTerminated();

		Map<String, Object> msg = new HashMap<String, Object>();
		msg.put("/data/xxx/text()", "abcdef");

		partnerLink.publish("requestResponseSimpleTest()", "process", msg);

		mock.assertExpected(5, TimeUnit.SECONDS);

		mock.expectProcessContextDestroyed(processctx);

		processctx.destroy();

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
				LifeCycleListener.Category.PROCESS_CONTEXT_DESTROYED,
				LifeCycleListener.Category.PROCESS_CREATED,
				LifeCycleListener.Category.PROCESS_STATE_CHANGE,
				LifeCycleListener.Category.PROCESS_TERMINATED);

		mock.expectProcessContextInitialized(processctx);

		processctx.init();

		mock.assertExpected(5, TimeUnit.SECONDS);

		mock.expectProcessCreated();
		mock.expectProcessStateChanged(ProcessState.ON, receive);
		mock.expectProcessStateChanged(ProcessState.AFTER, receive);
		mock.expectProcessTerminated();

		Map<String, Object> msg = new HashMap<String, Object>();
		msg.put("/data/xxx/text()", "azsw");

		partnerLink.publish("requestResponseSimpleTest()", "process", msg);
		
		mock.assertExpected(5, TimeUnit.SECONDS);

		mock.expectProcessContextDestroyed(processctx);

		processctx.destroy();

		mock.assertExpected(5, TimeUnit.SECONDS);
	}
}
