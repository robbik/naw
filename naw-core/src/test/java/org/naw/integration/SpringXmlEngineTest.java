package org.naw.integration;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;
import org.naw.core.Processor;
import org.springframework.context.support.GenericXmlApplicationContext;

import rk.commons.util.ObjectUtils;

public class SpringXmlEngineTest {

	@Test
	public void case1() throws Exception {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("/tmp/x-spring.txt"));
		ObjectUtils.writeBytes("abcdef", out);
		out.close();
		
		GenericXmlApplicationContext spring = new GenericXmlApplicationContext("classpath:naw2-spring.xml");
		
		final SpringXmlEngine engine = new SpringXmlEngine(spring, "classpath:naw2.xml");
		
		engine.start();
		
		Thread th = new Thread() {

			public void run() {
				Processor processor = engine.createProcessor();
				
				try {
					processor.run();
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		};
		
		th.setDaemon(true);
		
		th.start();
		
		Thread.sleep(1000);
		
		th.interrupt();
	}
}
