package org.naw.core;

import static org.naw.core.ProcessState.TERMINATED;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.naw.core.activity.Activity;
import org.naw.core.exchange.Message;
import org.naw.core.listener.LifeCycleListener;
import org.naw.core.partnerLink.PartnerLink;
import org.naw.core.pipeline.DefaultPipeline;
import org.naw.core.pipeline.Pipeline;
import org.naw.core.pipeline.Sink;
import org.naw.core.storage.Storage;
import org.naw.core.util.Selector;
import org.naw.core.util.Selectors;
import org.naw.core.util.SynchronizedHashMap;
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

	private final Map<String, PartnerLink> links;

	private final Map<String, Activity> activities;

	private Storage storage;

	private Timer timer;

	private DefaultPipeline pipeline;

	private ProcessFactory processFactory;

	private final Map<String, Process> instances;

	private final AtomicBoolean destroyed;

	private final Selector<LifeCycleListener> selector;

	/**
	 * create new instance of {@link DefaultProcessContext}
	 * 
	 * @param name
	 *            workflow name
	 */
	public DefaultProcessContext(String name) {
		this.name = name;

		links = new HashMap<String, PartnerLink>();
		activities = new HashMap<String, Activity>();

		storage = null;
		timer = null;
		pipeline = null;
		processFactory = DefaultProcessFactory.INSTANCE;

		instances = new SynchronizedHashMap<String, Process>();

		destroyed = new AtomicBoolean(false);

		selector = new Selector<LifeCycleListener>();
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

	public Pipeline getPipeline() {
		return pipeline;
	}

	public void setProcessFactory(ProcessFactory processFactory) {
		this.processFactory = processFactory;
	}

	public Selector<LifeCycleListener> getSelector() {
		return selector;
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
		String name = activity.getName();

		Activity old = activities.get(name);

		if (old == null) {
			activities.put(name, activity);
		} else if (!ObjectUtils.equals((Object) old, (Object) activity)) {
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

		Selectors.fireProcessContextInitialized(this);
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

		Process process = processFactory.newProcess();
		process.init(this);

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

		Process process = processFactory.newProcess(message);
		process.init(this);

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
	 * @see org.naw.process.ProcessContext#activate(org.naw.process.Process)
	 */
	public void activate(Process process) throws Exception {
		String pid = process.getId();

		if (process.getState() == TERMINATED) {
			throw new Exception("process " + pid + " already terminated");
		}

		synchronized (instances) {
			if (instances.containsKey(pid)) {
				throw new Exception("process " + pid + " already activated");
			}

			process.init(this);

			instances.put(pid, process);
		}
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

		// unlink partner links
		links.clear();

		// gc works
		pipeline = null;
		storage = null;
		timer = null;

		Selectors.fireProcessContextDestroyed(this);
	}
}
