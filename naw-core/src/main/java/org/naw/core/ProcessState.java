package org.naw.core;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * represent process state
 */
public enum ProcessState implements Externalizable {
	/**
	 * process is just initialized / created
	 */
	INIT(0),

	/**
	 * process is going to execute an activity
	 */
	BEFORE(1),

	/**
	 * process executed an activity and ready to execute next activity (if any)
	 */
	AFTER(2),

	/**
	 * an error occurred while executing an activity
	 */
	ERROR(3),

	/**
	 * process is already terminated gracefully
	 */
	TERMINATED(4),

	/**
	 * process is in sleep mode, waiting for external trigger / data / alarm
	 */
	SLEEP(5),

	/**
	 * process is manually hibernated
	 */
	HIBERNATED(6);

	private transient int code;

	private ProcessState(int code) {
		this.code = code;
	}

	public int codeValue() {
		return code;
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		code = in.readInt();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(code);
	}

	public static ProcessState valueOf(int code) {
		switch (code) {
		case 0:
			return INIT;
		case 1:
			return BEFORE;
		case 2:
			return AFTER;
		case 3:
			return ERROR;
		case 4:
			return TERMINATED;
		case 5:
			return SLEEP;
		case 6:
			return HIBERNATED;
		default:
			return null;
		}
	}
}
