package org.naw.links;

public class DefaultAsyncResult<T> implements AsyncResult<T> {
	
	protected volatile boolean failed;
	
	protected volatile boolean success;
	
	protected volatile boolean cancelled;
	
	protected volatile Throwable cause;
	
	protected volatile T result;
	
	protected volatile Object attachment;
	
	public DefaultAsyncResult(Object attachment) {
		failed = false;
		success = false;
		cancelled = false;
		
		cause = null;
		result = null;
		
		this.attachment = attachment;
	}

	public synchronized boolean isDone() {
		return success || cancelled || failed;
	}

	public synchronized boolean isSuccess() {
		return success;
	}

	public synchronized boolean isCancelled() {
		return cancelled;
	}

	public synchronized boolean cancel() {
		cancelled = docancel();
		
		return cancelled;
	}

	public synchronized T getResult() {
		return result;
	}

	public synchronized Throwable getCause() {
		return cause;
	}

	public synchronized void setSuccess(T result) {
		this.result = result;
		success = true;
	}

	public synchronized void setFailure(Throwable cause) {
		this.cause = cause;
		failed = true;
	}
	
	public synchronized Object getAttachment() {
		return attachment;
	}
	
	protected boolean docancel() {
		return true;
	}
}
