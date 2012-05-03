package org.naw.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.naw.core.task.DataExchange;
import org.naw.core.task.MockTaskContext;

public class MergeTest {

	@Test
	public void testRunIfBothVariablesAreUnset() throws Exception {
		Merge task = new Merge();
		task.setFrom("a");
		task.setTo("b");

		MockTaskContext mocktc = new MockTaskContext();

		mocktc.expectExecute();
		
		task.run(mocktc, new DataExchange());
		
		mocktc.assertExpected(10, TimeUnit.SECONDS);
		
		assertNull(mocktc.getExecuteDataExchange().get("b"));
		assertNull(mocktc.getExecuteDataExchange().get("a"));
	}

	@Test
	public void testRunIfFromVariableIsUnset() throws Exception {
		Merge task = new Merge();
		task.setFrom("a");
		task.setTo("b");

		DataExchange exchange = new DataExchange();
		exchange.set("b", "zz");

		MockTaskContext mocktc = new MockTaskContext();

		mocktc.expectExecute();
		
		task.run(mocktc, exchange);
		
		mocktc.assertExpected(10, TimeUnit.SECONDS);
		
		assertEquals("zz", mocktc.getExecuteDataExchange().get("b"));
		assertNull(mocktc.getExecuteDataExchange().get("a"));
	}

	@Test
	public void testRunIfToVariableIsUnset() throws Exception {
		Merge task = new Merge();
		task.setFrom("a");
		task.setTo("b");

		DataExchange exchange = new DataExchange();
		exchange.set("a", "zz");

		MockTaskContext mocktc = new MockTaskContext();

		mocktc.expectExecute();
		
		task.run(mocktc, exchange);
		
		mocktc.assertExpected(10, TimeUnit.SECONDS);
		
		assertEquals("zz", mocktc.getExecuteDataExchange().get("b"));
		assertEquals("zz", mocktc.getExecuteDataExchange().get("a"));
	}

	@Test
	public void testRunIfBothVariablesAreSet() throws Exception {
		Merge task = new Merge();
		task.setFrom("a");
		task.setTo("b");

		DataExchange exchange = new DataExchange();
		exchange.set("a", "zz");
		exchange.set("b", "xx");

		MockTaskContext mocktc = new MockTaskContext();

		mocktc.expectExecute();
		
		task.run(mocktc, exchange);
		
		mocktc.assertExpected(10, TimeUnit.SECONDS);
		
		assertEquals("zz", mocktc.getExecuteDataExchange().get("b"));
		assertEquals("zz", mocktc.getExecuteDataExchange().get("a"));
	}
}
