package org.naw.tasks;

import org.naw.core.task.DataExchange;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;
import org.naw.links.Link;

import rk.commons.ioc.factory.support.InitializingObject;
import rk.commons.ioc.factory.support.ObjectQNameAware;
import rk.commons.logging.Logger;
import rk.commons.logging.LoggerFactory;
import rk.commons.util.ObjectUtils;

public class Send implements Task, ObjectQNameAware, InitializingObject {

	private Link partnerLink;

	private String varName;

	private boolean retriable;
	
	private String exchangeId;
	
	private String objectQName;

	private Logger log;

	public void setPartnerLink(Link partnerLink) {
		this.partnerLink = partnerLink;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}

	public void setRetriable(boolean retriable) {
		this.retriable = retriable;
	}
	
	public void setExchangeId(String exchangeId) {
		this.exchangeId = exchangeId;
	}

	public void setObjectQName(String objectQName) {
		this.objectQName = objectQName;
		
		log = LoggerFactory.getLogger(objectQName);
	}

	public void initialize() throws Exception {
		if (exchangeId != null) {
			exchangeId = ObjectUtils.getPackageName(objectQName).concat(":sendAndReceive__exchange#").concat(exchangeId);
		}
	}

	public void run(TaskContext context, DataExchange exchange) throws Exception {
		if (exchangeId == null) {
			// stateless
			partnerLink.send(exchange.get(varName));
		} else {
			// stateful
			exchange.setpriv(exchangeId, partnerLink.sendAndReceive(exchange.get(varName)));
		}
		
		context.next(exchange);
	}

	public void recover(TaskContext context, DataExchange exchange) throws Exception {
		if (retriable) {
			if (exchangeId == null) {
				// stateless
				partnerLink.send(exchange.get(varName));
			} else {
				// stateful
				exchange.setpriv(exchangeId, partnerLink.sendAndReceive(exchange.get(varName)));
			}
		} else {
			log.warning("activity is recovered but retry is disabled");
		}

		context.next(exchange);
	}
}
