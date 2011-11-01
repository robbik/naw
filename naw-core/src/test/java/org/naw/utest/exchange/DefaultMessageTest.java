package org.naw.utest.exchange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;
import org.naw.exchange.DefaultMessage;

public class DefaultMessageTest {

    @Test
    public void testDeclareIfVariableNotExists() {
        DefaultMessage msg = new DefaultMessage();
        msg.declare("abcd");

        assertEquals(Collections.singleton("abcd"), msg.getDeclaredVariables());
    }

    @Test
    public void testDeclareIfVariableExists() {
        DefaultMessage msg = new DefaultMessage();
        msg.declare("abcd");

        msg.setValue("abcd", "zz", "77");
        msg.declare("abcd");

        assertEquals(Collections.singleton("abcd"), msg.getDeclaredVariables());
        assertEquals("77", msg.getValue("abcd", "zz", String.class));
    }

    @Test
    public void testUndeclareIfVariableNotExists() {
        DefaultMessage msg = new DefaultMessage();
        msg.undeclare("data");

        assertEquals(0, msg.getDeclaredVariables().size());
    }

    @Test
    public void testUndeclareIfVariableExists() {
        DefaultMessage msg = new DefaultMessage();
        msg.declare("data");
        msg.undeclare("data");

        assertEquals(0, msg.getDeclaredVariables().size());
    }

    @Test
    public void testSetValueIfVariableNotExists() {
        DefaultMessage msg = new DefaultMessage();
        msg.setValue("data", "r", "12");

        assertEquals(0, msg.getDeclaredVariables().size());
    }

    @Test
    public void testSetValueIfVariableExists() {
        DefaultMessage msg = new DefaultMessage();
        msg.declare("data");
        msg.setValue("data", "r", "12");

        assertEquals(Collections.singleton("data"), msg.getDeclaredVariables());
        assertEquals("12", msg.getValue("data", "r", String.class));
    }

    @Test
    public void testGetValueIfVariableNotExists() {
        DefaultMessage msg = new DefaultMessage();
        assertNull(msg.getValue("data", "r"));

        assertEquals(0, msg.getDeclaredVariables().size());
    }

    @Test
    public void testSetValueIfVariableExistsButNameNotExists() {
        DefaultMessage msg = new DefaultMessage();
        msg.declare("data");
        assertNull(msg.getValue("data", "r"));

        assertEquals(Collections.singleton("data"), msg.getDeclaredVariables());
    }

    @Test
    public void testSetValueIfVariableAndNameExists() {
        DefaultMessage msg = new DefaultMessage();
        msg.declare("data");

        msg.setValue("data", "r", "12");
        assertEquals("12", msg.getValue("data", "r"));

        assertEquals(Collections.singleton("data"), msg.getDeclaredVariables());
    }

    @Test
    public void testSetIfVariableNotExists() {
        Map<String, Object> values = Collections.singletonMap("r", (Object) "27");

        DefaultMessage msg = new DefaultMessage();
        msg.set("abcd", values);

        assertEquals(Collections.singleton("abcd"), msg.getDeclaredVariables());
        assertEquals(values, msg.get("abcd"));
    }

    @Test
    public void testSetIfVariableExists() {
        Map<String, Object> values = Collections.singletonMap("r", (Object) "27");

        DefaultMessage msg = new DefaultMessage();
        msg.declare("abcd");
        msg.set("abcd", values);

        assertEquals(Collections.singleton("abcd"), msg.getDeclaredVariables());
        assertEquals(values, msg.get("abcd"));
    }

    @Test
    public void testGetIfVariableNotExists() {
        DefaultMessage msg = new DefaultMessage();
        assertNull(msg.get("abcd"));

        assertEquals(Collections.emptySet(), msg.getDeclaredVariables());
    }

    @Test
    public void testGetIfVariableExists() {
        Map<String, Object> values = Collections.singletonMap("r", (Object) "27");

        DefaultMessage msg = new DefaultMessage();
        msg.set("abcd", values);

        assertEquals(values, msg.get("abcd"));
        assertEquals(Collections.singleton("abcd"), msg.getDeclaredVariables());
    }
}
