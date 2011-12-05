package org.naw.core.activity;

import java.util.concurrent.atomic.AtomicBoolean;

import org.naw.core.Process;
import org.naw.core.pipeline.DefaultPipeline;
import org.naw.core.pipeline.Pipeline;
import org.naw.core.pipeline.Sink;

/**
 * SEQUENCE
 */
public class Sequence extends AbstractActivity implements Sink {

	private DefaultPipeline pipeline;

	private Activity[] activities;

	private final AtomicBoolean shutdown;

	public Sequence(String name) {
		super(name);

		shutdown = new AtomicBoolean(false);
	}

	public void setActivities(Activity... activities) {
		this.activities = activities;
	}

	@Override
	public void init(ActivityContext ctx) throws Exception {
		super.init(ctx);

		pipeline = new DefaultPipeline(ctx.getPipeline());
		pipeline.setActivities(activities);
		pipeline.setProcessContext(ctx.getProcessContext());
		pipeline.setSink(this);

		pipeline.init();

		// gc works
		activities = null;
	}

	public void execute(Process process) throws Exception {
		pipeline.execute(process);
	}

	public void sunk(Pipeline pipeline, Process process) {
		ctx.execute(process);
	}

	@Override
	public void hibernate() {
		pipeline.hibernate();
	}

	@Override
	public void shutdown() {
		if (!shutdown.compareAndSet(false, true)) {
			return;
		}

		super.shutdown();

		// shutdown inner pipeline
		pipeline.shutdown();

		// gc works
		pipeline = null;
		activities = null;
	}
}
