package org.naw.exchange;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultMessage implements Message {

    private static final long serialVersionUID = 5747231707387987057L;

    private ConcurrentHashMap<String, Map<String, Object>> var;

    public DefaultMessage() {
        var = new ConcurrentHashMap<String, Map<String, Object>>();
    }

    public void declare(String variable) {
        var.putIfAbsent(variable, new ConcurrentHashMap<String, Object>());
    }

    public void undeclare(String variable) {
        Map<String, Object> map = var.remove(variable);

        if (map != null) {
            map.clear();
        }
    }

    public Set<String> getDeclaredVariables() {
        return var.keySet();
    }

    public void setValue(String variable, String name, Object value) {
        Map<String, Object> values = var.get(variable);
        if (values != null) {
            values.put(name, value);
        }
    }

    public Object getValue(String variable, String name) {
        Map<String, Object> values = var.get(variable);
        if (values == null) {
            return null;
        }

        return values.get(name);
    }

    public <T> T getValue(String variable, String name, Class<T> type) {
        Map<String, Object> values = var.get(variable);
        if (values == null) {
            return null;
        }

        Object value = values.get(name);
        if (value == null) {
            return null;
        }

        return type.cast(value);
    }

    public void set(String variable, Map<String, Object> values) {
        if (values != null) {
            var.put(variable, values);
        }
    }

    public Map<String, Object> get(String variable) {
        return var.get(variable);
    }

    @Override
    public String toString() {
        return super.toString() + " [vars=" + var + "]";
    }
}
