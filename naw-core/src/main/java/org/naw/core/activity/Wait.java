package org.naw.core.activity;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.naw.core.ProcessState.AFTER;
import static org.naw.core.ProcessState.BEFORE;
import static org.naw.core.ProcessState.SLEEP;

import java.util.concurrent.TimeUnit;

import org.naw.core.Process;
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

		timer = procctx.getTimer();
	}

	public void execute(Process process) throws Exception {
		Timeout to;

		if (deadline > 0) {
			to = timer.newTimeout(this, deadline, process.getId(), name);
		} else {
			to = timer.newTimeout(this, duration, MILLISECONDS,
					process.getId(), name);
		}

		synchronized (process) {
			if (process.compare(BEFORE, this)) {
				if (to != null) {
					process.registerTimeout(to);
				}

				process.update(SLEEP);
			}
		}
	}

	public void run(Timeout timeout) throws Exception {
		Process process = procctx.findProcess(timeout.getProcessId());
		if (process == null) {
			return;
		}

		boolean updated = process.compareAndUpdate(BEFORE, this, AFTER);

		if (!updated) {
			updated = process.compareAndUpdate(SLEEP, this, AFTER);

			if (updated) {
				process.unregisterTimeout(timeout);
			}
		}

		if (updated) {
			ctx.execute(process);
		}
	}

	@Override
	public void shutdown() {
		super.shutdown();

		timer = null;
	}
}
