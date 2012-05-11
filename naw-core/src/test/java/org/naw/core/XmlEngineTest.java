package org.naw.core;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

import rk.commons.util.ObjectUtils;

public class XmlEngineTest {

	@Test
	public void refreshTest() throws Exception {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("/tmp/x.txt"));
		ObjectUtils.writeObject("abcdef", out);
		out.close();
		
		final XmlEngine engine = new XmlEngine("classpath:naw1.xml");
		
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
		
		Thread.sleep(1000);
		
		ObjectInputStream in = new ObjectInputStream(new FileInputStream("/tmp/x.txt"));
		assertEquals("hehehe", ObjectUtils.readObject(in));
		in.close();
		
		in = new ObjectInputStream(new FileInputStream("/tmp/x2.txt"));
		assertEquals("hehehe", ObjectUtils.readObject(in));
		in.close();
	}
}
