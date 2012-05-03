package org.naw.engine;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.naw.activities.Activity;
import org.naw.core.logging.Logger;
import org.naw.core.logging.LoggerFactory;
import org.naw.core.task.SimpleTaskQueue;
import org.naw.core.task.TaskQueue;
import org.naw.core.task.TaskQueue.Entry;
import org.naw.core.task.support.HashedWheelTimer;
import org.naw.core.task.support.Timer;
import org.naw.core.utils.AlphaNumericUtils;
import org.naw.engine.config.NawProcessDefinition;
import org.naw.engine.storage.Storage;
import org.naw.tasks.CompletionHandler;

public class DefaultNawEngine implements NawEngine {

	private static final Logger log = LoggerFactory
			.getLogger(DefaultNawEngine.class);

	private static volatile String PREFIX_PID;

	private static final AtomicInteger COUNTER_PID = new AtomicInteger(0);

	static {
		Thread pidgen = new Thread() {

			public void run() {
				while (true) {
					DefaultNawEngine.PREFIX_PID = AlphaNumericUtils
							.bytesToString32(AlphaNumericUtils.asBytes(UUID
									.randomUUID()));

					try {
						Thread.sleep(10000);
					} catch (InterruptedException ie) {
						break;
					}
				}
			}
		};

		pidgen.setName("DefaultNawEngine#PIDGen");

		pidgen.setDaemon(true);
		pidgen.setPriority(Thread.MIN_PRIORITY);

		pidgen.start();
	}

	private Storage storage;

	private TaskQueue taskQueue;

	private Timer timer;

	private NawProcessFactory processFactory;

	public Storage getStorage() {
		return storage;
	}

	public void setStorage(Storage storage) {
		this.storage = storage;
	}

	public TaskQueue getTaskQueue() {
		return taskQueue;
	}

	public void setTaskQueue(TaskQueue taskQueue) {
		this.taskQueue = taskQueue;
	}

	public Timer getTimer() {
		return timer;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	public void setProcessFactory(NawProcessFactory processFactory) {
		this.processFactory = processFactory;
	}

	public void init(String... locations) throws Exception {
		if (timer == null) {
			if (taskQueue == null) {
				timer = new HashedWheelTimer();
			} else if (taskQueue instanceof SimpleTaskQueue) {
				timer = ((SimpleTaskQueue) taskQueue).getTimer();
			}
		}

		if (taskQueue == null) {
			taskQueue = new SimpleTaskQueue(timer);
		}

		if (processFactory == null) {
			processFactory = new SimpleNawProcessFactory();
		}

		// TODO put your application code here
	}

	public NawProcess exec(NawProcessDefinition def) throws Exception {
		return exec(def, null, null);
	}

	public NawProcess exec(NawProcessDefinition def, Map<String, Object> data)
			throws Exception {
		return exec(def, data, null);
	}

	public NawProcess exec(NawProcessDefinition def, CompletionHandler handler)
			throws Exception {
		return exec(null, handler);
	}

	public NawProcess exec(NawProcessDefinition def, Map<String, Object> data,
			CompletionHandler handler) throws Exception {

		if (def == null) {
			throw new NullPointerException("process definition");
		}

		String qName = PREFIX_PID.concat(String.valueOf(COUNTER_PID
				.incrementAndGet()));

		NawProcess proc = processFactory.newProcess(qName, this);

		if (data != null) {
			proc.set(data);
		}

		Activity activity = def.getFirstActivity();
		assert activity != null;

		taskQueue.add(activity, proc, handler);

		return proc;
	}

	public int stepping() {
		Entry entry = taskQueue.poll();
		if (entry == null) {
			if (timer.hasTimeout()) {
				return STEPPING_WAITING;
			}

			return STEPPING_NAVAIL;
		}

		Activity act = entry.getActivity();
		NawProcess proc = entry.getProcess();
		CompletionHandler handler = entry.getCompletionHandler();

		try {
			act.execute(proc, handler);
		} catch (Throwable t) {
			log.error(
					"an error occured while executing activity "
							+ act.getActivityQName() + " on process " + proc.getQName(),
					t);

			handler.error(proc, act, t);
		}

		return STEPPING_AVAIL;
	}
}
