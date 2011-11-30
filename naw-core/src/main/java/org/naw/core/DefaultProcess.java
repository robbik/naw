package org.naw.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.naw.core.activity.Activity;
import org.naw.core.exchange.DefaultMessage;
import org.naw.core.exchange.Message;
import org.naw.core.util.Timeout;
import org.naw.core.util.Timer;
import org.naw.core.util.TimerTask;
import org.naw.core.util.internal.ObjectUtils;

public class DefaultProcess implements Process {

	private static final String prefixId = UUID.randomUUID().toString()
			.replace("-", "")
			+ ".";

	private static final AtomicLong counter = new AtomicLong(0);

	private final String id;

	private final DefaultProcessContext ctx;

	private final ConcurrentHashMap<String, Object> attributes;

	private final Map<String, List<Timeout>> alarms;

	private final Message message;

	private volatile ProcessState state;

	private volatile Activity activity;

	private final Lock slock;

	private final Lock xlock;

	private final AtomicBoolean activated;

	private final AtomicBoolean destroyed;

	public DefaultProcess(DefaultProcessContext ctx) {
		this(ctx, new DefaultMessage());
	}

	public DefaultProcess(DefaultProcessContext ctx, Message message) {
		this(ctx, prefixId + counter.incrementAndGet(), message);
	}

	public DefaultProcess(DefaultProcessContext ctx, String pid) {
		this(ctx, pid, new DefaultMessage());
	}

	public DefaultProcess(DefaultProcessContext ctx, String id, Message message) {
		this.id = id;
		this.ctx = ctx;

		attributes = new ConcurrentHashMap<String, Object>(10);

		alarms = Collections
				.synchronizedMap(new HashMap<String, List<Timeout>>());

		if (message == null) {
			this.message = new DefaultMessage();
		} else {
			this.message = message;
		}

		state = ProcessState.INIT;
		activity = null;

		ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
		slock = lock.readLock();
		xlock = lock.writeLock();

		activated = new AtomicBoolean(false);
		destroyed = new AtomicBoolean(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.process.Process#getId()
	 */
	public String getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.process.Process#getContext()
	 */
	public ProcessContext getContext() {
		return ctx;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.process.Process#setAttribute(java.lang.String,
	 * java.lang.Object)
	 */
	public void setAttribute(String name, Object value) {
		attributes.put(name, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.process.Process#getAttribute(java.lang.String,
	 * java.lang.Class)
	 */
	public <T> T getAttribute(String name, Class<T> type) {
		Object v = attributes.get(name);
		if (v == null) {
			return null;
		}

		return type.cast(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.process.Process#removeAttribute(java.lang.String,
	 * java.lang.Class)
	 */
	public <T> T removeAttribute(String name, Class<T> type) {
		Object v = attributes.remove(name);
		if (v == null) {
			return null;
		}

		return type.cast(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.process.Process#getMessage()
	 */
	public Message getMessage() {
		return message;
	}

	public void addAlarm(Timeout timeout) {
		if (timeout == null) {
			return;
		}

		String activityName = timeout.getActivityName();

		List<Timeout> list;

		synchronized (alarms) {
			list = alarms.get(activityName);

			if (list == null) {
				list = Collections.synchronizedList(new ArrayList<Timeout>());
				alarms.put(activityName, list);
			}
		}

		list.add(timeout);
	}

	public void removeAlarm(Timeout timeout) {
		if (timeout == null) {
			return;
		}

		String activityName = timeout.getActivityName();

		List<Timeout> list = alarms.get(activityName);
		if (list == null) {
			return;
		}

		synchronized (list) {
			if (list.remove(timeout)) {
				if (list.isEmpty()) {
					alarms.remove(timeout);
				}
			}
		}
	}

	public void removeAlarmForActivity(String activityName) {
		if (activityName == null) {
			return;
		}

		List<Timeout> list = alarms.remove(activityName);
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

		xlock.lock();

		this.state = state;
		this.activity = activity;

		xlock.unlock();

		ctx.fireProcessStateChange(this, state, activity);
	}

	public ProcessState getState() {
		if (destroyed.get()) {
			throw new IllegalStateException("process already destroyed");
		}

		slock.lock();
		final ProcessState state = this.state;
		slock.unlock();

		return state;
	}

	public Activity getActivity() {
		if (destroyed.get()) {
			throw new IllegalStateException("process already destroyed");
		}

		slock.lock();
		final Activity activity = this.activity;
		slock.unlock();

		return activity;
	}

	public boolean compareAndUpdate(ProcessState expectedState,
			Activity expectedActivity, ProcessState newState,
			Activity newActivity) {
		if (destroyed.get()) {
			throw new IllegalStateException("process already destroyed");
		}

		boolean isEqual = false;

		xlock.lock();

		isEqual = ObjectUtils.equals(this.state, expectedState);
		isEqual = isEqual
				&& ObjectUtils.equals(this.activity, expectedActivity);

		if (isEqual) {
			this.state = newState;
			this.activity = newActivity;
		}

		xlock.unlock();

		ctx.fireProcessStateChange(this, state, activity);
		return isEqual;
	}

	public void activate(ProcessContext ctx) throws Exception {
		if (!activated.compareAndSet(false, true)) {
			return;
		}

		Timer timer = ctx.getTimer();

		synchronized (alarms) {
			for (List<Timeout> list : alarms.values()) {
				synchronized (list) {
					for (int i = list.size() - 1; i >= 0; --i) {
						Timeout timeout = list.get(i);

						Activity act = ctx.findActivity(timeout
								.getActivityName());
						if (act instanceof TimerTask) {
							list.set(
									i,
									timer.newTimeout((TimerTask) act,
											timeout.getDeadline(),
											timeout.getProcessId(),
											timeout.getActivityName()));
						}
					}
				}
			}
		}
	}

	public void deactivate() {
		if (!activated.compareAndSet(true, false)) {
			return;
		}

		// cancel alarms
		synchronized (alarms) {
			for (List<Timeout> list : alarms.values()) {
				synchronized (list) {
					for (int i = list.size() - 1; i >= 0; --i) {
						list.get(i).cancel();
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
		deactivate();

		if (!destroyed.compareAndSet(false, true)) {
			return;
		}

		// cancel and remove alarms
		synchronized (alarms) {
			for (List<Timeout> list : alarms.values()) {
				synchronized (list) {
					for (int i = list.size() - 1; i >= 0; --i) {
						list.get(i).cancel();
					}

					list.clear();
				}
			}

			alarms.clear();
		}

		// clear attributes
		attributes.clear();

		// gc works
		activity = null;
	}
}
