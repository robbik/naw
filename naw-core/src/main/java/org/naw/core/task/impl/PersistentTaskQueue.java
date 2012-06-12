package org.naw.core.task.impl;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.naw.core.Engine;
import org.naw.core.exchange.MessageExchange;
import org.naw.core.storage.Storage;
import org.naw.core.storage.StoredTask;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;
import org.naw.core.task.TaskQueue;
import org.naw.executables.Executable;

import rk.commons.inject.factory.ObjectFactory;
import rk.commons.logging.Logger;
import rk.commons.logging.LoggerFactory;

public class PersistentTaskQueue implements TaskQueue {

	private static final Logger log = LoggerFactory.getLogger(PersistentTaskQueue.class);

	private final BlockingQueue<Object> queue;

	private Storage storage;

	public PersistentTaskQueue() {
		this(new ArrayBlockingQueue<Object>(5000, true), null);
	}

	public PersistentTaskQueue(BlockingQueue<Object> queue) {
		this(queue, null);
	}
	
	public PersistentTaskQueue(Storage storage) {
		this(new ArrayBlockingQueue<Object>(5000, true), storage);
	}
	
	public PersistentTaskQueue(BlockingQueue<Object> queue, Storage storage) {
		this.queue = queue;
		this.storage = storage;
	}
	
	public void setStorage(Storage storage) {
		this.storage = storage;
	}

	public void attach(Engine engine, ObjectFactory factory) throws Exception {
		Collection<StoredTask> c = storage.getTasks();
		
		// recover running tasks
		for (StoredTask stored : c) {
			String taskId = stored.getTaskId();
			MessageExchange mex = stored.getMessageExchange();
			
			int status = stored.getStatus();
			if (status != StoredTask.STATUS_RUNNING) {
				continue;
			}
			
			Executable exec = engine.getExecutable(mex.getExecutableName());
			
			if (exec == null) {
				log.error("unable to recover task " + taskId + ", executable " + mex.getExecutableName() +
						" cannot be found");
			} else {
				TaskContext tctx = exec.getPipeline().getTaskContext(taskId);
				
				if (tctx == null) {
					log.error("unable to recover task " + taskId + ", task cannot be found in executable " +
							mex.getExecutableName());
				} else {
					queue.put(new Entry(tctx, mex, true));
				}
			}
		}
		
		// recover pending tasks
		for (StoredTask stored : c) {
			String taskId = stored.getTaskId();
			MessageExchange mex = stored.getMessageExchange();
			
			int status = stored.getStatus();
			if (status != StoredTask.STATUS_PENDING) {
				continue;
			}
			
			Executable exec = engine.getExecutable(mex.getExecutableName());
			
			if (exec == null) {
				log.error("unable to recover task " + taskId + ", executable " + mex.getExecutableName() +
						" cannot be found");
			} else {
				TaskContext tctx = exec.getPipeline().getTaskContext(taskId);
				
				if (tctx == null) {
					log.error("unable to recover task " + taskId + ", task cannot be found in executable " +
							mex.getExecutableName());
				} else {
					queue.put(new Entry(tctx, mex, false));
				}
			}
		}
	}
	
	public void detach() {
		// do nothing
	}
	
	public void add(TaskContext context, MessageExchange mex) {
		if (log.isTraceEnabled()) {
			if (mex == null) {
				log.trace("adding task " + context.getTask().getId() + " with no mex to execution queue");
			} else {
				log.trace("adding task " + context.getTask().getId() + " with mex #" + mex.getId() + " to execution queue");
			}
		}
		
		Task task = context.getTask();
		
		String mexId;
		if (mex == null) {
			mexId = null;
		} else {
			mexId = mex.getId();
		}
		
		if (mexId != null) {
			storage.persist(task.getId(), mex, StoredTask.STATUS_PENDING);
		}
		
		boolean enqueued = queue.add(new Entry(context, mex, false));
		
		if (enqueued) {
			if (log.isTraceEnabled()) {
				if (mex == null) {
					log.trace("task " + task.getId() + " with no mex added to execution queue");
				} else {
					log.trace("task " + task.getId() + " with mex #" + mexId + " added to execution queue");
				}
			}
		} else {
			if (mexId != null) {
				storage.remove(task.getId(), mexId);
			}
			
			if (mexId == null) {
				log.error("unable to enqueue task " + task.getId() + " with no mex");
			} else {
				log.error("unable to enqueue task " + task.getId() + " with mex #" + mexId);
			}
		}
	}

	public boolean next() throws InterruptedException {
		run((Entry) queue.take());
		
		return true;
	}

	public boolean next(long timeout, TimeUnit unit) throws InterruptedException {
		Entry e = (Entry) queue.poll(timeout, unit);
		if (e == null) {
			return false;
		}
		
		run(e);
		
		return true;
	}
	
	private void run(Entry entry) {
		TaskContext ctx = entry.taskContext;
		MessageExchange mex = entry.messageExchange;
		
		Task task = ctx.getTask();
		
		if (log.isTraceEnabled()) {
			if (mex == null) {
				log.trace("before executing task " + task.getId() + " with no mex");
			} else {
				log.trace("before executing task " + task.getId() + " with mex #" + mex.getId());
			}
		}
		
		String mexId;
		
		if (mex == null) {
			mexId = null;
		} else {
			mexId = mex.getId();
		}

		if (mexId != null) {
			storage.update(task.getId(), mexId, StoredTask.STATUS_RUNNING);
		}
		
		try {
			if (entry.recovery) {
				task.recover(ctx, mex);
			} else {
				task.run(ctx, mex);
			}
		} catch (Throwable t) {
			String what;
			
			if (entry.recovery) {
				what = "recovering";
			} else {
				what = "executing";
			}
			
			if (mex == null) {
				log.error("an error occured while " + what + " task " + task.getId() + " with no mex.", t);
			} else {
				log.error("an error occured while " + what + " task " + task.getId() + " mex #" + mex.getId() + ".", t);
			}

			return;
		}
		
		if (mexId != null) {
			storage.remove(task.getId(), mexId);
		}
		
		if (log.isTraceEnabled()) {
			if (mex == null) {
				log.trace("after executing task " + task.getId() + " with no mex");
			} else {
				log.trace("after executing task " + task.getId() + " with mex " + mex.getId());
			}
		}
	}
	
	private static class Entry {
		final TaskContext taskContext;

		final MessageExchange messageExchange;
		
		final boolean recovery;

		Entry(TaskContext taskContext, MessageExchange exchange, boolean recovery) {
			this.taskContext = taskContext;
			this.messageExchange = exchange;
			
			this.recovery = recovery;
		}
	}
}
