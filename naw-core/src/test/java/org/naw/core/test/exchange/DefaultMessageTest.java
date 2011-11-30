package org.naw.core.test.exchange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;
import org.naw.core.exchange.DefaultMessage;

public class DefaultMessageTest {

	@Test
	public void testRemoveIfVariableNotExists() {
		DefaultMessage msg = new DefaultMessage();
		msg.remove("data");

		assertEquals(0, msg.getVariables().size());
	}

	@Test
	public void testRemoveIfVariableExists() {
		DefaultMessage msg = new DefaultMessage();
		msg.set("data", Collections.<String, Object> emptyMap());
		msg.remove("data");

		assertEquals(0, msg.getVariables().size());
	}

	@Test
	public void testSetIfVariableNotExists() {
		Map<String, Object> values = Collections.singletonMap("r",
				(Object) "27");

		DefaultMessage msg = new DefaultMessage();
		msg.set("abcd", values);

		assertEquals(Collections.singleton("abcd"), msg.getVariables());
		assertEquals(values, msg.get("abcd"));
	}

	@Test
	public void testSetIfVariableExists() {
		Map<String, Object> values = Collections.singletonMap("r",
				(Object) "27");

		DefaultMessage msg = new DefaultMessage();
		msg.set("abcd", Collections.<String, Object> emptyMap());
		msg.set("abcd", values);

		assertEquals(Collections.singleton("abcd"), msg.getVariables());
		assertEquals(values, msg.get("abcd"));
	}

	@Test
	public void testGetIfVariableNotExists() {
		DefaultMessage msg = new DefaultMessage();
		assertNull(msg.get("abcd"));

		assertEquals(Collections.emptySet(), msg.getVariables());
	}

	@Test
	public void testGetIfVariableExists() {
		Map<String, Object> values = Collections.singletonMap("r",
				(Object) "27");

		DefaultMessage msg = new DefaultMessage();
		msg.set("abcd", values);

		assertEquals(values, msg.get("abcd"));
		assertEquals(Collections.singleton("abcd"), msg.getVariables());
	}
}
