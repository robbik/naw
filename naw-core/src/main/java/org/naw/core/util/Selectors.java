package org.naw.core.util;

import static org.naw.core.listener.LifeCycleListener.Category.PROCESS_CONTEXT_INITIALIZED;
import static org.naw.core.listener.LifeCycleListener.Category.PROCESS_CONTEXT_SHUTDOWN;
import static org.naw.core.listener.LifeCycleListener.Category.PROCESS_CREATED;
import static org.naw.core.listener.LifeCycleListener.Category.PROCESS_STATE_CHANGE;
import static org.naw.core.listener.LifeCycleListener.Category.PROCESS_TERMINATED;

import java.util.List;

import org.naw.core.Process;
import org.naw.core.ProcessContext;
import org.naw.core.listener.LifeCycleListener;

public abstract class Selectors {

	public static final Object[] ALL_SELECTIONS = new Object[] {
			PROCESS_STATE_CHANGE, PROCESS_CREATED, PROCESS_TERMINATED,
			PROCESS_CONTEXT_INITIALIZED, PROCESS_CONTEXT_SHUTDOWN };

	public static void fireProcessStateChange(ProcessContext ctx,
			Process process) {
		Selector<LifeCycleListener> selector = ctx.getSelector();

		List<LifeCycleListener> list = selector.select(PROCESS_STATE_CHANGE);

		if (list != null) {
			synchronized (list) {
				for (int i = 0, len = list.size(); i < len; ++i) {
					list.get(i).processStateChange(ctx, process);
				}
			}
		}
	}

	public static void fireProcessCreated(ProcessContext ctx, Process process) {
		Selector<LifeCycleListener> selector = ctx.getSelector();

		List<LifeCycleListener> list = selector.select(PROCESS_CREATED);

		if (list != null) {
			synchronized (list) {
				for (int i = 0, len = list.size(); i < len; ++i) {
					list.get(i).processCreated(ctx, process);
				}
			}
		}
	}

	public static void fireProcessTerminated(ProcessContext ctx, Process process) {
		Selector<LifeCycleListener> selector = ctx.getSelector();

		List<LifeCycleListener> list = selector.select(PROCESS_TERMINATED);

		if (list != null) {
			synchronized (list) {
				for (int i = 0, len = list.size(); i < len; ++i) {
					list.get(i).processTerminated(ctx, process);
				}
			}
		}
	}

	public static void fireProcessContextInitialized(ProcessContext ctx) {
		Selector<LifeCycleListener> selector = ctx.getSelector();

		List<LifeCycleListener> list = selector
				.select(PROCESS_CONTEXT_INITIALIZED);

		if (list != null) {
			synchronized (list) {
				for (int i = 0, len = list.size(); i < len; ++i) {
					list.get(i).processContextInitialized(ctx);
				}
			}
		}
	}

	public static void fireProcessContextShutdown(ProcessContext ctx) {
		Selector<LifeCycleListener> selector = ctx.getSelector();

		List<LifeCycleListener> list = selector
				.select(PROCESS_CONTEXT_SHUTDOWN);

		if (list != null) {
			synchronized (list) {
				for (int i = 0, len = list.size(); i < len; ++i) {
					list.get(i).processContextShutdown(ctx);
				}
			}
		}
	}
}
