package org.naw.core.task;

public interface TaskFuture {
	
	void beforeRun();
	
	void cancel();

	void setSuccess();
	
	void setFailure(Throwable cause);
}
