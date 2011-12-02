package org.naw.core;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public enum ProcessState implements Externalizable {
    INIT(0),

    BEFORE(1),

    AFTER(2),

    ERROR(3),

    TERMINATED(4),

    SLEEP(5),

    ON(6);

    private transient int code;

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
            return ON;
        default:
            return null;
        }
    }
}
