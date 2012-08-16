package naw.app.builtin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import naw.app.ActivityManager;
import naw.content.Context;
import naw.content.Intent;
import naw.os.ApplicationContext;
import naw.os.Looper;

import org.apache.axis.types.DateTime;
import org.apache.axis.types.Duration;
import org.jgroups.util.UUID;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class WaitTest {
	
	private static Context mContext;
	
	private static Looper mLooper;
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		mContext = new ApplicationContext();
		
		(new Thread() {
			
			@Override
			public void run() {
				Looper.prepare(mContext);
				mLooper = Looper.myLooper();
				
				Looper.loop();
			}
		}).start();
	}
	
	@AfterClass
	public static void afterClass() throws Exception {
		mLooper.quit();
	}
	
	@Test
	public void testDuration() throws Exception {
		ActivityManager am = ActivityManager.getInstance(mContext);
		
		CountDownLatch latch = new CountDownLatch(1);
		String requestId = UUID.randomUUID().toString();
		
		Wait begin = new Wait(mContext, "Wait#01");
		begin.setDuration(new Duration("PT3S"));
		
		EndNotify end = new EndNotify(mContext, latch);
		
		am.register(begin);
		am.register(end);
		
		am.select(begin).onFinish(end);
		
		Intent intent = new Intent();
		intent.setActivity(begin);
		intent.putExtra("requestId", requestId);
		
		mContext.startActivity(intent);
		
		assertFalse(latch.await(1, TimeUnit.SECONDS));
		assertTrue(latch.await(5, TimeUnit.SECONDS));
		
		assertNotNull(end.getIntent());
		assertEquals(1, end.getIntent().getExtras().size());
		assertEquals(requestId, end.getIntent().getStringExtra("requestId"));
	}
	
	@Test
	public void testDeadline() throws Exception {
		ActivityManager am = ActivityManager.getInstance(mContext);
		
		CountDownLatch latch = new CountDownLatch(1);
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.SECOND, 4);
		
		String requestId = UUID.randomUUID().toString();
		
		Wait begin = new Wait(mContext, "Wait#02");
		begin.setDeadline(new DateTime(c));
		
		EndNotify end = new EndNotify(mContext, latch);
		
		am.register(begin);
		am.register(end);
		
		am.select(begin).onFinish(end);
		
		Intent intent = new Intent();
		intent.setActivity(begin);
		intent.putExtra("requestId", requestId);
		
		mContext.startActivity(intent);
		
		assertFalse(latch.await(1, TimeUnit.SECONDS));
		assertTrue(latch.await(6, TimeUnit.SECONDS));
		
		assertNotNull(end.getIntent());
		assertEquals(1, end.getIntent().getExtras().size());
		assertEquals(requestId, end.getIntent().getStringExtra("requestId"));
	}
}
