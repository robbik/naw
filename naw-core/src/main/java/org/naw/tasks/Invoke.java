package org.naw.tasks;

import org.naw.activities.support.AbstractActivity;
import org.naw.core.logging.Logger;
import org.naw.core.logging.LoggerFactory;
import org.naw.engine.NawProcess;
import org.naw.links.Link;

public class Invoke extends AbstractActivity {

	private Link partnerLink;

	private String requestVarName;

	private String responseVarName;

	private boolean oneWay;

	private boolean retriable;

	private Receive receive;

	private String ivarname;

	private Logger log;

	public void setPartnerLink(Link partnerLink) {
		this.partnerLink = partnerLink;
	}

	public void setVarName(String requestVarName) {
		this.requestVarName = requestVarName;
	}

	public void setResponseVarName(String responseVarName) {
		this.responseVarName = responseVarName;
	}

	public void setOneWay(boolean oneWay) {
		this.oneWay = oneWay;
	}

	public void setRetriable(boolean retriable) {
		this.retriable = retriable;
	}

	public void init() throws Exception {
		if (partnerLink == null) {
			throw new IllegalStateException("partner link cannot be null");
		}

		if (requestVarName == null) {
			throw new IllegalStateException(
					"request variable name cannot be null");
		}

		log = LoggerFactory.getLogger(super.activityQName);

		if (oneWay) {
			receive = null;
		} else {
			if (responseVarName == null) {
				throw new IllegalStateException(
						"response variable name cannot be null");
			}

			ivarname = super.activityQName.concat(".RECEIVE_TRACE_NO");

			receive = new Receive(ivarname);

			receive.setQName(super.activityQName.concat("$Receive"));

			receive.setVarName(responseVarName);

			receive.setNext(super.next);
		}
	}

	public void execute(NawProcess process, CompletionHandler completion)
			throws Exception {
		Object data = process.get(requestVarName);

		if (oneWay) {
			partnerLink.send(data);

			executeNext(process, completion);
		} else {
			process.set(ivarname, partnerLink.sendAndReceive(data));

			execute(receive, process, completion);
		}
	}

	public void recover(NawProcess process, CompletionHandler completion)
			throws Exception {
		if (retriable) {
			execute(process, completion);
		} else {
			log.warning("activity has been recovered but retry was disable");

			if (oneWay) {
				executeNext(process, completion);
			} else {
				execute(receive, process, completion);
			}
		}
	}
}
