package org.naw.core;

import java.util.concurrent.TimeUnit;

public interface Processor {
	
	Engine getEngine();

	void run() throws InterruptedException;

	boolean next() throws InterruptedException;

	boolean next(long timeout, TimeUnit unit) throws InterruptedException;
}
