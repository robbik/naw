package org.naw.core;

import java.util.List;

import org.apache.axis.types.DateTime;
import org.apache.axis.types.Duration;
import org.naw.core.task.SimpleTaskQueue;
import org.naw.core.task.TaskQueue;
import org.naw.executables.Executable;
import org.naw.links.Link;
import org.naw.links.factory.StringToLinkConverter;
import org.naw.tasks.support.StringToDateTimeConverter;
import org.naw.tasks.support.StringToDurationConverter;

import rk.commons.ioc.factory.SingletonIocObjectFactory;
import rk.commons.ioc.factory.type.converter.TypeConverterResolver;
import rk.commons.loader.ResourceLoader;

public abstract class AbstractEngine implements Engine {

	protected static final int STATUS_NONE = 0;

	protected static final int STATUS_STARTED = 1;

	protected static final int STATUS_STOPPED = 2;

	protected static final int STATUS_DESTROYED = 3;

	protected final Object statusLock;

	protected int status;

	protected TaskQueue taskQueue;
	
	protected ResourceLoader resourceLoader;

	protected SingletonIocObjectFactory iocFactory;

	protected Thread shutdownHook;

	protected AbstractEngine() {
		statusLock = new Object();
		status = STATUS_NONE;

		taskQueue = new SimpleTaskQueue();
		
		resourceLoader = new ResourceLoader();

		iocFactory = new SingletonIocObjectFactory(resourceLoader);
		
		TypeConverterResolver typeResolver = iocFactory.getTypeConverterResolver();
		typeResolver.register(String.class, DateTime.class, new StringToDateTimeConverter());
		typeResolver.register(String.class, Duration.class, new StringToDurationConverter());
		typeResolver.register(String.class, Link.class, new StringToLinkConverter(iocFactory));

		shutdownHook = new Thread() {

			@Override
			public void run() {
				iocFactory.destroy();
			}
		};

		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}

	public TaskQueue getTaskQueue() {
		return taskQueue;
	}

	public void setTaskQueue(TaskQueue taskQueue) {
		this.taskQueue = taskQueue;
	}
	
	public String[] getObjectQNames() {
		return iocFactory.getObjectQNames();
	}

	public Processor createProcessor() {
		return new SimpleProcessor(this);
	}

	public void start() {
		synchronized (statusLock) {
			if (status == STATUS_STARTED) {
				return;
			}

			List<Executable> executables = iocFactory.getObjectsOfType(Executable.class);

			for (int i = 0, n = executables.size(); i < n; ++i) {
				Executable e = executables.get(i);
				e.attach(this);

				taskQueue.add(e.getEntryPoint(), null);
			}

			status = STATUS_STARTED;
		}
	}

	public void stop() {
		synchronized (statusLock) {
			status = STATUS_STOPPED;
		}
	}

	public boolean stopped() {
		synchronized (statusLock) {
			return status != STATUS_STARTED;
		}
	}

	public void destroy() {
		synchronized (statusLock) {
			switch (status) {
			case STATUS_NONE:
				iocFactory.destroy();
				break;
			case STATUS_STARTED:
				iocFactory.destroy();
				break;
			case STATUS_STOPPED:
				iocFactory.destroy();
				break;
			}
			
			Runtime.getRuntime().removeShutdownHook(shutdownHook);

			status = STATUS_DESTROYED;
		}
	}
}