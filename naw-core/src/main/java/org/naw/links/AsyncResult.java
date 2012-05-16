package org.naw.links;

public interface AsyncResult<T> {
	
	boolean isDone();
	
	boolean isSuccess();
	
	boolean isCancelled();
	
	boolean isTimeout();
	
	boolean cancel();
	
	T getResult();
	
	Object getAttachment();
	
	Throwable getCause();
	
	void setSuccess(T result);
	
	void setFailure(Throwable cause);
	
	boolean timeout();
}
