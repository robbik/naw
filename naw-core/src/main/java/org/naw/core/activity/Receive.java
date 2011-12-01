package org.naw.core.activity;

import static org.naw.core.ProcessState.BEFORE;
import static org.naw.core.ProcessState.ON;
import static org.naw.core.ProcessState.SLEEP;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.naw.core.Process;
import org.naw.core.partnerLink.MessageEvent;
import org.naw.core.partnerLink.PartnerLink;
import org.naw.core.partnerLink.PartnerLinkListener;

/**
 * RECEIVE
 */
public class Receive extends AbstractActivity implements PartnerLinkListener {

	private String partnerLink;

	private String operation;

	private String variable;

	private String correlationAttribute;

	private boolean createInstance;

	private boolean oneWay;

	private String attrName;

	private PartnerLink link;

	private final AtomicBoolean destroyed;

	public Receive(String name) {
		super(name);

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

	public void setCreateInstance(boolean createInstance) {
		this.createInstance = createInstance;
	}

	public void setOneWay(boolean oneWay) {
		this.oneWay = oneWay;
	}

	@Override
	public void init(ActivityContext ctx) throws Exception {
		super.init(ctx);

		attrName = "EXCHANGE$" + operation;

		link = procctx.findPartnerLink(partnerLink);
		if (link == null) {
			throw new IllegalArgumentException("partner link " + partnerLink
					+ " cannot be found");
		}

		link.subscribe(operation, this);
	}

	public void messageReceived(MessageEvent e) {
		if (destroyed.get()) {
			return;
		}

		Map<String, Object> message = e.getMessage();

		Process process = null;

		if (createInstance) {
			process = procctx.newProcess();
		} else {
			process = procctx.findProcess((String) message.get(correlationAttribute));
		}

		if (process == null) {
			return;
		}

		boolean ok = createInstance;

		if (ok) {
			process.update(ON, this);
		} else {
			ok = process.compareAndUpdate(BEFORE, this, ON);

			if (!ok) {
				ok = process.compareAndUpdate(SLEEP, this, ON);
			}
		}

		if (ok) {
			if (!oneWay) {
				process.setAttribute(attrName, e.getSource());
			}

			process.getMessage().set(variable, message);

			ctx.execute(process);
		}
	}

	public void execute(Process process) throws Exception {
		process.compareAndUpdate(BEFORE, this, SLEEP);
	}

	@Override
	public void destroy() {
		super.destroy();

		if (!destroyed.compareAndSet(false, true)) {
			return;
		}

		// unsubscribe from partner link
		link.unsubscribe(operation, this);

		// let gc do its work
		link = null;
		ctx = null;
	}
}
