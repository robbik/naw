package org.naw.core.activity;

import static org.naw.core.ProcessState.BEFORE;
import static org.naw.core.ProcessState.SLEEP;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.naw.core.Process;

/**
 * PICK
 */
public class Pick extends AbstractActivity {

	private boolean createInstance;

	private List<PickOnAlarm> onAlarms;

	private List<PickOnMessage> onMessages;

	private final AtomicBoolean shutdown;

	public Pick(String name) {
		super(name);

		shutdown = new AtomicBoolean(false);
	}

	public void setCreateInstance(boolean createInstance) {
		this.createInstance = createInstance;
	}

	public boolean isCreateInstance() {
		return createInstance;
	}

	public void setOnAlarms(List<PickOnAlarm> onAlarms) {
		this.onAlarms = onAlarms;
	}

	public void setOnMessages(List<PickOnMessage> onMessages) {
		this.onMessages = onMessages;
	}

	@Override
	public void init(ActivityContext ctx) throws Exception {
		super.init(ctx);

		if (onMessages != null) {
			for (int i = onMessages.size() - 1; i >= 0; --i) {
				onMessages.get(i).init(ctx);
			}
		}

		if (onAlarms != null) {
			for (int i = onAlarms.size() - 1; i >= 0; --i) {
				onAlarms.get(i).init(ctx);
			}
		}
	}

	public void execute(Process process) throws Exception {
		synchronized (process) {
			if (onAlarms != null) {
				for (int i = onAlarms.size() - 1; i >= 0; --i) {
					onAlarms.get(i).execute(process);
				}
			}
		}

		process.compareAndUpdate(BEFORE, this, SLEEP);
	}

	public void afterExecute(Process process) {
		if (createInstance) {
			return;
		}

		// cancel all alarms associated with the process and this activity
		if (onAlarms != null) {
			for (int i = onAlarms.size() - 1; i >= 0; --i) {
				process.cancelTimeout(onAlarms.get(i).getName());
			}
		}
	}

	@Override
	public void hibernate() {
		if (onMessages != null) {
			for (int i = onMessages.size() - 1; i >= 0; --i) {
				onMessages.get(i).hibernate();
			}
		}

		if (onAlarms != null) {
			for (int i = onAlarms.size() - 1; i >= 0; --i) {
				onAlarms.get(i).hibernate();
			}
		}
	}

	@Override
	public void shutdown() {
		if (!shutdown.compareAndSet(false, true)) {
			return;
		}

		super.shutdown();

		if (onMessages != null) {
			for (int i = onMessages.size() - 1; i >= 0; --i) {
				onMessages.get(i).shutdown();
			}

			onMessages.clear();
			onMessages = null;
		}

		if (onAlarms != null) {
			for (int i = onAlarms.size() - 1; i >= 0; --i) {
				onAlarms.get(i).shutdown();
			}

			onAlarms.clear();
			onAlarms = null;
		}
	}
}
