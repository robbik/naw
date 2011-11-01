package org.naw.process;

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

import org.naw.exchange.DefaultMessage;
import org.naw.exchange.Message;
import org.naw.process.activity.Activity;
import org.naw.util.Timeout;

public class DefaultProcess implements Process {

    private static final String prefixPid = UUID.randomUUID().toString().replace("-", "") + ".";

    private static final AtomicLong counter = new AtomicLong(0);

    private final String pid;

    private final ProcessContext ctx;

    private final ConcurrentHashMap<String, Object> attributes;

    private final Map<String, List<Timeout>> alarms;

    private final Message message;

    private ProcessState state;

    private Activity activity;

    private Lock slock;

    private Lock xlock;

    private AtomicBoolean destroyed;

    public DefaultProcess(ProcessContext ctx) {
        this(ctx, new DefaultMessage());
    }

    public DefaultProcess(ProcessContext ctx, Message message) {
        this(ctx, prefixPid + counter.incrementAndGet(), message);
    }

    public DefaultProcess(ProcessContext ctx, String pid) {
        this(ctx, pid, new DefaultMessage());
    }

    public DefaultProcess(ProcessContext ctx, String pid, Message message) {
        this.pid = pid;
        this.ctx = ctx;

        attributes = new ConcurrentHashMap<String, Object>(10);

        alarms = Collections.synchronizedMap(new HashMap<String, List<Timeout>>());

        if (message == null) {
            this.message = new DefaultMessage();
        } else {
            this.message = message;
        }

        state = ProcessState.INIT;
        activity = null;

        destroyed = new AtomicBoolean(false);

        ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
        slock = lock.readLock();
        xlock = lock.writeLock();
    }

    public String getProcessId() {
        if (destroyed.get()) {
            throw new IllegalStateException("process already destroyed");
        }

        return pid;
    }

    public ProcessContext getProcessContext() {
        return ctx;
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public <T> T getAttribute(String name, Class<T> type) {
        Object v = attributes.get(name);
        if (v == null) {
            return null;
        }

        return type.cast(v);
    }

    public Object removeAttribute(String name) {
        return attributes.remove(name);
    }

    public <T> T removeAttribute(String name, Class<T> type) {
        Object v = attributes.remove(name);
        if (v == null) {
            return null;
        }

        return type.cast(v);
    }

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

    public void setState(ProcessState state, Activity activity) {
        if (destroyed.get()) {
            throw new IllegalStateException("process already destroyed");
        }

        xlock.lock();

        this.state = state;
        this.activity = activity;

        xlock.unlock();
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

    public boolean compareAndSet(ProcessState state, Activity activity, ProcessState newState, Activity newActivity) {
        if (destroyed.get()) {
            throw new IllegalStateException("process already destroyed");
        }

        boolean isEqual = false;

        xlock.lock();

        isEqual = equals(this.state, state) && equals(this.activity, activity);
        if (isEqual) {
            this.state = newState;
            this.activity = newActivity;
        }

        xlock.unlock();

        return isEqual;
    }

    public void terminate() {
        setState(ProcessState.TERMINATED, null);

        destroy();
    }

    public void destroy() {
        if (!destroyed.compareAndSet(false, true)) {
            return;
        }

        // cancel and remove all alarms
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
        slock = null;
        xlock = null;
    }

    private static boolean equals(ProcessState a, ProcessState b) {
        if (a == b) {
            return true;
        }

        if ((a == null) || (b == null)) {
            return false;
        }

        return a.equals(b);
    }

    private static boolean equals(Activity a, Activity b) {
        if (a == b) {
            return true;
        }

        if ((a == null) || (b == null)) {
            return false;
        }

        return a.getName().equalsIgnoreCase(b.getName());
    }
}
