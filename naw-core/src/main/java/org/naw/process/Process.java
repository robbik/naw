package org.naw.process;

import org.naw.exchange.Message;
import org.naw.process.activity.Activity;
import org.naw.util.Timeout;

public interface Process {

    String getProcessId();

    ProcessContext getProcessContext();

    void setAttribute(String name, Object value);

    Object getAttribute(String name);

    <T> T getAttribute(String name, Class<T> type);

    Object removeAttribute(String name);

    <T> T removeAttribute(String name, Class<T> type);

    Message getMessage();

    void addAlarm(Timeout timeout);

    void removeAlarm(Timeout timeout);

    void removeAlarmForActivity(String activityName);

    void setState(ProcessState newState, Activity newActivity);

    boolean compareAndSet(ProcessState state, Activity activity, ProcessState newState, Activity newActivity);

    ProcessState getState();

    Activity getActivity();

    void terminate();

    void destroy();
}
