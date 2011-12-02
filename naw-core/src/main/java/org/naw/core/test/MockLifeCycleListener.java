package org.naw.core.test;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.naw.core.Process;
import org.naw.core.ProcessContext;
import org.naw.core.ProcessState;
import org.naw.core.activity.Activity;
import org.naw.core.listener.SimpleLifeCycleListener;
import org.naw.core.logging.Logger;
import org.naw.core.logging.LoggerFactory;
import org.naw.core.util.internal.ObjectUtils;

public class MockLifeCycleListener extends SimpleLifeCycleListener {

	private static final Logger log = LoggerFactory
			.getLogger(MockLifeCycleListener.class);

	private final Object monitor;

	private final Queue<ProcessContext> qctx;

	private final Queue<Process> qprocess;

	private final Queue<ProcessState> qstate;

	private final Queue<Activity> qactivity;

	private final Queue<ProcessContext> qxctx;

	private final Queue<Process> qxprocess;

	private final Queue<ProcessState> qxstate;

	private final Queue<Activity> qxactivity;

	public MockLifeCycleListener() {
		monitor = new Object();

		qctx = new LinkedList<ProcessContext>();
		qprocess = new LinkedList<Process>();
		qstate = new LinkedList<ProcessState>();
		qactivity = new LinkedList<Activity>();

		qxctx = new LinkedList<ProcessContext>();
		qxprocess = new LinkedList<Process>();
		qxstate = new LinkedList<ProcessState>();
		qxactivity = new LinkedList<Activity>();
	}

	public void expectProcessCreated() {
		synchronized (monitor) {
			qxctx.add(null);
			qxprocess.add(null);

			qxstate.add(ProcessState.INIT);
			qxactivity.add(null);
		}
	}

	public void expectProcessStateChanged(ProcessState state, Activity activity) {
		synchronized (monitor) {
			qxctx.add(null);
			qxprocess.add(null);

			qxstate.add(state);
			qxactivity.add(activity);
		}
	}

	public void expectProcessTerminated() {
		synchronized (monitor) {
			qxctx.add(null);
			qxprocess.add(null);

			qxstate.add(ProcessState.TERMINATED);
			qxactivity.add(null);
		}
	}

	public void expectProcessContextInitialized(ProcessContext ctx) {
		synchronized (monitor) {
			qxctx.add(ctx);
			qxprocess.add(null);

			qxstate.add(ProcessState.INIT);
			qxactivity.add(null);
		}
	}

	public void expectProcessContextDestroyed(ProcessContext ctx) {
		synchronized (monitor) {
			qxctx.add(ctx);
			qxprocess.add(null);
			qxstate.add(ProcessState.TERMINATED);
			qxactivity.add(null);
		}
	}

	public void reset() {
		synchronized (monitor) {
			qctx.clear();
			qprocess.clear();
			qstate.clear();
			qactivity.clear();

			qxctx.clear();
			qxprocess.clear();
			qxstate.clear();
			qxactivity.clear();

			monitor.notifyAll();
		}
	}

	private Boolean expected() {
		ProcessContext ctx = qctx.peek();
		Process process = qprocess.peek();
		ProcessState state = qstate.peek();
		Activity activity = qactivity.peek();

		ProcessContext xctx = qxctx.peek();
		Process xprocess = qxprocess.peek();
		ProcessState xstate = qxstate.peek();
		Activity xactivity = qxactivity.peek();

		if (ObjectUtils.equals(ctx, xctx)
				&& ObjectUtils.equals(process, xprocess)
				&& ObjectUtils.equals(state, xstate)
				&& ObjectUtils.equals(activity, xactivity)) {

			qctx.poll();
			qprocess.poll();
			qstate.poll();
			qactivity.poll();

			qxctx.poll();
			qxprocess.poll();
			qxstate.poll();
			qxactivity.poll();

			if (qxctx.isEmpty()) {
				qctx.clear();
				qprocess.clear();
				qstate.clear();
				qactivity.clear();

				return Boolean.TRUE;
			}

			return null; // we want more
		}

		return Boolean.FALSE;
	}

	public void assertExpected(long wait, TimeUnit unit) {
		assertExpected(null, wait, unit);
	}

	public void assertExpected(String message, long wait, TimeUnit unit) {
		long deadline = System.currentTimeMillis() + unit.toMillis(wait);
		Boolean ok;

		synchronized (monitor) {
			while (!Boolean.TRUE.equals(ok = expected())) {
				if (ok == null) {
					Thread.yield();
				} else {
					long realWait = deadline - System.currentTimeMillis();
					if (realWait <= 0) {
						throw new AssertionError(message == null ? "" : message);
					}

					try {
						monitor.wait(realWait);
					} catch (InterruptedException e) {
						throw new RuntimeException("assertion interrupted");
					}
				}
			}
		}
	}

	@Override
	public void processCreated(ProcessContext ctx, Process process) {
		synchronized (monitor) {
			if (log.isTraceEnabled()) {
				log.trace("process " + process.getId() + " created");
			}

			if (qxctx.isEmpty()) {
				return;
			}

			qctx.add(null);
			qprocess.add(null);
			qstate.add(ProcessState.INIT);
			qactivity.add(null);

			monitor.notifyAll();
		}
	}

	@Override
	public void processStateChange(ProcessContext ctx, Process process) {
		synchronized (monitor) {
			if (log.isTraceEnabled()) {
				log.trace("process " + process.getId() + " state changed to "
						+ process.getState() + "@"
						+ process.getActivity().getName());
			}

			if (qxctx.isEmpty()) {
				return;
			}

			qctx.add(null);
			qprocess.add(null);
			qstate.add(process.getState());
			qactivity.add(process.getActivity());

			monitor.notifyAll();
		}
	}

	@Override
	public void processTerminated(ProcessContext ctx, Process process) {
		synchronized (monitor) {
			if (log.isTraceEnabled()) {
				log.trace("process " + process.getId() + " terminated");
			}

			if (qxctx.isEmpty()) {
				return;
			}

			qctx.add(null);
			qprocess.add(null);
			qstate.add(ProcessState.TERMINATED);
			qactivity.add(null);

			monitor.notifyAll();
		}
	}

	@Override
	public void processContextInitialized(ProcessContext ctx) {
		synchronized (monitor) {
			if (log.isTraceEnabled()) {
				log.trace("process context " + ctx.getName() + " initialized");
			}

			if (qxctx.isEmpty()) {
				return;
			}

			qctx.add(ctx);
			qprocess.add(null);
			qstate.add(ProcessState.INIT);
			qactivity.add(null);

			monitor.notifyAll();
		}
	}

	@Override
	public void processContextDestroyed(ProcessContext ctx) {
		synchronized (monitor) {
			if (log.isTraceEnabled()) {
				log.trace("process context " + ctx.getName() + " destroyed");
			}

			if (qxctx.isEmpty()) {
				return;
			}

			qctx.add(ctx);
			qprocess.add(null);
			qstate.add(ProcessState.TERMINATED);
			qactivity.add(null);

			monitor.notifyAll();
		}
	}
}
