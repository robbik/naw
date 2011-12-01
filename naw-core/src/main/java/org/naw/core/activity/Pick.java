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

	private final AtomicBoolean destroyed;

	public Pick(String name) {
		super(name);

		destroyed = new AtomicBoolean(false);
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
		if (onAlarms != null) {
			for (int i = onAlarms.size() - 1; i >= 0; --i) {
				onAlarms.get(i).execute(process);
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
	public void destroy() {
		super.destroy();
		
		if (!destroyed.compareAndSet(false, true)) {
			return;
		}

		if (onMessages != null) {
			for (int i = onMessages.size() - 1; i >= 0; --i) {
				onMessages.get(i).destroy();
			}

			onMessages.clear();
			onMessages = null;
		}

		if (onAlarms != null) {
			for (int i = onAlarms.size() - 1; i >= 0; --i) {
				onAlarms.get(i).destroy();
			}

			onAlarms.clear();
			onAlarms = null;
		}
	}
}
