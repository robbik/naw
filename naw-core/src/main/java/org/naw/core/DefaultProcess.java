package org.naw.core;

import static org.naw.core.ProcessState.ERROR;
import static org.naw.core.ProcessState.INIT;
import static org.naw.core.ProcessState.TERMINATED;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.naw.core.activity.AbstractActivity;
import org.naw.core.activity.Activity;
import org.naw.core.activity.ActivityContext;
import org.naw.core.exchange.DefaultMessage;
import org.naw.core.exchange.Message;
import org.naw.core.util.InactiveTimeout;
import org.naw.core.util.Selectors;
import org.naw.core.util.SynchronizedHashMap;
import org.naw.core.util.Timeout;
import org.naw.core.util.Timer;
import org.naw.core.util.TimerTask;
import org.naw.core.util.internal.ObjectUtils;

public class DefaultProcess implements Process {

	private static final long serialVersionUID = -847897634552529547L;

	private static final transient String prefix = UUID.randomUUID().toString()
			.replace("-", "")
			+ ".";

	private static final transient AtomicLong counter = new AtomicLong(0);

	private final String id;

	private transient ProcessContext ctx;

	private final Map<String, Object> attributes;

	private transient final Map<String, List<Timeout>> timeoutMap;

	private final Message message;

	private transient ProcessState state;

	private transient Activity activity;

	private transient final Object monitor;

	private transient final AtomicBoolean activated;

	private transient final AtomicBoolean destroyed;

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

		attributes = new SynchronizedHashMap<String, Object>();
		timeoutMap = new SynchronizedHashMap<String, List<Timeout>>();

		if (message == null) {
			this.message = new DefaultMessage();
		} else {
			this.message = message;
		}

		state = INIT;
		activity = null;

		monitor = new Object();

