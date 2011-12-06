package org.naw.core;

import static org.naw.core.ProcessState.ERROR;
import static org.naw.core.ProcessState.INIT;
import static org.naw.core.ProcessState.TERMINATED;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

import org.naw.core.activity.AbstractActivity;
import org.naw.core.activity.Activity;
import org.naw.core.activity.ActivityContext;
import org.naw.core.exchange.DefaultMessage;
import org.naw.core.exchange.Message;
import org.naw.core.util.InactiveTimeout;
import org.naw.core.util.Selectors;
import org.naw.core.util.Timeout;
import org.naw.core.util.Timer;
import org.naw.core.util.TimerTask;
import org.naw.core.util.internal.ObjectUtils;
import org.naw.core.util.internal.SharedExecutors;

public class DefaultProcess implements Process {

	private static final long serialVersionUID = -847897634552529547L;

	private static final transient String prefix = UUID.randomUUID().toString()
			.replace("-", "")
			+ ".";

	private static final transient AtomicLong counter = new AtomicLong(0);

	private final String id;

	private transient ProcessContext ctx;

	private String ctxName;

	private final Map<String, Object> attributes;

	private transient Map<String, List<Timeout>> timeoutMap;

	private final Message message;

	private transient ProcessState state;

	private transient Activity activity;

	private transient boolean activated;

	private transient boolean destroyed;

	public DefaultProcess() {
		this(new DefaultMessage());
	}

	public DefaultProcess(Message message) {
		this(prefix.concat(String.valueOf(counter.incrementAndGet())), message);
	}

	public DefaultProcess(String pid) {
		this(pid, new DefaultMessage());
	}

	public DefaultProcess(String id, Message message) {
		this.id = id;

		attributes = new HashMap<String, Object>();
		timeoutMap = new HashMap<String, List<Timeout>>();

		if (message == null) {
			this.message = new DefaultMessage();
		} else {
			this.message = message;
		}

		state = INIT;
		activity = null;

		activated = false;
		destroyed = false;
	}

	public synchronized String getId() {
		return id;
	}

	public synchronized ProcessContext getContext() {
		return ctx;
	}

	public synchronized String getContextName() {
		return ctxName;
	}

	public void setContextName(String contextName) {
		ctxName = contextName;
	}

	public synchronized void setAttribute(String name, Object value) {
		attributes.put(name, value);
	}

	@SuppressWarnings("unchecked")
	public synchronized <T> T getAttribute(String name, Class<T> type) {
		Object v = attributes.get(name);
		if (v == null) {
			return null;
		}

		return (T) v;
	}

	@SuppressWarnings("unchecked")
	public synchronized <T> T removeAttribute(String name, Class<T> type) {
		Object v = attributes.remove(name);
		if (v == null) {
			return null;
		}

		return (T) v;
	}

	public synchronized Map<String, Object> getAttributes() {
		return attributes;
	}

	public synchronized Message getMessage() {
		return message;
	}

	public synchronized void registerTimeout(Timeout timeout) {
		if (timeout == null) {
			return;
		}

		String activityName = timeout.getActivityName();

		List<Timeout> list = timeoutMap.get(activityName);

		if (list == null) {
			list = new ArrayList<Timeout>();
			timeoutMap.put(activityName, list);
		}

		list.add(timeout);
	}

	public synchronized void unregisterTimeout(Timeout timeout) {
		if (timeout == null) {
			return;
		}

		String activityName = timeout.getActivityName();

		List<Timeout> list = timeoutMap.get(activityName);
		if (list == null) {
			return;
		}

		if (list.remove(timeout)) {
			if (list.isEmpty()) {
				timeoutMap.remove(timeout);
			}
		}
	}

	public synchronized void cancelTimeout(String activityName) {
		if (activityName == null) {
			return;
		}

		List<Timeout> list = timeoutMap.remove(activityName);
		if (list == null) {
			return;
		}

		for (int i = list.size() - 1; i >= 0; --i) {
			list.get(i).cancel();
		}

		list.clear();
	}

