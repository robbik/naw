package org.naw.engine.test;

import org.naw.activities.support.AbstractActivity;
import org.naw.engine.ProcessInstance;

public class MockActivity extends AbstractActivity {

	public MockActivity(String name) {
		super(name);
	}

	public void execute(ProcessInstance process) throws Exception {
		// do nothing
	}
}
