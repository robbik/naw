package org.naw.core.activity;

import static org.naw.core.ProcessState.AFTER;
import static org.naw.core.ProcessState.BEFORE;
import static org.naw.core.ProcessState.SLEEP;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.naw.core.Process;
import org.naw.core.ProcessContext;
import org.naw.core.pipeline.DefaultPipeline;
import org.naw.core.pipeline.Pipeline;
import org.naw.core.pipeline.Sink;
import org.naw.core.util.Timeout;
import org.naw.core.util.Timer;
import org.naw.core.util.TimerTask;

public class PickOnAlarm implements TimerTask, Sink {

	private final Pick parent;

	private ActivityContext ctx;

	private ProcessContext procctx;

	private String name;

	private boolean createInstance;

	private long deadline;

	private long duration;

	private Activity[] activities;

	private DefaultPipeline pipeline;

	private Timer timer;

	private Timeout timeout;

	private final AtomicBoolean shutdown;

	public PickOnAlarm(Pick parent) {
		this.parent = parent;

		shutdown = new AtomicBoolean(false);
	}

	public void setName(String name) {
		this.name = parent.getName() + "$" + name;
	}

	public String getName() {
		return name;
	}

	public void setDuration(long duration, TimeUnit unit) {
		this.duration = unit.toMillis(duration);
	}

	public void setDeadline(long deadline) {
		this.deadline = deadline;
	}

	public void setActivity(Activity... activities) {
		this.activities = activities;
	}

	public void init(ActivityContext ctx) throws Exception {
		this.ctx = ctx;
		procctx = ctx.getProcessContext();

		createInstance = parent.isCreateInstance();

		if ((activities == null) || (activities.length == 0)) {
			pipeline = null;
		} else {
			pipeline = new DefaultPipeline(ctx.getPipeline());
			pipeline.setActivities(activities);
			pipeline.setProcessContext(procctx);
			pipeline.setSink(this);

			activities = null;
			pipeline.init();
		}

		timer = procctx.getTimer();

		if (createInstance) {
			if (deadline > 0) {
				timeout = timer.newTimeout(this, deadline, null, name);
			} else {
				timeout = timer.newTimeout(this, duration,
						TimeUnit.MILLISECONDS, null, name);
			}
		}
	}

	public void execute(Process process) throws Exception {
		if (!createInstance) {
			Timeout to;

			if (deadline > 0) {
				to = timer.newTimeout(this, deadline, process.getId(), name);
			} else {
				to = timer.newTimeout(this, duration, TimeUnit.MILLISECONDS,
						process.getId(), name);
			}

			if (to != null) {
				process.registerTimeout(to);
			}
		}
	}

	public void run(Timeout timeout) throws Exception {
		Process process;

		if (createInstance) {
			process = procctx.newProcess();
		} else {
			process = procctx.findProcess(timeout.getProcessId());
		}

		if (process == null) {
			return;
		}

		boolean ok = createInstance;

		synchronized (process) {
			if (!ok) {
				ok = process.compare(BEFORE, parent)
						|| process.compare(SLEEP, parent);
			}

			if (ok) {
				parent.afterExecute(process);

				if (pipeline == null) {
					process.update(AFTER, parent);
				} else {
					process.update(BEFORE, pipeline.getFirstActivity());
				}
			}
		}

		if (ok) {
			if (pipeline == null) {
				ctx.execute(process);
			} else {
				pipeline.execute(process);
			}
		}
	}

	public void sunk(Pipeline pipeline, Process process) {
		ctx.execute(process);
	}

	public void hibernate() {
		// hibernate pipeline
		if (pipeline != null) {
			pipeline.hibernate();
		}
	}

	public void shutdown() {
		if (!shutdown.compareAndSet(false, true)) {
			return;
		}

		// cancel timeout created by this sub-activity
		if (timeout != null) {
			timeout.cancel();
		}

		// shutdown pipeline
		if (pipeline != null) {
			pipeline.shutdown();
		}

		// gc works
		activities = null;
		pipeline = null;

		ctx = null;
		procctx = null;

		timer = null;
		timeout = null;
	}
}