	public synchronized List<Timeout> getTimeouts() {
		List<Timeout> timeouts = new ArrayList<Timeout>();
		Set<Timeout> dupcheck = new HashSet<Timeout>();

		for (List<Timeout> list : timeoutMap.values()) {
			for (int i = 0, len = list.size(); i < len; ++i) {
				Timeout to = list.get(i);

				if (!dupcheck.contains(to)) {
					dupcheck.add(to);
					timeouts.add(to);
				}
			}
		}

		dupcheck.clear();
		dupcheck = null;

		return timeouts;
	}

	public synchronized boolean compare(ProcessState state, Activity activity) {
		return ObjectUtils.equals(this.state, state)
				&& ObjectUtils.equals(this.activity, activity);
	}

	public synchronized void noFireEventUpdate(ProcessState state,
			Activity activity) {
		if (destroyed) {
			throw new IllegalStateException("process already destroyed");
		}

		this.state = state;
		this.activity = activity;
	}

	public synchronized void update(ProcessState state, Activity activity) {
		if (destroyed) {
			throw new IllegalStateException("process already destroyed");
		}

		if ((this.state == state)
				&& ObjectUtils.equals(this.activity, activity)) {
			return;
		}

		this.state = state;
		this.activity = activity;

		if (state == TERMINATED) {
			Selectors.fireProcessTerminated(ctx, this);
		} else {
			Selectors.fireProcessStateChange(ctx, this);
		}
	}

	public synchronized void update(ProcessState state) {
		if (destroyed) {
			throw new IllegalStateException("process already destroyed");
		}

		if (this.state == state) {
			return;
		}

		this.state = state;

		if (state == TERMINATED) {
			Selectors.fireProcessTerminated(ctx, this);
		} else {
			Selectors.fireProcessStateChange(ctx, this);
		}
	}

	public synchronized boolean compareAndUpdate(ProcessState expectedState,
			Activity expectedActivity, ProcessState newState,
			Activity newActivity) {
		if (destroyed) {
			throw new IllegalStateException("process already destroyed");
		}

		boolean ok = (this.state == expectedState)
				&& ObjectUtils.equals(this.activity, expectedActivity);

		if (ok
				&& ((this.state != newState) || !ObjectUtils.equals(
						this.activity, newActivity))) {
			this.state = newState;
			this.activity = newActivity;

			if (state == TERMINATED) {
				Selectors.fireProcessTerminated(ctx, this);
			} else {
				Selectors.fireProcessStateChange(ctx, this);
			}
		}

		return ok;
	}

	public synchronized boolean compareAndUpdate(ProcessState expectedState,
			Activity expectedActivity, ProcessState newState) {
		if (destroyed) {
			throw new IllegalStateException("process already destroyed");
		}

		boolean ok = ObjectUtils.equals(this.state, expectedState)
				&& ObjectUtils.equals(this.activity, expectedActivity);

		if (ok && (this.state != newState)) {
			this.state = newState;

			if (state == TERMINATED) {
				Selectors.fireProcessTerminated(ctx, this);
			} else {
				Selectors.fireProcessStateChange(ctx, this);
			}
		}

		return ok;
	}

	public synchronized ProcessState getState() {
		return state;
	}

	public synchronized Activity getActivity() {
		return activity;
	}

	private void execute(final ActivityContext actx, final Activity activity) {
		Executor executor = ctx.getExecutor();
		if (executor == null) {
			executor = SharedExecutors.DIRECT;
		}

		executor.execute(new Runnable() {
			public void run() {
				try {
					activity.execute(DefaultProcess.this);
				} catch (Throwable t) {
					update(ERROR, activity);

					// run compensation handlers
					actx.getPipeline().exceptionThrown(DefaultProcess.this, t,
							false);

					// terminate the process
					ctx.terminate(id);
				}
			}
		});
	}

	private void wakeUp(final ActivityContext actx, final Activity activity) {
		Executor executor = ctx.getExecutor();
		if (executor == null) {
			executor = SharedExecutors.DIRECT;
		}

		executor.execute(new Runnable() {
			public void run() {
				try {
					activity.wakeUp(DefaultProcess.this);
				} catch (Throwable t) {
					update(ERROR, activity);

					// run compensation handlers
					actx.getPipeline().exceptionThrown(DefaultProcess.this, t,
							false);

					// terminate the process
					ctx.terminate(id);
				}
			}
		});
	}

