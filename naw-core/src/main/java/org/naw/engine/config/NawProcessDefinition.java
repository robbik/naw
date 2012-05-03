package org.naw.engine.config;

import org.naw.activities.Activity;

public interface NawProcessDefinition {
	
	String getName();

	Activity getFirstActivity();
}
