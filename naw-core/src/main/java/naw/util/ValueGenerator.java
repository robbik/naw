package naw.util;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import rk.commons.util.StringHelper;

public abstract class ValueGenerator {
	
	private static final AtomicReference<String> PID_PREFIX; 
	
	private static final AtomicInteger PID_COUNTER;
	
	static {
		PID_PREFIX = new AtomicReference<String>(null);
		PID_COUNTER = new AtomicInteger(0);
		
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
		reloader.setName("ValueGenerator#Reloader");
		
		reloader.start();
	}
	
	private static void reload() {
		for (;;) {
			int oldv = PID_COUNTER.get();
			PID_PREFIX.set(StringHelper.valueOf(UUID.randomUUID()));
			
			if (PID_COUNTER.compareAndSet(oldv, 0)) {
				break;
			}
		}
	}

	public static String processId() {
		int counter = PID_COUNTER.incrementAndGet();
		String prefix = PID_PREFIX.get();
		
		return prefix.concat(String.valueOf(counter));
	}
}
