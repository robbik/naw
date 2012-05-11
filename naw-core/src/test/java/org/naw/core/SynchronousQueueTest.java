package org.naw.core;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

public class SynchronousQueueTest {

	private SynchronousQueue<Object> queue;
	
	@Before
	public void before() {
		queue = new SynchronousQueue<Object>(true);
	}
	
	private Thread fork(Runnable runnable) {
		Thread t = new Thread(runnable);
		t.setDaemon(true);
		
		t.start();
		
		return t;
	}
	
	@Test
	public void peekTest() throws Exception {
		fork(new Runnable() {
			
			public void run() {
				for (;;) {
					System.out.println(queue.peek());
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		});
		
		System.out.println(queue.offer("zzz", 5, TimeUnit.SECONDS));
	}
	
	@Test
	public void doubleOfferTest() throws Exception {
		fork(new Runnable() {
			
			public void run() {
				try {
					System.out.println("F" + queue.offer("zzF", 5, TimeUnit.SECONDS));
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		});
		
		System.out.println(queue.offer("zzz", 5, TimeUnit.SECONDS));
	}
}
