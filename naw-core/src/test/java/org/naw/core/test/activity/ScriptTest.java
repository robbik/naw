package org.naw.core.test.activity;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.UUID;

import org.junit.Test;
import org.naw.core.DefaultProcessContext;
import org.naw.core.Process;
import org.naw.core.ProcessState;
import org.naw.core.activity.Activity;
import org.naw.core.activity.Script;
import org.naw.core.activity.Script.Handler;
import org.naw.core.pipeline.DefaultPipeline;
import org.naw.core.pipeline.Pipeline;

public class ScriptTest {

	private static DefaultPipeline newPipeline(Activity... activities) {
		DefaultPipeline pipeline = new DefaultPipeline();
		pipeline.setActivities(activities);
		pipeline.setProcessContext(new DefaultProcessContext("bb"));
		pipeline.setSink(null);

		return pipeline;
	}

	@Test
	public void testGetName() {
		String name = UUID.randomUUID().toString();

		Script act = new Script(name);
		assertEquals(name, act.getName());
	}

	@Test
	public void testInit() throws Exception {
		Script act = new Script("a");
		newPipeline(act).init();
	}

	@Test
	public void testExecute() throws Exception {
		Script act = new Script("a");
		act.setHandler(new Handler() {
			public void handle(Process process) throws Exception {
				process.getMessage().set(
						"data",
						Collections.<String, Object> singletonMap("response",
								"OK"));
			}
		});

		Pipeline p = newPipeline(act).init();

		Process process = p.getProcessContext().newProcess();

		p.execute(process);

		assertEquals(act, process.getActivity());
		assertEquals(ProcessState.AFTER, process.getState());

		assertEquals(1, process.getMessage().get("data").size());
		assertEquals("OK", process.getMessage().get("data").get("response"));
		assertEquals(1, process.getMessage().getVariables().size());

		p.shutdown();
	}

	@Test
	public void testDestroyBeforeInit() throws Exception {
		Script act = new Script("a");
		act.shutdown();
	}

	@Test
	public void testDestroyAfterInit() throws Exception {
		Script act = new Script("a");
		newPipeline(act).init().shutdown();
	}

	@Test
	public void testDoubleDestroyAfterInit() throws Exception {
		Script act = new Script("a");

		Pipeline p = newPipeline(act).init();
		p.shutdown();
		p.shutdown();
	}
}
