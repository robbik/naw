package org.naw.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.naw.exceptions.LinkException;
import org.naw.integration.SpringXmlEngine;
import org.naw.links.AsyncCallback;
import org.naw.links.AsyncResult;
import org.naw.links.Link;
import org.naw.links.Message;
import org.naw.links.factory.LinkFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

public class DirectLinkTest {
	
	private ApplicationContext spring;
	
	private SpringXmlEngine engine;
	
	private LinkFactory directLink;
	
	private static AsyncResult<Message> asyncReceive(Link link, Object correlation, long deadline, final CountDownLatch latch) throws Exception {
		return link.asyncReceive(correlation, null, deadline, new AsyncCallback<Message>() {
			
			public void timeout(AsyncResult<Message> ar) {
				System.out.println("timeout");
				
				if (latch != null) {
					latch.countDown();
				}
			}
			
			public void completed(AsyncResult<Message> ar) {
				System.out.println("completed: " + ar.isSuccess() + ", " + ar.getResult());
				
				if (latch != null) {
					latch.countDown();
				}
			}
		});
	}
	
	private static AsyncResult<Message> asyncReceiveReply(Link link, Object correlation, long deadline, final CountDownLatch latch) throws Exception {
		return link.asyncReceiveReply(correlation, null, deadline, new AsyncCallback<Message>() {
			
			public void timeout(AsyncResult<Message> ar) {
				System.out.println("timeout");
				
				if (latch != null) {
					latch.countDown();
				}
			}
			
			public void completed(AsyncResult<Message> ar) {
				System.out.println("completed: " + ar.isSuccess() + ", " + ar.getResult());
				
				if (latch != null) {
					latch.countDown();
				}
			}
		});
	}

	@Before
	public void before() throws Exception {
		spring = new GenericXmlApplicationContext("classpath:direct-link-spring.xml");
		
		directLink = spring.getBean("direct-test", LinkFactory.class);
		
		engine = new SpringXmlEngine(spring, "classpath:direct-link.xml");
		
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
	}

	@Test
	public void timeoutTest() throws Exception {
		Link link = directLink.createLink("T1");
		
		CountDownLatch latch = new CountDownLatch(1);
		
		AsyncResult<Message> ar = asyncReceive(link, "12345678", System.currentTimeMillis() + 2000, latch);
		
		assertTrue(latch.await(4, TimeUnit.SECONDS));
		assertFalse(ar.isSuccess());
	}

	@Test(expected = IllegalArgumentException.class)
	public void doubleReceiveTest() throws Exception {
		Link link = directLink.createLink("T2");
		
		asyncReceive(link, "12345678", System.currentTimeMillis() + 2000, null);
		
		asyncReceive(link, "12345678", System.currentTimeMillis() + 2000, null);
	}

	@Test(expected = LinkException.class)
	public void receiveAndDoubleSendTest() throws Exception {
		Link link = directLink.createLink("T3");
		
		CountDownLatch latch = new CountDownLatch(2);
		
		asyncReceive(link, "12345678", -1, latch);
		
		link.send(new Message("12345678", "abcdef"));
		link.send(new Message("12345678", "abcdefg"));
		
		assertFalse(latch.await(4, TimeUnit.SECONDS));
		
		assertEquals(1, latch.getCount());
	}

	@Test(expected = LinkException.class)
	public void sendNoReceiveTest() throws Exception {
		directLink.createLink("T4").send(new Message("12345678", "abcdef"));
	}

	@Test
	public void receiveAndSendTest() throws Exception {
		Link link = directLink.createLink("T5");
		
		CountDownLatch latch = new CountDownLatch(1);
		
		AsyncResult<Message> ar = asyncReceive(link, "12345678", -1, latch);
		
		link.send(new Message("12345678", "abcdef"));
		
		assertTrue(latch.await(5, TimeUnit.SECONDS));
		
		assertTrue(ar.isSuccess());
		assertEquals("12345678", ar.getResult().getCorrelation());
		assertEquals("abcdef", ar.getResult().getBody());
	}

	@Test(expected = LinkException.class)
	public void sendAndReceiveTest() throws Exception {
		Link link = directLink.createLink("T6");
		
		CountDownLatch latch = new CountDownLatch(1);
		
		link.send(new Message("12345678", "abcdef"));
		
		AsyncResult<Message> ar = asyncReceive(link, "12345678", -1, latch);
		
		assertTrue(latch.await(5, TimeUnit.SECONDS));
		
		assertTrue(ar.isSuccess());
		assertEquals("12345678", ar.getResult().getCorrelation());
		assertEquals("abcdef", ar.getResult().getBody());
	}

	@Test
	public void inProcessSingleInvokeTest() throws Exception {
		Link link = directLink.createLink("A");
		
		CountDownLatch latch = new CountDownLatch(1);
		
		link.send(new Message("12345678", "abcdef"));
		
		AsyncResult<Message> ar = asyncReceiveReply(link, "12345678", -1, latch);
		
		assertTrue(latch.await(5, TimeUnit.SECONDS));
		
		assertTrue(ar.isSuccess());
		assertEquals("12345678", ar.getResult().getCorrelation());
		assertEquals("abcdef_ARESPONSE", ar.getResult().getBody());
	}

	@Test
	public void inProcessDoubleInvokeTest() throws Exception {
		Link link = directLink.createLink("A");
		
		CountDownLatch latch = new CountDownLatch(1);
		
		link.send(new Message("12345678_1", "abcdef_1"));
		
		link.send(new Message("12345678_2", "abcdef_2"));
		
		AsyncResult<Message> ar = asyncReceiveReply(link, "12345678_1", -1, latch);
		
		assertTrue(latch.await(5, TimeUnit.SECONDS));
		
		assertTrue(ar.isSuccess());
		assertEquals("12345678_1", ar.getResult().getCorrelation());
		assertEquals("abcdef_1_ARESPONSE", ar.getResult().getBody());
		
		latch = new CountDownLatch(1);

		ar = asyncReceiveReply(link, "12345678_2", -1, latch);
		
		assertTrue(latch.await(5, TimeUnit.SECONDS));
		
		assertTrue(ar.isSuccess());
		assertEquals("12345678_2", ar.getResult().getCorrelation());
		assertEquals("abcdef_2_ARESPONSE", ar.getResult().getBody());
	}

	@Test
	public void inProcess1DeepInvokeTest() throws Exception {
		Link link = directLink.createLink("B");
		
		CountDownLatch latch = new CountDownLatch(1);
		
		link.send(new Message("12345678", "abcdef"));
		
		AsyncResult<Message> ar = asyncReceiveReply(link, "12345678", -1, latch);
		
		assertTrue(latch.await(7, TimeUnit.SECONDS));
		
		assertTrue(ar.isSuccess());
		assertEquals("12345678", ar.getResult().getCorrelation());
		assertEquals("abcdef_AREQUEST_ARESPONSE", ar.getResult().getBody());
	}
	@Test
	
	public void inProcess2DeepInvokeTest() throws Exception {
		Link link = directLink.createLink("C");
		
		CountDownLatch latch = new CountDownLatch(1);
		
		link.send(new Message("12345678", "abcdef"));
		
		AsyncResult<Message> ar = asyncReceiveReply(link, "12345678", -1, latch);
		
		assertTrue(latch.await(7, TimeUnit.SECONDS));
		
		assertTrue(ar.isSuccess());
		assertEquals("12345678", ar.getResult().getCorrelation());
		assertEquals("abcdef_BREQUEST_AREQUEST_ARESPONSE", ar.getResult().getBody());
	}
}
