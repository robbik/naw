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

	private final AtomicBoolean destroyed;

	public Sequence(String name) {
		super(name);

		destroyed = new AtomicBoolean(false);
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
	public void destroy() {
		super.destroy();

		if (!destroyed.compareAndSet(false, true)) {
			return;
		}

		// destroy pipeline
		pipeline.destroy();

		// gc workds
		pipeline = null;
		activities = null;
	}
}
