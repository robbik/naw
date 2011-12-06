package org.naw.core;

import static org.naw.core.ProcessState.TERMINATED;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import org.naw.core.activity.Activity;
import org.naw.core.listener.LifeCycleListener;
import org.naw.core.logging.Logger;
import org.naw.core.logging.LoggerFactory;
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
import org.naw.core.util.internal.ProcessUtils;

/**
 * This class is default implementation of {@link org.naw.core.ProcessContext}
 * 
 * @see org.naw.core.ProcessContext
 * 
 */
public class DefaultProcessContext implements ProcessContext, Sink {

	private static final Logger log = LoggerFactory
			.getLogger(DefaultProcessContext.class);

	private static final int STATE_UNINITIALIZED = 0;

	private static final int STATE_RUNNING = 0;

	private static final int STATE_HIBERNATING = 1;

	private static final int STATE_SHUTTING_DOWN = 2;

	private static final int STATE_HIBERNATED = 3;

	private static final int STATE_SHUTDOWN = 4;

	private final String name;

	private final Map<String, PartnerLink> links;

	private final Map<String, Activity> activities;

	private Storage storage;

	private Timer timer;

	private Executor executor;

	private DefaultPipeline pipeline;

	private ProcessFactory processFactory;

	private final Map<String, Process> instances;

	private final AtomicInteger state;

	private final Selector<LifeCycleListener> selector;

	/**
	 * create new instance of {@link DefaultProcessContext}
	 * 
	 * @param name
	 *            process context name
	 */
	public DefaultProcessContext(String name) {
		this.name = name;

		links = new HashMap<String, PartnerLink>();
		activities = new HashMap<String, Activity>();

		storage = null;
		timer = null;
		executor = null;

		pipeline = null;
		processFactory = DefaultProcessFactory.INSTANCE;

		instances = new SynchronizedHashMap<String, Process>();

		state = new AtomicInteger(STATE_UNINITIALIZED);

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
	 * set timer used by this process context
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

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.core.ProcessContext#getExecutor()
	 */
	public Executor getExecutor() {
		return executor;
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
		if (state.get() != STATE_UNINITIALIZED) {
			throw new IllegalStateException("unexpected state " + state.get());
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
	 * set process context activities
	 * 
	 * @param activities
	 *            process context activities
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
		if (!state.compareAndSet(STATE_UNINITIALIZED, STATE_RUNNING)) {
			throw new IllegalStateException();
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
		if (state.get() != STATE_RUNNING) {
			return null; // not in running state
		}

		Process process = processFactory.newProcess();
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
	 * @see org.naw.core.ProcessContext#resume()
	 */
	public void resume() throws Exception {
		if (storage == null) {
			throw new UnsupportedOperationException("no storage defined");
		}

		if (state.get() != STATE_RUNNING) {
			init();
		}

		Process[] processes = storage.findByProcessContext(name);
		int len = processes.length;

		if (len == 0) {
			return;
		}

		for (int i = 0; i < len; ++i) {
			Process process = processes[i];
			if (process.getState() == TERMINATED) {
				continue;
			}

			String pid = process.getId();

			synchronized (instances) {
				if (instances.containsKey(pid)) {
					continue;
				}

				process.init(this);

				synchronized (process) {
					if (process.getState() != TERMINATED) {
						instances.put(pid, process);
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.process.ProcessContext#terminate(java.lang.String)
	 */
	public void terminate(String pid) {
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
	 * @see org.naw.process.ProcessContext#shutdown()
	 */
	public void shutdown() {
		if (!state.compareAndSet(STATE_RUNNING, STATE_SHUTTING_DOWN)) {
			return;
		}

		// waiting for running processes
		int running = 0, last = 0;

		do {
			synchronized (instances) {
				running = ProcessUtils.countRunning(instances.values());
			}

			if (running > 0) {
				if (running != last) {
					log.info("waiting for " + running
							+ " processes to shutdown");

					last = running;
				}

				try {
					Thread.sleep(500);
				} catch (InterruptedException ex) {
					break;
				}
			}
		} while (running > 0);

		if (running > 0) {
			synchronized (instances) {
				running = ProcessUtils.countRunning(instances.values());
			}

			if (running > 0) {
				log.info(running
						+ " running processes detected, forcing shutdown");
			}
		}

		destroy();
	}

	public void shutdownNow() {
		if (!state.compareAndSet(STATE_RUNNING, STATE_SHUTTING_DOWN)) {
			return;
		}

		destroy();
	}

	private void destroy() {
		// destroy instances
		synchronized (instances) {
			for (Process p : instances.values()) {
				p.destroy();
			}

			instances.clear();
		}

		// shutdown pipeline
		pipeline.shutdown();

		// unlink partner links
		links.clear();

		// gc works
		pipeline = null;
		storage = null;
		timer = null;

		state.set(STATE_SHUTDOWN);

		Selectors.fireProcessContextShutdown(this);
	}

	public void hibernate() {
		if (storage == null) {
			throw new UnsupportedOperationException("no storage defined");
		}

		if (!state.compareAndSet(STATE_RUNNING, STATE_HIBERNATING)) {
			return;
		}

		// hibernate pipeline
		pipeline.hibernate();

		// wait for running processes
		int running, sleeping, last;

		last = 0;
		do {
			synchronized (instances) {
				running = ProcessUtils.countRunning(instances.values());
			}

			if (running > 0) {
				if (running != last) {
					log.info("waiting for " + running
							+ " processes to be hibernated");

					last = running;
				}

				try {
					Thread.sleep(500);
				} catch (InterruptedException ex) {
					break;
				}
			}
		} while (running > 0);

		// wait at most 6 seconds for sleeping processes
		last = 0;
		sleeping = 0;

		for (int i = 0; i < 12; ++i) {
			synchronized (instances) {
				sleeping = ProcessUtils.countSleeping(instances.values());
			}

			if (sleeping == 0) {
				break;
			}

			if (sleeping != last) {
				log.info("waiting for " + sleeping
						+ " sleeping processes to be hibernated, " + (11 - i)
						+ " waits left");

				last = sleeping;
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException ex) {
				break;
			}
		}

		if (running > 0) {
			synchronized (instances) {
				running = ProcessUtils.countRunning(instances.values());
			}

			if (running > 0) {
				log.info(running
						+ " running processes detected, forcing hibernate");
			}
		}

		if (sleeping > 0) {
			synchronized (instances) {
				sleeping = ProcessUtils.countSleeping(instances.values());
			}

			if (sleeping > 0) {
				log.info(running
						+ " sleeping processes detected, forcing hibernate");
			}
		}

		state.set(STATE_HIBERNATED);
	}
}
