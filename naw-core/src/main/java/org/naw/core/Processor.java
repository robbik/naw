package org.naw.core;

import java.util.concurrent.TimeUnit;

public interface Processor {
	
	Engine getEngine();

	void run() throws InterruptedException;

	void runOnce() throws InterruptedException;

	void runOnce(long timeout, TimeUnit unit) throws InterruptedException;
}
