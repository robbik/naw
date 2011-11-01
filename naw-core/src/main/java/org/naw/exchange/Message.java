package org.naw.exchange;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public interface Message extends Serializable {

    void declare(String variable);

    void undeclare(String variable);

    Set<String> getDeclaredVariables();

    void setValue(String variable, String name, Object value);

    Object getValue(String variable, String name);

    <T> T getValue(String variable, String name, Class<T> type);

    void set(String variable, Map<String, Object> values);

    Map<String, Object> get(String variable);
}
