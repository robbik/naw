package org.naw.core.test.activity;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.UUID;

import org.junit.Test;
import org.naw.activities.Activity;
import org.naw.engine.DefaultProcessContext;
import org.naw.engine.ProcessInstance;
import org.naw.engine.RelativePosition;
import org.naw.engine.pipeline.DefaultPipeline;
import org.naw.engine.pipeline.Pipeline;
import org.naw.tasks.Script;
import org.naw.tasks.Script.Handler;

public class ScriptTest {

	private static DefaultPipeline newPipeline(Activity... activities) {
		DefaultPipeline pipeline = new DefaultPipeline();
		pipeline.setActivities(activities);
		pipeline.setNawProcess(new DefaultProcessContext("bb"));
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
		newPipeline(act).initialize();
	}

	@Test
	public void testExecute() throws Exception {
		Script act = new Script("a");
		act.setHandler(new Handler() {
			public void handle(ProcessInstance process) throws Exception {
				process.getMessage().set(
						"data",
						Collections.<String, Object> singletonMap("response",
								"OK"));
			}
		});

		Pipeline p = newPipeline(act).initialize();

		ProcessInstance process = p.getNawProcess().newProcess();

		p.next(process);

		assertEquals(act, process.getActivity());
		assertEquals(RelativePosition.AFTER, process.getState());

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
		newPipeline(act).initialize().shutdown();
	}

	@Test
	public void testDoubleDestroyAfterInit() throws Exception {
		Script act = new Script("a");

		Pipeline p = newPipeline(act).initialize();
		p.shutdown();
		p.shutdown();
	}
}
