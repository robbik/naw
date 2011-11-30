package org.naw.core.activity;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.naw.core.Process;
import org.naw.core.ProcessContext;
import org.naw.core.util.Selectors;

/**
 * PICK
 */
public class Pick extends AbstractActivity {

	private ProcessContext procctx;

	private boolean createInstance;

	private List<PickOnAlarm> onAlarms;

	private List<PickOnMessage> onMessages;

	private final AtomicBoolean destroyed;

	public Pick(String name) {
		super(name);

		destroyed = new AtomicBoolean(false);
	}

	public String getName() {
		return name;
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

	public void init(ActivityContext ctx) throws Exception {
		super.init(ctx);

		procctx = ctx.getProcessContext();

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
		if (!createInstance) {
			Selectors.fireProcessBeginWait(procctx, process, this);
		}

		if (onAlarms != null) {
			for (int i = onAlarms.size() - 1; i >= 0; --i) {
				onAlarms.get(i).execute(process);
			}
		}
	}

	public void afterExecute(Process process) {
		if (createInstance) {
			return;
		}

		// cancel all alarms associated with the process and this activity
		if (onAlarms != null) {
			for (int i = onAlarms.size() - 1; i >= 0; --i) {
				process.removeAlarmForActivity(onAlarms.get(i).getName());
			}
		}

		Selectors.fireProcessEndWait(procctx, process, this);
	}

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
