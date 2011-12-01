package org.naw.core.activity;

import java.util.concurrent.TimeUnit;

import org.naw.core.Process;
import org.naw.core.ProcessState;
import org.naw.core.util.Timeout;
import org.naw.core.util.Timer;
import org.naw.core.util.TimerTask;

/**
 * WAIT
 */
public class Wait extends AbstractActivity implements TimerTask {

	private Timer timer;

	private long deadline;

	private long duration;

	public Wait(String name) {
		super(name);
	}

	public void setDuration(long duration, TimeUnit unit) {
		this.duration = unit.toMillis(duration);
	}

	public void setDeadline(long deadline) {
		this.deadline = deadline;
	}

	@Override
	public void init(ActivityContext ctx) throws Exception {
		super.init(ctx);

		timer = ctx.getProcessContext().getTimer();
	}

	public void execute(Process process) throws Exception {
		Timeout to;

		if (deadline > 0) {
			to = timer.newTimeout(this, deadline, process.getId(), name);
		} else {
			to = timer.newTimeout(this, duration, TimeUnit.MILLISECONDS, process.getId(), name);
		}

		if (to != null) {
			process.registerTimeout(to);
		}

		process.compareAndUpdate(ProcessState.BEFORE, this, ProcessState.SLEEP);
	}

	public void run(Timeout timeout) throws Exception {
		Process process = ctx.getProcessContext().findProcess(timeout.getProcessId());
		if (process == null) {
			return;
		}

		boolean updated = process.compareAndUpdate(ProcessState.BEFORE, this, ProcessState.ON);

		if (!updated) {
			updated = process.compareAndUpdate(ProcessState.SLEEP, this, ProcessState.ON);
		}

		if (updated) {
			ctx.execute(process);
		}
	}

	@Override
	public void destroy() {
		super.destroy();

		timer = null;
	}
}
