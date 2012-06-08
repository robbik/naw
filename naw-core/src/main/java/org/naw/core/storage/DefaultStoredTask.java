package org.naw.core.storage;

import org.naw.core.exchange.MessageExchange;

public class DefaultStoredTask implements StoredTask {

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
