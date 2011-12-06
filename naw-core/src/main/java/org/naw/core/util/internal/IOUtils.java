package org.naw.core.util.internal;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class IOUtils {

	public static boolean tryClose(InputStream in) {
		if (in != null) {
			try {
				in.close();
				return true;
			} catch (Throwable t) {
				// do nothing
			}
		}

		return false;
	}

	public static boolean tryClose(OutputStream out) {
		if (out != null) {
			try {
				out.close();
				return true;
			} catch (Throwable t) {
				// do nothing
			}
		}

		return false;
	}
}
