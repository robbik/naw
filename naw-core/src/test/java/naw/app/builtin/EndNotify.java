package naw.app.builtin;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import naw.app.Activity;
import naw.content.Context;
import naw.content.Intent;

public class EndNotify extends Activity {
	
	private final CountDownLatch latch;
	
	private volatile Intent intent;

	protected EndNotify(Context base,  CountDownLatch latch) {
		super(base, "EndNotify#" + UUID.randomUUID().toString());
		
		this.latch = latch;
	}

	protected void onStart(Intent intent) {
		this.intent = intent;
		
		latch.countDown();
	}
	
	public Intent getIntent() {
		return intent;
	}
}