		activated = new AtomicBoolean(false);
		destroyed = new AtomicBoolean(false);
	}

	public String getId() {
		return id;
	}

	public ProcessContext getContext() {
		return ctx;
	}

	public void setAttribute(String name, Object value) {
		attributes.put(name, value);
	}

	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name, Class<T> type) {
		Object v = attributes.get(name);
		if (v == null) {
			return null;
		}

		return (T) v;
	}

	@SuppressWarnings("unchecked")
	public <T> T removeAttribute(String name, Class<T> type) {
		Object v = attributes.remove(name);
		if (v == null) {
			return null;
		}

		return (T) v;
	}

	public Message getMessage() {
		return message;
	}

	public void registerTimeout(Timeout timeout) {
		if (timeout == null) {
			return;
		}

		String activityName = timeout.getActivityName();

		List<Timeout> list;

		synchronized (timeoutMap) {
			list = timeoutMap.get(activityName);

			if (list == null) {
				list = Collections.synchronizedList(new ArrayList<Timeout>());
				timeoutMap.put(activityName, list);
			}
		}

		list.add(timeout);
	}

	public void unregisterTimeout(Timeout timeout) {
		if (timeout == null) {
			return;
		}

		String activityName = timeout.getActivityName();

		List<Timeout> list = timeoutMap.get(activityName);
		if (list == null) {
			return;
		}

		synchronized (list) {
			if (list.remove(timeout)) {
				if (list.isEmpty()) {
					timeoutMap.remove(timeout);
				}
			}
		}
	}

	public void cancelTimeout(String activityName) {
		if (activityName == null) {
			return;
		}

		List<Timeout> list = timeoutMap.remove(activityName);
		if (list == null) {
			return;
		}

		synchronized (list) {
			for (int i = list.size() - 1; i >= 0; --i) {
				list.get(i).cancel();
			}

			list.clear();
		}
	}

	public void update(ProcessState state, Activity activity) {
		if (destroyed.get()) {
			throw new IllegalStateException("process already destroyed");
		}

		synchronized (monitor) {
			this.state = state;
			this.activity = activity;

			if (state != TERMINATED) {
				Selectors.fireProcessStateChange(ctx, this);
			}
		}
	}

	public void update(ProcessState state) {
		if (destroyed.get()) {
			throw new IllegalStateException("process already destroyed");
		}

		synchronized (monitor) {
			this.state = state;

			if (state != TERMINATED) {
				Selectors.fireProcessStateChange(ctx, this);
			}
		}
	}

	public boolean compareAndUpdate(ProcessState expectedState,
			Activity expectedActivity, ProcessState newState,
			Activity newActivity) {
		if (destroyed.get()) {
			throw new IllegalStateException("process already destroyed");
		}

		boolean ok = false;

		synchronized (monitor) {
			ok = ObjectUtils.equals(this.state, expectedState);

			if (!ok) {
				ok = ObjectUtils.equals(this.activity, expectedActivity);
			}

			if (ok) {
				this.state = newState;
				this.activity = newActivity;

				if (state != TERMINATED) {
					Selectors.fireProcessStateChange(ctx, this);
				}
			}
		}

		return ok;
	}

	public boolean compareAndUpdate(ProcessState expectedState,
			Activity expectedActivity, ProcessState newState) {
		if (destroyed.get()) {
			throw new IllegalStateException("process already destroyed");
		}

		boolean ok;

		synchronized (monitor) {
			ok = ObjectUtils.equals(this.state, expectedState);

			if (!ok) {
				ok = ObjectUtils.equals(this.activity, expectedActivity);
			}

			if (ok) {
				this.state = newState;

				if (state != TERMINATED) {
					Selectors.fireProcessStateChange(ctx, this);
				}
			}
		}

		return ok;
	}

	public ProcessState getState() {
		synchronized (monitor) {
			return state;
		}
	}

	public Activity getActivity() {
		synchronized (monitor) {
			return activity;
		}
	}

	public void init(ProcessContext ctx) {
		if (!activated.compareAndSet(false, true)) {
			return;
		}

		this.ctx = ctx;

		// recover activity
		synchronized (monitor) {
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
				// re-execute
				try {
					activity.execute(this);
				} catch (Throwable t) {
					update(ERROR, activity);

					// run compensation handlers
					actx.getPipeline().exceptionThrown(this, t, false);

					// terminate the process
					ctx.terminate(id);

					throw new RuntimeException("process " + id
							+ " is terminated in activation process");
				}

				break;
			case SLEEP:
				break; // do nothing
			case ON:
				break; // FIXME do nothing?
			case AFTER:
				// re-execute next activity
				actx.execute(this);

				if (state == TERMINATED) {
					throw new RuntimeException("process " + id
							+ " is terminated in activation process");
				}
				break;
			case ERROR:
				// rerun compensation handler
				actx.getPipeline()
						.exceptionThrown(this, new Exception(), false);

				// terminate this process
				ctx.terminate(id);

				throw new RuntimeException("process " + id
						+ " is terminated in activation process");
			case TERMINATED:
				throw new IllegalArgumentException("process " + id
						+ " is already terminated");
			}

			Selectors.fireProcessCreated(ctx, this);
		}

		// recover timeout
		Timer timer = ctx.getTimer();

		synchronized (timeoutMap) {
			for (List<Timeout> list : timeoutMap.values()) {
				synchronized (list) {
					for (int i = list.size() - 1; i >= 0; --i) {
						Timeout timeout = list.get(i);
						if (!(timeout instanceof InactiveTimeout)) {
							continue;
						}

						Activity act = ctx.findActivity(timeout
								.getActivityName());

						if (act instanceof TimerTask) {
							timeout = timer.newTimeout((TimerTask) act,
									timeout.getDeadline(),
									timeout.getProcessId(),
									timeout.getActivityName());

							list.set(i, timeout);
						}
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.process.Process#destroy()
	 */
	public void destroy() {
		if (!destroyed.compareAndSet(false, true)) {
			return;
		}

		// cancel and remove alarms
		synchronized (timeoutMap) {
			for (List<Timeout> list : timeoutMap.values()) {
				synchronized (list) {
					for (int i = list.size() - 1; i >= 0; --i) {
						list.get(i).cancel();
					}

					list.clear();
				}
			}

			timeoutMap.clear();
		}

		// clear attributes
		attributes.clear();

		// gc works
		activity = null;

		if (state == TERMINATED) {
			Selectors.fireProcessTerminated(ctx, this);
		}
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

		synchronized (monitor) {
			synchronized (timeoutMap) {
				synchronized (attributes) {
					in.defaultReadObject();

					state = ProcessState.valueOf(in.readUnsignedByte());

					String activityName = in.readUTF();

					if (activityName.length() == 0) {
						activity = null;
					} else {
						activity = new AbstractActivity(activityName) {

							public void execute(Process process)
									throws Exception {
								// do nothing
							}
						};
					}

					int size = in.readInt();
					timeoutMap.clear();

					for (int i = 0; i < size; ++i) {
						String key = in.readUTF();
						List<Timeout> value = Collections
								.synchronizedList(new ArrayList<Timeout>());

						int lsize = in.readInt();
						for (int j = 0; j < lsize; ++j) {
							value.add((Timeout) in.readObject());
						}

						timeoutMap.put(key, value);
					}
				}
			}
		}
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		synchronized (monitor) {
			synchronized (timeoutMap) {
				synchronized (attributes) {
					out.defaultWriteObject();

					out.writeByte(state.codeValue());

					if (activity == null) {
						out.writeUTF("");
					} else {
						out.writeUTF(activity.getName());
					}

					out.writeInt(timeoutMap.size());

					for (Map.Entry<String, List<Timeout>> e : timeoutMap
							.entrySet()) {
						List<Timeout> list = e.getValue();

						if (list != null) {
							synchronized (list) {
								out.writeUTF(e.getKey());

								out.writeInt(list.size());
								for (int i = 0, len = list.size(); i < len; ++i) {
									out.writeObject(InactiveTimeout
											.copyFrom(list.get(i)));
								}
							}
						}
					}
				}
			}
		}
	}
}
