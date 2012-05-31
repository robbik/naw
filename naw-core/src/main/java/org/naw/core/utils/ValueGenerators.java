package org.naw.core.utils;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import rk.commons.util.StringHelper;

public abstract class ValueGenerators {
	
	private static final AtomicReference<String> CORRELATION_PREFIX; 
	
	private static final AtomicInteger CORRELATION_COUNTER;
	
	private static final AtomicReference<String> EXCID_PREFIX; 
	
	private static final AtomicInteger EXCID_COUNTER;
	
	static {
		CORRELATION_PREFIX = new AtomicReference<String>(null);
		CORRELATION_COUNTER = new AtomicInteger(0);
		
		EXCID_PREFIX = new AtomicReference<String>(null);
		EXCID_COUNTER = new AtomicInteger(0);
		
		reload();
		
		Thread reloader = new Thread() {
			
			public void run() {
				for (;;) {
					reload();
					
					try {
						Thread.sleep(30000);
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		};
		
		reloader.setDaemon(true);
		reloader.setName("Generators#Reloader");
		
		reloader.start();
	}
	
	private static void reload() {
		for (;;) {
			int oldv = CORRELATION_COUNTER.get();
			CORRELATION_PREFIX.set(StringHelper.valueOf(UUID.randomUUID()));
			
			if (CORRELATION_COUNTER.compareAndSet(oldv, 0)) {
				break;
			}
		}
		
		for (;;) {
			int oldv = EXCID_COUNTER.get();
			EXCID_PREFIX.set(StringHelper.valueOf(UUID.randomUUID()));
			
			if (EXCID_COUNTER.compareAndSet(oldv, 0)) {
				break;
			}
		}
	}

	public static Object correlation() {
		int counter = CORRELATION_COUNTER.incrementAndGet();
		String prefix = CORRELATION_PREFIX.get();
		
		return prefix.concat(String.valueOf(counter));
	}

	public static String messageExchangeId() {
		int counter = EXCID_COUNTER.incrementAndGet();
		String prefix = EXCID_PREFIX.get();
		
		return prefix.concat(String.valueOf(counter));
	}
}
