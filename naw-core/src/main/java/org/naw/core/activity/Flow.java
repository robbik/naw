package org.naw.core.activity;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.naw.core.Process;
import org.naw.core.pipeline.DefaultPipeline;
import org.naw.core.pipeline.Pipeline;
import org.naw.core.pipeline.Sink;

/**
 * FLOW
 * 
 * FIXME: process doesn't support multi state
 */
public class Flow extends AbstractActivity implements Sink {

	private final String attributeName;

	private final AtomicBoolean shutdown;

	private Executor executor;

	private ActivityContext ctx;

	private Activity[] activities;

	private DefaultPipeline[] pipelines;

	private int npipelines;

	public Flow(String name) {
		super(name);

		attributeName = "FLOW$" + name + "#counter";

		shutdown = new AtomicBoolean(false);
	}

	public void setActivities(Activity... activities) {
		this.activities = activities;
	}

	public void init(ActivityContext ctx) throws Exception {
		super.init(ctx);

		executor = procctx.getExecutor();
		if (executor == null) {
			executor = Executors.newCachedThreadPool();
		}

		if (activities == null) {
			pipelines = null;
		} else {
			Pipeline parent = ctx.getPipeline();

			npipelines = activities.length;
			pipelines = new DefaultPipeline[npipelines];

			for (int i = 0; i < npipelines; ++i) {
				DefaultPipeline pipeline = new DefaultPipeline(parent);
				pipeline.setActivities(activities[i]);
				pipeline.setProcessContext(procctx);
				pipeline.setSink(this);

				pipelines[i] = pipeline;
			}

			activities = null;
		}
	}

	public void execute(Process process) throws Exception {
		if (pipelines != null) {
			process.setAttribute(attributeName, new AtomicInteger(0));

			for (int i = npipelines - 1; i >= 0; --i) {
				executor.execute(new CompletionHandler(pipelines[i], process));
			}
		}
	}

	public void sunk(Pipeline pipeline, Process process) {
		AtomicInteger c = process.getAttribute(attributeName,
				AtomicInteger.class);

		if (c.incrementAndGet() == npipelines) {
			ctx.execute(process);
		}
	}

	public void hibernate() {
		// hibernate pipelines
		if (pipelines != null) {
			for (int i = 0; i < npipelines; ++i) {
				pipelines[i].hibernate();
			}
		}
	}

	public void shutdown() {
		if (!shutdown.compareAndSet(false, true)) {
			return;
		}

		super.shutdown();

		// shutdown pipelines
		if (pipelines != null) {
			for (int i = 0; i < npipelines; ++i) {
				pipelines[i].shutdown();
			}
		}

		// gc works
		activities = null;
		pipelines = null;
	}

	private static class CompletionHandler implements Runnable {

		private final Pipeline pipeline;

		private final Process process;

		public CompletionHandler(Pipeline pipeline, Process process) {
			this.pipeline = pipeline;
			this.process = process;
		}

		public void run() {
			try {
				pipeline.execute(process);
			} catch (Throwable t) {
				// do nothing
			}
		}
	}
}
