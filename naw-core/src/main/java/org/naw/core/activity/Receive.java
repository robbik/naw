package org.naw.core.activity;

import static org.naw.core.ProcessState.BEFORE;
import static org.naw.core.ProcessState.SLEEP;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.naw.core.Process;
import org.naw.core.ProcessState;
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

	private final AtomicBoolean shutdown;

	public Receive(String name) {
		super(name);

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
		if (shutdown.get()) {
			return;
		}

		Map<String, Object> message = e.getMessage();

		Process process = null;

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
				ok = process.compare(BEFORE, this)
						|| process.compare(SLEEP, this);
			}

			if (ok) {
				if (!oneWay) {
					process.setAttribute(attrName, e.getSource());
				}

				process.getMessage().set(variable, message);

				process.update(ProcessState.AFTER, this);
			}
		}

		if (ok) {
			ctx.execute(process);
		}
	}

	public void execute(Process process) throws Exception {
		process.compareAndUpdate(BEFORE, this, SLEEP);
	}

	@Override
	public void shutdown() {
		if (!shutdown.compareAndSet(false, true)) {
			return;
		}

		super.shutdown();

		// un-subscribe from partner link
		link.unsubscribe(operation, this);

		// let gc do its work
		link = null;
		ctx = null;
	}
}
