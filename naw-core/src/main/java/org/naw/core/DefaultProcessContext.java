package org.naw.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.naw.core.activity.Activity;
import org.naw.core.exchange.Message;
import org.naw.core.partnerLink.PartnerLink;
import org.naw.core.pipeline.DefaultPipeline;
import org.naw.core.pipeline.Pipeline;
import org.naw.core.pipeline.Sink;
import org.naw.core.storage.Storage;
import org.naw.core.util.Timer;
import org.naw.core.util.internal.ObjectUtils;

/**
 * This class is default implementation of {@link org.naw.core.ProcessContext}
 * 
 * @see org.naw.core.ProcessContext
 * 
 */
public class DefaultProcessContext implements ProcessContext, Sink {

	private final String name;

	private final ConcurrentHashMap<String, PartnerLink> links;

	private final ConcurrentHashMap<String, Activity> activities;

	private Storage storage;

	private Timer timer;

	private DefaultPipeline pipeline;

	private final Map<String, Process> instances;

	private final AtomicBoolean destroyed;

	private final List<ProcessLifeCycleListener> lcls;

	private final Lock lclslock;

	private final Lock lclxlock;

	/**
	 * create new instance of {@link DefaultProcessContext}
	 * 
	 * @param name
	 *            workflow name
	 */
	public DefaultProcessContext(String name) {
		this.name = name;

		links = new ConcurrentHashMap<String, PartnerLink>();
		activities = new ConcurrentHashMap<String, Activity>();

		storage = null;
		timer = null;
		pipeline = null;

		instances = Collections.synchronizedMap(new HashMap<String, Process>());

		destroyed = new AtomicBoolean(false);

		lcls = new ArrayList<ProcessLifeCycleListener>();

		ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
		lclslock = lock.readLock();
		lclxlock = lock.writeLock();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.process.ProcessContext#getName()
	 */
	public String getName() {
		return name;
	}

	public void setStorage(Storage storage) {
		this.storage = storage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.process.ProcessContext#getStorage()
	 */
	public Storage getStorage() {
		return storage;
	}

	/**
	 * set timer used by this workflow
	 * 
	 * @param timer
	 *            the timer
	 */
	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.process.ProcessContext#getTimer()
	 */
	public Timer getTimer() {
		return timer;
	}

	public void addPartnerLink(String name, PartnerLink link) {
		if (destroyed.get()) {
			throw new IllegalStateException("context already destroyed");
		}

		links.put(name, link);
	}

	public void removePartnerLink(String name) {
		links.remove(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.process.ProcessContext#findPartnerLink(java.lang.String)
	 */
	public PartnerLink findPartnerLink(String name) {
		return links.get(name);
	}

	public Activity findActivity(String name) {
		return activities.get(name);
	}

	public void registerActivity(Activity activity) throws Exception {
		Activity old = activities.putIfAbsent(activity.getName(), activity);

		if ((old != null)
				&& !ObjectUtils.equals((Object) old, (Object) activity)) {
			throw new Exception();
		}
	}

	/**
	 * set workflow activities
	 * 
	 * @param activities
	 *            workflow activities
	 */
	public void setActivities(Activity... activities) {
		pipeline = new DefaultPipeline();
		pipeline.setActivities(activities);
		pipeline.setProcessContext(this);
		pipeline.setSink(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.process.ProcessContext#init()
	 */
	public void init() throws Exception {
		if (destroyed.get()) {
			throw new IllegalStateException("context already destroyed");
		}

		pipeline.init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.process.ProcessContext#newProcess()
	 */
	public Process newProcess() {
		if (destroyed.get()) {
			throw new IllegalStateException("context already destroyed");
		}

		DefaultProcess process = new DefaultProcess(this);

		instances.put(process.getId(), process);
		return process;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.process.ProcessContext#newProcess(org.naw.exchange.Message)
	 */
	public Process newProcess(Message message) {
		if (destroyed.get()) {
			throw new IllegalStateException("context already destroyed");
		}

		DefaultProcess process = new DefaultProcess(this, message);

		instances.put(process.getId(), process);
		return process;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.process.ProcessContext#findProcess(java.lang.String)
	 */
	public Process findProcess(String pid) {
		return instances.get(pid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.process.ProcessContext#findAllProcesses()
	 */
	public Collection<Process> findAllProcesses() {
		return Collections.unmodifiableCollection(instances.values());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.process.ProcessContext#hydrateAll()
	 */
	public void hydrateAll() {
		if (destroyed.get()) {
			throw new IllegalStateException("context already destroyed");
		}

		if (storage == null) {
			throw new IllegalStateException("no storage defined");
		}

		Process[] processes = storage.findByWorkflowName(name);
		if (processes != null) {
			for (int i = 0; i < processes.length; ++i) {
				Process process = processes[i];

				if (process.getState() != ProcessState.TERMINATED) {
					instances.put(process.getId(), process);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.process.ProcessContext#hydrate(java.lang.String)
	 */
	public void hydrate(String pid) {
		if (destroyed.get()) {
			throw new IllegalStateException("context already destroyed");
		}

		if (storage == null) {
			throw new IllegalStateException("no storage defined");
		}

		Process process = storage.find(pid);
		if (process != null) {
			instances.put(pid, process);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.process.ProcessContext#activate(org.naw.process.Process)
	 */
	public void activate(Process process) throws Exception {
		synchronized (instances) {
			if (instances.containsKey(process.getId())) {
				throw new Exception("process " + process.getId()
						+ " already activated");
			}

			process.activate(this);

			instances.put(process.getId(), process);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.process.ProcessContext#deactivate(java.lang.String, boolean)
	 */
	public Process deactivate(String pid, boolean destroyAfter) {
		if (destroyed.get()) {
			throw new IllegalStateException("context already destroyed");
		}

		Process process = instances.remove(pid);
		if (process != null) {
			process.deactivate();

			if (destroyAfter) {
				process.destroy();
			}
		}

		return process;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.process.ProcessContext#terminate(java.lang.String)
	 */
	public void terminate(String pid) {
		if (destroyed.get()) {
			throw new IllegalStateException("context already destroyed");
		}

		Process process = instances.remove(pid);
		if (process != null) {
			process.update(ProcessState.TERMINATED, null);
			process.destroy();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.naw.process.pipeline.Sink#sunk(org.naw.process.pipeline.Pipeline,
	 * org.naw.process.Process)
	 */
	public void sunk(Pipeline pipeline, Process process) {
		terminate(process.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.process.ProcessContext#destroy()
	 */
	public void destroy() {
		if (!destroyed.compareAndSet(false, true)) {
			return;
		}

		// destroy instances
		synchronized (instances) {
			for (Process p : instances.values()) {
				p.destroy();
			}

			instances.clear();
		}

		// destroy pipeline
		pipeline.destroy();
		pipeline = null;

		// unlink partner links
		links.clear();

		// unlink dehydration storage
		storage = null;

		// unlink timer
		timer = null;
	}

	public void addLifeCycleListener(ProcessLifeCycleListener listener) {
		lclxlock.lock();
		lcls.add(listener);
		lclxlock.unlock();
	}

	public void removeLifeCycleListener(ProcessLifeCycleListener listener) {
		lclxlock.lock();
		lcls.remove(listener);
		lclxlock.unlock();
	}

	public void fireProcessStateChange(Process process, ProcessState newState,
			Activity newActivity) {

		lclslock.lock();
		try {
			for (int i = 0, len = lcls.size(); i < len; ++i) {
				lcls.get(i).processStateChange(this, process, newState,
						newActivity);
			}
		} finally {
			lclslock.unlock();
		}
	}

	public void fireProcessBeginWait(Process process, Activity activity) {
		lclslock.lock();
		try {
			for (int i = 0, len = lcls.size(); i < len; ++i) {
				lcls.get(i).processBeginWait(this, process, activity);
			}
		} finally {
			lclslock.unlock();
		}
	}

	public void fireProcessEndWait(Process process, Activity activity) {
		lclslock.lock();
		try {
			for (int i = 0, len = lcls.size(); i < len; ++i) {
				lcls.get(i).processEndWait(this, process, activity);
			}
		} finally {
			lclslock.unlock();
		}
	}
}
