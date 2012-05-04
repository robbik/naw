package org.naw.core;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

import rk.commons.util.ObjectUtils;

public class XmlEngineTest {

	@Test
	public void refreshTest() throws Exception {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("/tmp/x.txt"));
		ObjectUtils.writeBytes("abcdef", out);
		out.close();
		
		final XmlEngine engine = new XmlEngine("classpath:naw1.xml");
		
		// System.out.println(Arrays.toString(engine.getObjectQNames()));
		
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
