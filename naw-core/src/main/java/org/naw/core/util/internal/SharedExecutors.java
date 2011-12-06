package org.naw.core.util.internal;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.naw.core.util.DirectExecutor;

public abstract class SharedExecutors {

	public static final Executor DIRECT = new DirectExecutor();

	public static final Executor CACHED = Executors.newCachedThreadPool();
}
