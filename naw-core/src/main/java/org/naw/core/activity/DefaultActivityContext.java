package org.naw.core.activity;

import static org.naw.core.ProcessState.AFTER;
import static org.naw.core.ProcessState.BEFORE;
import static org.naw.core.ProcessState.ERROR;
import static org.naw.core.ProcessState.HIBERNATED;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.naw.core.Process;
import org.naw.core.ProcessContext;
import org.naw.core.pipeline.Pipeline;
import org.naw.core.pipeline.Sink;
import org.naw.core.util.internal.ObjectUtils;
import org.naw.core.util.internal.SharedExecutors;

/**
 * Default implementation of {@link ActivityContext}
 */
public class DefaultActivityContext implements ActivityContext {

	private final Pipeline pipeline;

	private final Executor executor;

	private final Activity activity;

	private DefaultActivityContext next;

	private final AtomicBoolean hibernate;

	public DefaultActivityContext(Pipeline pipeline, Activity activity) {
		this.pipeline = pipeline;
		executor = ObjectUtils.coalesce(pipeline.getProcessContext()
				.getExecutor(), SharedExecutors.DIRECT);

		this.activity = activity;

		next = null;
		hibernate = new AtomicBoolean(false);
	}

	public void setNext(DefaultActivityContext next) {
		this.next = next;
	}

	public DefaultActivityContext getNext() {
		return next;
	}

	public void unsetNext() {
		next = null;
	}

	public Pipeline getPipeline() {
		return pipeline;
	}

	public ProcessContext getProcessContext() {
		return pipeline.getProcessContext();
	}

	public Activity getActivity() {
		return activity;
	}

	public void execute(final Process process) {
		process.update(AFTER, activity);

		if (next == null) {
			Sink sink = pipeline.getSink();

			if (sink != null) {
				sink.sunk(pipeline, process);
			}
		} else if (hibernate.get()) {
			process.update(HIBERNATED, activity);
		} else {
			executor.execute(new NextRunnable(process));
		}
	}

	public void hibernate() {
		hibernate.set(true);
	}

	private class NextRunnable implements Runnable {

		private final Process process;

		public NextRunnable(Process process) {
			this.process = process;
		}

		public void run() {
			Activity nextActivity = next.activity;

			process.update(BEFORE, nextActivity);
			try {
				nextActivity.execute(process);
			} catch (Throwable t) {
				process.update(ERROR, nextActivity);

				// run compensation handlers
				pipeline.exceptionThrown(process, t, false);

				// terminate the process
				getProcessContext().terminate(process.getId());
			}
		}
	}
}
