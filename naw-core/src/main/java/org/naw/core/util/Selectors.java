package org.naw.core.util;

import java.util.List;

import org.naw.core.Process;
import org.naw.core.ProcessContext;
import org.naw.core.ProcessLifeCycleListener;
import org.naw.core.ProcessState;
import org.naw.core.activity.Activity;

public abstract class Selectors {

	public static void fireProcessStateChange(ProcessContext ctx,
			Process process, ProcessState newState, Activity newActivity) {

		Selector<ProcessLifeCycleListener> selector = ctx.getSelector();

		List<ProcessLifeCycleListener> list = selector
				.select(ProcessLifeCycleListener.STATE_CHANGE);

		if (list != null) {
			synchronized (list) {
				for (int i = 0, len = list.size(); i < len; ++i) {
					list.get(i).processStateChange(ctx, process, newState,
							newActivity);
				}
			}
		}
	}

	public static void fireProcessBeginWait(ProcessContext ctx,
			Process process, Activity activity) {

		Selector<ProcessLifeCycleListener> selector = ctx.getSelector();

		List<ProcessLifeCycleListener> list = selector
				.select(ProcessLifeCycleListener.BEGIN_WAIT);

		if (list != null) {
			synchronized (list) {
				for (int i = 0, len = list.size(); i < len; ++i) {
					list.get(i).processBeginWait(ctx, process, activity);
				}
			}
		}
	}

	public static void fireProcessEndWait(ProcessContext ctx, Process process,
			Activity activity) {

		Selector<ProcessLifeCycleListener> selector = ctx.getSelector();

		List<ProcessLifeCycleListener> list = selector
				.select(ProcessLifeCycleListener.END_WAIT);

		if (list != null) {
			synchronized (list) {
				for (int i = 0, len = list.size(); i < len; ++i) {
					list.get(i).processEndWait(ctx, process, activity);
				}
			}
		}
	}
}
