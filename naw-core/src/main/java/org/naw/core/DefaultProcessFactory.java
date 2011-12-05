package org.naw.core;


public class DefaultProcessFactory implements ProcessFactory {

	public static final DefaultProcessFactory INSTANCE = new DefaultProcessFactory();

	public Process newProcess() {
		return new DefaultProcess();
	}
}
