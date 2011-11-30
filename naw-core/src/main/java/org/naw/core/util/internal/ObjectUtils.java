package org.naw.core.util.internal;

import org.naw.core.activity.Activity;

public abstract class ObjectUtils {

	public static boolean equals(Object a, Object b) {
		if (a == b) {
			return true;
		}

		if ((a == null) || (b == null)) {
			return false;
		}

		return a.equals(b);
	}

	public static boolean equals(Activity a, Activity b) {
		if (a == b) {
			return true;
		}

		if ((a == null) || (b == null)) {
			return false;
		}

		return a.getName().equalsIgnoreCase(b.getName());
	}
}
