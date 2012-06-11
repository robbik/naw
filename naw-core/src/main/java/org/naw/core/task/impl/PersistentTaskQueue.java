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

	public void attach(Engine engine) throws Exception {
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
	
	public void detach(Engine engine) {
		// do nothing
	}
	
	public void add(TaskContext context, MessageExchange mex) {
		if (log.isTraceEnabled()) {
			log.trace("adding task " + context.getTask() + " to execution queue");
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
				log.trace("task " + context.getTask() + " added to execution queue");
			}
		} else {
			if (mexId != null) {
				storage.remove(task.getId(), mexId);
			}
			
			log.error("unable to enqueue task " + context.getTask());
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
			log.trace("before executing task " + task);
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
			if (entry.recovery) {
				log.error("an error occured while recovering task " + task + ".", t);
			} else {
				log.error("an error occured while executing task " + task + ".", t);
			}
		}
		
		if (mexId != null) {
			storage.remove(task.getId(), mexId);
		}
		
		if (log.isTraceEnabled()) {
			log.trace("after executing task " + task);
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
