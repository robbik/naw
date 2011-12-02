package org.naw.core.test;

import org.naw.core.Process;
import org.naw.core.activity.AbstractActivity;

public class MockActivity extends AbstractActivity {

	public MockActivity(String name) {
		super(name);
	}

	public void execute(Process process) throws Exception {
		// do nothing
	}
}
