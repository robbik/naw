package org.naw.engine.test;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.naw.activities.Activity;
import org.naw.core.logging.Logger;
import org.naw.core.logging.LoggerFactory;
import org.naw.core.utils.ObjectUtils;
import org.naw.engine.NawProcess;
import org.naw.engine.ProcessInstance;
import org.naw.engine.RelativePosition;
import org.naw.engine.listener.SimpleLifeCycleListener;

public class MockLifeCycleListener extends SimpleLifeCycleListener {

	private static final Logger log = LoggerFactory
			.getLogger(MockLifeCycleListener.class);

	private final Object monitor;

	private final Queue<NawProcess> qctx;

	private final Queue<ProcessInstance> qprocess;

	private final Queue<RelativePosition> qstate;

	private final Queue<Activity> qactivity;

	private final Queue<NawProcess> qxctx;

	private final Queue<ProcessInstance> qxprocess;

	private final Queue<RelativePosition> qxstate;

	private final Queue<Activity> qxactivity;

	public MockLifeCycleListener() {
		monitor = new Object();

		qctx = new LinkedList<NawProcess>();
		qprocess = new LinkedList<ProcessInstance>();
		qstate = new LinkedList<RelativePosition>();
		qactivity = new LinkedList<Activity>();

		qxctx = new LinkedList<NawProcess>();
		qxprocess = new LinkedList<ProcessInstance>();
		qxstate = new LinkedList<RelativePosition>();
		qxactivity = new LinkedList<Activity>();
	}

	public void expectProcessCreated() {
		synchronized (monitor) {
			qxctx.add(null);
			qxprocess.add(null);

			qxstate.add(RelativePosition.INIT);
			qxactivity.add(null);
		}
	}

	public void expectProcessStateChanged(RelativePosition state, Activity activity) {
		synchronized (monitor) {
			qxctx.add(null);
			qxprocess.add(null);

			qxstate.add(state);
			qxactivity.add(activity);
		}
	}

	public void expectProcessStateChanged(RelativePosition state,
			String activityName) {
		synchronized (monitor) {
			qxctx.add(null);
			qxprocess.add(null);

			qxstate.add(state);
			qxactivity.add(new MockActivity(activityName));
		}
	}

	public void expectProcessTerminated() {
		synchronized (monitor) {
			qxctx.add(null);
			qxprocess.add(null);

			qxstate.add(RelativePosition.TERMINATED);
			qxactivity.add(null);
		}
	}

	public void expectProcessContextInitialized(NawProcess ctx) {
		synchronized (monitor) {
			qxctx.add(ctx);
			qxprocess.add(null);

			qxstate.add(RelativePosition.INIT);
			qxactivity.add(null);
		}
	}

	public void expectProcessContextDestroyed(NawProcess ctx) {
		synchronized (monitor) {
			qxctx.add(ctx);
			qxprocess.add(null);
			qxstate.add(RelativePosition.TERMINATED);
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
		NawProcess ctx = qctx.peek();
		ProcessInstance process = qprocess.peek();
		RelativePosition state = qstate.peek();
		Activity activity = qactivity.peek();

		NawProcess xctx = qxctx.peek();
		ProcessInstance xprocess = qxprocess.peek();
		RelativePosition xstate = qxstate.peek();
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

	public void assertNotExpected(long wait, TimeUnit unit) {
		assertNotExpected(null, wait, unit);
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

	public void assertNotExpected(String message, long wait, TimeUnit unit) {
		long deadline = System.currentTimeMillis() + unit.toMillis(wait);
		Boolean ok;

		synchronized (monitor) {
			while (!Boolean.TRUE.equals(ok = expected())) {
				if (ok == null) {
					Thread.yield();
				} else {
					long realWait = deadline - System.currentTimeMillis();
					if (realWait <= 0) {
						return;
					}

					try {
						monitor.wait(realWait);
					} catch (InterruptedException e) {
						throw new RuntimeException("assertion interrupted");
					}
				}
			}
		}

		throw new AssertionError(message == null ? "" : message);
	}

	@Override
	public void processCreated(NawProcess ctx, ProcessInstance process) {
		synchronized (monitor) {
			if (log.isTraceEnabled()) {
				log.trace("process " + process.getId() + " created");
			}

			if (qxctx.isEmpty()) {
				return;
			}

			qctx.add(null);
			qprocess.add(null);
			qstate.add(RelativePosition.INIT);
			qactivity.add(null);

			monitor.notifyAll();
		}
	}

	@Override
	public void processStateChange(NawProcess ctx, ProcessInstance process) {
		synchronized (monitor) {
			Activity activity = process.getActivity();

			if (log.isTraceEnabled()) {
				log.trace("process " + process.getId() + " state changed to "
						+ process.getState()
						+ (activity == null ? "" : "@" + activity.getName()));
			}

			if (qxctx.isEmpty()) {
				return;
			}

			qctx.add(null);
			qprocess.add(null);
			qstate.add(process.getState());
			qactivity.add(activity);

			monitor.notifyAll();
		}
	}

	@Override
	public void processTerminated(NawProcess ctx, ProcessInstance process) {
		synchronized (monitor) {
			if (log.isTraceEnabled()) {
				log.trace("process " + process.getId() + " terminated");
			}

			if (qxctx.isEmpty()) {
				return;
			}

			qctx.add(null);
			qprocess.add(null);
			qstate.add(RelativePosition.TERMINATED);
			qactivity.add(null);

			monitor.notifyAll();
		}
	}

	@Override
	public void processContextInitialized(NawProcess ctx) {
		synchronized (monitor) {
			if (log.isTraceEnabled()) {
				log.trace("process context " + ctx.getName() + " initialized");
			}

			if (qxctx.isEmpty()) {
				return;
			}

			qctx.add(ctx);
			qprocess.add(null);
			qstate.add(RelativePosition.INIT);
			qactivity.add(null);

			monitor.notifyAll();
		}
	}

	@Override
	public void processContextShutdown(NawProcess ctx) {
		synchronized (monitor) {
			if (log.isTraceEnabled()) {
				log.trace("process context " + ctx.getName() + " destroyed");
			}

			if (qxctx.isEmpty()) {
				return;
			}

			qctx.add(ctx);
			qprocess.add(null);
			qstate.add(RelativePosition.TERMINATED);
			qactivity.add(null);

			monitor.notifyAll();
		}
	}
}
