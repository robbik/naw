package org.naw.core.activity;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.naw.core.Process;
import org.naw.core.ProcessState;
import org.naw.core.partnerLink.MessageEvent;
import org.naw.core.partnerLink.PartnerLink;
import org.naw.core.partnerLink.PartnerLinkListener;
import org.naw.core.pipeline.DefaultPipeline;
import org.naw.core.pipeline.Pipeline;
import org.naw.core.pipeline.Sink;

public class PickOnMessage implements PartnerLinkListener, Sink {

	private final Pick parent;

	private ActivityContext ctx;

	private String partnerLink;

	private String operation;

	private String variable;

	private String correlationAttribute;

	private boolean createInstance;

	private boolean oneWay;

	private Activity[] activities;

	private String attrName;

	private PartnerLink link;

	private DefaultPipeline pipeline;

	private final AtomicBoolean destroyed;

	public PickOnMessage(Pick parent) {
		this.parent = parent;

		destroyed = new AtomicBoolean(false);
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

		if (activities == null) {
			pipeline = null;
		} else {
			pipeline = new DefaultPipeline(ctx.getPipeline());
			pipeline.setActivities(activities);
			pipeline.setProcessContext(ctx.getProcessContext());
			pipeline.setSink(this);

			activities = null;
		}

		attrName = "EXCHANGE$" + operation;

		createInstance = parent.isCreateInstance();

		link = ctx.getProcessContext().findPartnerLink(partnerLink);
		if (link == null) {
			throw new IllegalArgumentException("partner link " + partnerLink
					+ " cannot be found");
		}

		link.subscribe(operation, this);
	}

	public void messageReceived(MessageEvent e) {
		Map<String, Object> message = e.getMessage();

		Process process = null;

		if (createInstance) {
			process = ctx.getProcessContext().newProcess();
		} else {
			process = ctx.getProcessContext().findProcess(
					(String) message.get(correlationAttribute));
		}

		if (process == null) {
			return;
		}

		if (createInstance
				|| process.compareAndUpdate(ProcessState.BEFORE_ACTIVITY,
						parent, ProcessState.ONGOING_ACTIVITY, parent)) {
			parent.afterExecute(process);

			if (!oneWay) {
				process.setAttribute(attrName, e.getSource());
			}

			process.getMessage().set(variable, message);

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

	public void destroy() {
		if (!destroyed.compareAndSet(false, true)) {
			return;
		}

		// unlink from partnerLink
		link.unsubscribe(operation, this);

		// destroy pipeline
		if (pipeline != null) {
			pipeline.destroy();
		}

		// gc works
		ctx = null;
		activities = null;
		link = null;
		pipeline = null;
	}
}
