package org.naw.core.activity;

import static org.naw.core.ProcessState.AFTER;
import static org.naw.core.ProcessState.BEFORE;
import static org.naw.core.ProcessState.SLEEP;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.naw.core.Process;
import org.naw.core.ProcessContext;
import org.naw.core.partnerLink.MessageEvent;
import org.naw.core.partnerLink.PartnerLink;
import org.naw.core.partnerLink.PartnerLinkListener;
import org.naw.core.pipeline.DefaultPipeline;
import org.naw.core.pipeline.Pipeline;
import org.naw.core.pipeline.Sink;

public class PickOnMessage implements PartnerLinkListener, Sink {

	private final Pick parent;

	private ActivityContext ctx;

	private ProcessContext procctx;

	private String partnerLink;

	private String operation;

	private String variable;

	private String correlationAttribute;

	private boolean createInstance;

	private boolean oneWay;

	private Activity[] activities;

	private DefaultPipeline pipeline;

	private String attrName;

	private PartnerLink link;

	private final AtomicBoolean shutdown;

	public PickOnMessage(Pick parent) {
		this.parent = parent;

		shutdown = new AtomicBoolean(false);
	}

	public void setPartnerLink(String partnerLink) {
		this.partnerLink = partnerLink;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}

	public void setCorrelationAttribute(String correlationAttribute) {
		this.correlationAttribute = correlationAttribute;
	}

	public void setOneWay(boolean oneWay) {
		this.oneWay = oneWay;
	}

	public void setActivities(Activity... activities) {
		this.activities = activities;
	}

	public void init(ActivityContext ctx) throws Exception {
		this.ctx = ctx;
		procctx = ctx.getProcessContext();

		createInstance = parent.isCreateInstance();

		if ((activities == null) || (activities.length == 0)) {
			pipeline = null;
		} else {
			pipeline = new DefaultPipeline(ctx.getPipeline());
			pipeline.setActivities(activities);
			pipeline.setProcessContext(procctx);
			pipeline.setSink(this);

			activities = null;
			pipeline.init();
		}

		attrName = "EXCHANGE$" + operation;

		link = procctx.findPartnerLink(partnerLink);
		if (link == null) {
			throw new IllegalArgumentException("partner link " + partnerLink
					+ " cannot be found");
		}

		link.subscribe(operation, this);
	}

	public void messageReceived(MessageEvent e) {
		Map<String, Object> message = e.getMessage();

		Process process;

		if (createInstance) {
			process = procctx.newProcess();
		} else {
			process = procctx.findProcess((String) message
					.get(correlationAttribute));
		}

		if (process == null) {
			return;
		}

		boolean ok = createInstance;

		synchronized (process) {
			if (!ok) {
				ok = process.compare(BEFORE, parent)
						|| process.compare(SLEEP, parent);
			}

			if (ok) {
				parent.afterExecute(process);

				if (!oneWay) {
					process.setAttribute(attrName, e.getSource());
				}

				process.getMessage().set(variable, message);

				if (pipeline == null) {
					process.update(AFTER, parent);
				} else {
					process.update(BEFORE, pipeline.getFirstActivity());
				}
			}
		}

		if (ok) {
			if (pipeline == null) {
				ctx.execute(process);
			} else {
				pipeline.execute(process);
			}
		}
	}

	public void sunk(Pipeline pipeline, Process process) {
		ctx.execute(process);
	}

	public void hibernate() {
		// hibernate pipeline
		if (pipeline != null) {
			pipeline.hibernate();
		}
	}

	public void shutdown() {
		if (!shutdown.compareAndSet(false, true)) {
			return;
		}

		// unlink from partnerLink
		link.unsubscribe(operation, this);

		// shutdown pipeline
		if (pipeline != null) {
			pipeline.shutdown();
		}

		// gc works
		procctx = null;
		ctx = null;

		activities = null;
		pipeline = null;

		link = null;
	}
}
