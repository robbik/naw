package org.naw.tasks;

import org.naw.core.task.DataExchange;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;
import org.naw.exceptions.LinkException;
import org.naw.links.Link;

import rk.commons.inject.factory.support.InitializingObject;
import rk.commons.inject.factory.support.ObjectQNameAware;
import rk.commons.logging.Logger;
import rk.commons.logging.LoggerFactory;
import rk.commons.util.ObjectUtils;

public class Send implements Task, ObjectQNameAware, InitializingObject {

	private Link partnerLink;

	private String varName;

	private boolean retriable;
	
	private String exchangeVarName;
	
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
	
	public void setExchangeVarName(String exchangeVarName) {
		this.exchangeVarName = exchangeVarName;
	}

	public void setObjectQName(String objectQName) {
		this.objectQName = objectQName;
		
		log = LoggerFactory.getLogger(objectQName);
	}

	public void initialize() throws Exception {
		if (exchangeVarName != null) {
			exchangeVarName = ObjectUtils.getPackageName(objectQName).concat(
					":sendAndReceive__exchange#").concat(exchangeVarName);
		}
	}

	public void run(TaskContext context, DataExchange exchange) throws Exception {
		int errorCode = 0;

		if (exchangeVarName == null) {
			// oneWay
			try {
				partnerLink.send(exchange.get(varName), true);
			} catch (LinkException e) {
				errorCode = e.getErrorCode();
			}
		} else {
			// request-response
			try {
				exchange.setpriv(exchangeVarName, partnerLink.send(exchange.get(varName), false));
			} catch (LinkException e) {
				errorCode = e.getErrorCode();
			}
		}
		
		exchange.setLastError(errorCode);
		
		context.next(exchange);
	}

	public void recover(TaskContext context, DataExchange exchange) throws Exception {
		int errorCode = 0;

		if (retriable) {
			if (exchangeVarName == null) {
				// oneWay
				try {
					partnerLink.send(exchange.get(varName), true);
				} catch (LinkException e) {
					errorCode = e.getErrorCode();
				}
			} else {
				// request-response
				try {
					exchange.setpriv(exchangeVarName, partnerLink.send(exchange.get(varName), false));
				} catch (LinkException e) {
					errorCode = e.getErrorCode();
				}
			}
		} else {
			log.warning("activity is recovered but retry is disabled");
		}

		exchange.setLastError(errorCode);
		
		context.next(exchange);
	}
}
