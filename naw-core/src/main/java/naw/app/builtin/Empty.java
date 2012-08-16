package naw.app.builtin;

import naw.app.Activity;
import naw.content.Context;
import naw.content.Intent;

public class Empty extends Activity {

	protected Empty(Context base, String id) {
		super(base, id);
	}

	@Override
	protected void onStart(Intent intent) {
		finish(intent.getProcessId());
	}
}
