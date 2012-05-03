package org.naw.core.task.support;

import org.naw.core.Engine;
import org.naw.core.task.SimpleTaskContext;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;
import org.naw.core.task.TaskContextFuture;
import org.naw.executables.Executable;

public abstract class TaskContextUtils {

	public static TaskContext getFirstTaskContext(TaskContext ref, Task... tasks) {
		return chain(ref.getEngine(), ref.getExecutable(), tasks);
	}

	public static TaskContext getFirstTaskContext(TaskContext ref, Iterable<Task> tasks) {
		return chain(ref.getEngine(), ref.getExecutable(), tasks);
	}

	/**
	 * chain tasks, create {@link TaskContext} for each task and return the first one.
	 * 
	 * @param engine
	 * @param executable
	 * @param tasks
	 * @return first {@link TaskContext} in the chain
	 */
	public static TaskContext chain(Engine engine, Executable executable, Task... tasks) {
		SimpleTaskContext first = null;
		SimpleTaskContext prev = null;
		
		for (int i = 0, n = tasks.length; i < n; ++i) {
			Task task = tasks[i];
			
			SimpleTaskContext curr = new SimpleTaskContext(engine, executable, task);
			
			if (first == null) {
				first = curr;
			} else if (prev != null) {
				prev.setNext(curr);
			}

			prev = curr;
		}

		return first;
	}

	/**
	 * chain tasks, create {@link TaskContext} for each task and return the first one.
	 * 
	 * @param engine
	 * @param executable
	 * @param tasks
	 * @return first {@link TaskContext} in the chain
	 */
	public static TaskContext chain(Engine engine, Executable executable, Iterable<Task> tasks) {
		SimpleTaskContext first = null;
		SimpleTaskContext prev = null;

		for (Task task : tasks) {
			SimpleTaskContext curr = new SimpleTaskContext(engine, executable, task);

			if (first == null) {
				first = curr;
			} else if (prev != null) {
				prev.setNext(curr);
			}
			
			prev = curr;
		}

		return first;
	}
	
	/**
	 * add new {@link TaskContext} to the chain after end of chain.
	 * 
	 * @param chain
	 * @param e
	 */
	public static void addLast(TaskContext chain, TaskContext e) {
		if ((chain == null) || !(chain instanceof SimpleTaskContext) || (e == null)) {
			return;
		}
		
		if (e.equals(chain)) {
			return;
		}
		
		SimpleTaskContext prev = (SimpleTaskContext) chain;
		
		while (prev.getNext() != null) {
			prev = (SimpleTaskContext) prev.getNext();

			if (e.equals(prev)) {
				return;
			}
		}

		prev.setNext(e);
	}

	/**
	 * create {@link TaskContext} and add the newly created {@link TaskContext} to
	 * the chain after end of chain.
	 * 
	 * @param chain
	 * @param e
	 */
	public static void addLast(TaskContext chain, Task e) {
		SimpleTaskContext curr = new SimpleTaskContext(chain.getEngine(), chain.getExecutable(), e);
		
		addLast(chain, curr);
	}
	
	public static TaskContextFuture createFuture(final Timeout timeout) {
		return new TaskContextFuture() {
			
			public void cancel() {
				timeout.cancel();
			}
		};
	}
	
	public static TaskContextFuture createFinishedFuture() {
		return new TaskContextFuture() {
			
			public void cancel() {
				// do nothing
			}
		};
	}
}
