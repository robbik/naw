package org.naw.core.storage;

import java.io.Serializable;

import org.naw.core.exchange.MessageExchange;

public class DefaultStoredTask implements StoredTask, Serializable {

	private static final long serialVersionUID = -3468563508796149649L;

	private final String taskId;

	private final MessageExchange messageExchange;

	private int status;

	public DefaultStoredTask(String taskId, MessageExchange messageExchange, int status) {
		this.taskId = taskId;
		this.messageExchange = messageExchange;
		this.status = status;
	}

	public String getTaskId() {
		return taskId;
	}

	public MessageExchange getMessageExchange() {
		return messageExchange;
	}

	public int getStatus() {
		return status;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
}
