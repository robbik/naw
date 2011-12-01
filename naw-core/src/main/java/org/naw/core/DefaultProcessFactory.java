package org.naw.core;

import org.naw.core.exchange.Message;

public class DefaultProcessFactory implements ProcessFactory {

	public static final DefaultProcessFactory INSTANCE = new DefaultProcessFactory();

	public Process newProcess() {
		return new DefaultProcess();
	}

	public Process newProcess(Message msg) {
		return new DefaultProcess(msg);
	}
}
