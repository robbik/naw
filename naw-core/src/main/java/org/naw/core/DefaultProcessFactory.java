package org.naw.core;

import org.naw.core.exchange.Message;

public class DefaultProcessFactory implements ProcessFactory {
	
	public static final DefaultProcessFactory INSTANCE = new DefaultProcessFactory();

	public Process newProcess(ProcessContext ctx) {
		return new DefaultProcess((DefaultProcessContext) ctx);
	}

	public Process newProcess(ProcessContext ctx, Message msg) {
		return new DefaultProcess((DefaultProcessContext) ctx, msg);
	}
}
