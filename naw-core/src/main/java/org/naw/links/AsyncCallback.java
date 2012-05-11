package org.naw.links;

public interface AsyncCallback<T> {

	void completed(AsyncResult<T> asyncResult);
	
	void timeout(AsyncResult<T> asyncResult);
}