	public synchronized void init(ProcessContext ctx) {
		if (activated || destroyed) {
			return;
		}

		this.ctx = ctx;
		ctxName = ctx.getName();

		// recover activity
		if (activity != null) {
			activity = ctx.findActivity(activity.getName());

			if (activity == null) {
				throw new RuntimeException("activity " + activity.getName()
						+ " cannot be found");
			}
		}

		ActivityContext actx = activity == null ? null : activity
				.getActivityContext();

		switch (state) {
		case INIT:
			break; // do nothing
		case BEFORE:
			// execute
			execute(actx, activity);
			break;
		case SLEEP:
			// wake-up
			wakeUp(actx, activity);
			break;
		case AFTER:
		case HIBERNATED:
			// execute next activity
			actx.execute(this);
			break;
		case ERROR:
			// rerun compensation handler
			actx.getPipeline().exceptionThrown(this, new Exception(), false);

			// terminate this process
			ctx.terminate(id);

			return;
		case TERMINATED:
			throw new IllegalArgumentException("process " + id
					+ " is already terminated");
		}

		if (state == TERMINATED) {
			return;
		}

		Selectors.fireProcessCreated(ctx, this);

		// recover timeout
		Timer timer = ctx.getTimer();

		for (List<Timeout> list : timeoutMap.values()) {
			for (int i = list.size() - 1; i >= 0; --i) {
				Timeout timeout = list.get(i);
				if (!(timeout instanceof InactiveTimeout)) {
					continue;
				}

				Activity act = ctx.findActivity(timeout.getActivityName());

				if (act instanceof TimerTask) {
					timeout = timer.newTimeout((TimerTask) act,
							timeout.getDeadline(), timeout.getProcessId(),
							timeout.getActivityName());

					list.set(i, timeout);
				}
			}
		}

		activated = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.process.Process#destroy()
	 */
	public synchronized void destroy() {
		if (destroyed) {
			return;
		}

		// cancel and remove alarms
		for (List<Timeout> list : timeoutMap.values()) {
			for (int i = list.size() - 1; i >= 0; --i) {
				list.get(i).cancel();
			}

			list.clear();
		}

		timeoutMap.clear();

		// clear
		attributes.clear();
		message.clear();

		// gc works
		ctx = null;
		timeoutMap = null;

		state = TERMINATED;
		activity = null;

		activated = false;
		destroyed = true;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}

		if (o == this) {
			return true;
		}

		if (o instanceof Process) {
			return ObjectUtils.equals(id, ((Process) o).getId());
		}

		return false;
	}

	@Override
	public int hashCode() {
		if (id == null) {
			return 0;
		}

		return id.hashCode();
	}

	private void readObject(ObjectInputStream in)
			throws ClassNotFoundException, IOException {
		in.defaultReadObject();

		String ctxName = in.readUTF();
		if (ctxName.length() == 0) {
			this.ctxName = null;
		} else {
			this.ctxName = ctxName;
		}

		state = ProcessState.valueOf(in.readUnsignedByte());

		String activityName = in.readUTF();

		if (activityName.length() == 0) {
			activity = null;
		} else {
			activity = new AbstractActivity(activityName) {

				public void execute(Process process) throws Exception {
					// do nothing
				}
			};
		}

		timeoutMap = new HashMap<String, List<Timeout>>();

		for (int i = 0, size = in.readInt(); i < size; ++i) {
			Timeout to = (Timeout) in.readObject();
			String key = to.getActivityName();

			List<Timeout> value = timeoutMap.get(key);
			if (value == null) {
				value = new ArrayList<Timeout>();
				timeoutMap.put(key, value);
			}

			if (!value.contains(to)) {
				value.add(to);
			}
		}

		activated = false;
		destroyed = false;
	}

	private synchronized void writeObject(ObjectOutputStream out)
			throws IOException {
		out.defaultWriteObject();

		if (ctxName == null) {
			out.writeUTF("");
		} else {
			out.writeUTF(ctxName);
		}

		out.writeByte(state.codeValue());

		if (activity == null) {
			out.writeUTF("");
		} else {
			out.writeUTF(activity.getName());
		}

		List<Timeout> timeouts = getTimeouts();
		int size = timeouts.size();

		out.writeInt(size);

		for (int i = 0; i < size; ++i) {
			out.writeObject(InactiveTimeout.copyFrom(timeouts.get(i)));
		}
	}
}
