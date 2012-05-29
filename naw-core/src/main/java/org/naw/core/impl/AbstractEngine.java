package org.naw.core.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.axis.types.DateTime;
import org.apache.axis.types.Duration;
import org.naw.core.Engine;
import org.naw.core.Processor;
import org.naw.core.task.TaskQueue;
import org.naw.core.task.impl.DefaultTaskQueue;
import org.naw.core.task.support.HashedWheelTimer;
import org.naw.core.task.support.Timer;
import org.naw.executables.Executable;
import org.naw.links.Link;
import org.naw.links.factory.StringToLinkConverter;
import org.naw.tasks.support.StringToDateTimeConverter;
import org.naw.tasks.support.StringToDurationConverter;

import rk.commons.inject.factory.AbstractObjectFactory;
import rk.commons.inject.factory.SingletonObjectFactory;
import rk.commons.inject.factory.type.converter.TypeConverterResolver;
import rk.commons.loader.ResourceLoader;

public abstract class AbstractEngine implements Engine {

	protected static final int STATUS_NONE = 0;

	protected static final int STATUS_STARTED = 1;

	protected static final int STATUS_STOPPED = 2;

	protected static final int STATUS_DESTROYED = 3;

	protected final Object statusLock;

	protected int status;
	
	protected Timer timer;

	protected TaskQueue taskQueue;
	
	protected ResourceLoader resourceLoader;

	protected AbstractObjectFactory objectFactory;

	protected Thread shutdownHook;

	protected AbstractEngine() {
		statusLock = new Object();
		status = STATUS_NONE;
	}
	
	protected void initialize() {
		timer = new HashedWheelTimer();
		
		taskQueue = new DefaultTaskQueue();
		
		resourceLoader = new ResourceLoader();

		objectFactory = createObjectFactory();
		
		TypeConverterResolver typeResolver = objectFactory.getTypeConverterResolver();
		typeResolver.register(String.class, DateTime.class, new StringToDateTimeConverter());
		typeResolver.register(String.class, Duration.class, new StringToDurationConverter());
		typeResolver.register(String.class, Link.class, new StringToLinkConverter(objectFactory));

		shutdownHook = new Thread() {

			@Override
			public void run() {
				objectFactory.destroy();
			}
		};

		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}
	
	protected AbstractObjectFactory createObjectFactory() {
		return new SingletonObjectFactory(resourceLoader);
	}
	
	protected String[] addDefaultImport(String[] locations) {
		boolean defImportsFound = false;

		for (int i = 0, n = locations.length; i < n; ++i) {
			if ("classpath:META-INF/naw/naw-default.xml".equals(locations[i])) {
				defImportsFound = true;
				break;
			}
		}
		
		if (!defImportsFound) {
			List<String> newLocations = new ArrayList<String>();
			
			newLocations.add("classpath:META-INF/naw/naw-default.xml");
			
			for (int i = 0, n = locations.length; i < n; ++i) {
				newLocations.add(locations[i]);
			}
			
			locations = newLocations.toArray(new String[0]);
			newLocations.clear();
		}
		
		return locations;
	}
	
	public Timer getTimer() {
		return timer;
	}

	public TaskQueue getTaskQueue() {
		return taskQueue;
	}

	public void setTaskQueue(TaskQueue taskQueue) {
		this.taskQueue = taskQueue;
	}
	
	public Set<String> getObjectQNames() {
		return objectFactory.getObjectQNames();
	}

	public Processor createProcessor() {
		return new DefaultProcessor(this);
	}

	public void start() {
		synchronized (statusLock) {
			if (status == STATUS_STARTED) {
				return;
			}

			Collection<Executable> executables = objectFactory.getObjectsOfType(Executable.class).values();

			for (Executable e : executables) {
				e.start(this);
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
				objectFactory.destroy();
				break;
			case STATUS_STARTED:
				objectFactory.destroy();
				break;
			case STATUS_STOPPED:
				objectFactory.destroy();
				break;
			}
			
			Runtime.getRuntime().removeShutdownHook(shutdownHook);

			status = STATUS_DESTROYED;
		}
	}
}
