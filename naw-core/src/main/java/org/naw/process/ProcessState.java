package org.naw.process;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public enum ProcessState implements Externalizable {
    INIT(-1),

    BEFORE_ACTIVITY(0),

    AFTER_ACTIVITY(1),

    ERROR(2),

    TERMINATED(3);

    private int code;

    private ProcessState(int code) {
        this.code = code;
    }

    public int codeValue() {
        return code;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        code = in.readInt();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(code);
    }

    public static ProcessState valueOf(int code) {
        switch (code) {
        case -1:
            return INIT;
        case 0:
            return BEFORE_ACTIVITY;
        case 1:
            return AFTER_ACTIVITY;
        case 2:
            return ERROR;
        case 3:
            return TERMINATED;
        default:
            return null;
        }
    }
}
