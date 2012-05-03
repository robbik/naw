package org.naw.core.task;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.naw.core.Engine;
import org.naw.executables.Executable;

public class MockTaskContext implements TaskContext {

	private Engine engine;

	private Executable executable;

	private Task task;

	private Semaphore semExecute;

	private Semaphore semRun;

	private Semaphore semRepeat;

	private volatile boolean expExecute;

	private volatile boolean expRun;

	private volatile boolean expRepeat;

	private volatile DataExchange deExecute;

	private volatile DataExchange deRun;

	private volatile DataExchange deRepeat;

	public MockTaskContext() {
		semExecute = new Semaphore(0, true);
		semRun = new Semaphore(0, true);
		semRepeat = new Semaphore(0, true);

		expExecute = false;
		expRun = false;
		expRepeat = false;

		deExecute = null;
		deRun = null;
		deRepeat = null;
	}

	public void setEngine(Engine engine) {
		this.engine = engine;
	}

	public Engine getEngine() {
		return engine;
	}

	public void setExecutable(Executable executable) {
		this.executable = executable;
	}

	public Executable getExecutable() {
		return executable;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public Task getTask() {
		return task;
	}
	
	public TaskContext getNextTaskContext() {
		return null;
	}

	public void next(DataExchange exchange) {
		semExecute.tryAcquire();

		deExecute = exchange;

		semExecute.release();
	}

	public void start(DataExchange exchange) {
		semRepeat.tryAcquire();

		deRepeat = exchange;

		semRepeat.release();
	}

	public void run(DataExchange exchange) {
		semRun.tryAcquire();

		deRun = exchange;

		semRun.release();
	}

	public void reset() {
		semExecute.tryAcquire();
		semExecute.release();

		semRepeat.tryAcquire();
		semRepeat.release();

		semRun.tryAcquire();
		semRun.release();

		expExecute = false;
		expRun = false;
		expRepeat = false;

		deExecute = null;
		deRun = null;
		deRepeat = null;
	}

	public void expectExecute() {
		expExecute = true;
		semExecute.tryAcquire();
	}

	public void expectRun() {
		expRun = true;
		semRun.tryAcquire();
	}

	public void expectRepeat() {
		expRepeat = true;
		semRepeat.tryAcquire();
	}

	public void assertExpected(long timeout, TimeUnit unit) {
		try {
			if (expExecute && !semExecute.tryAcquire(timeout, unit)) {
				throw new AssertionError();
			}

			if (expRun && !semRun.tryAcquire(timeout, unit)) {
				throw new AssertionError();
			}

			if (expRepeat && !semRepeat.tryAcquire(timeout, unit)) {
				throw new AssertionError();
			}
		} catch (InterruptedException e) {
			throw new AssertionError(e);
		}
	}

	public DataExchange getExecuteDataExchange() {
		return deExecute;
	}

	public DataExchange getRunDataExchange() {
		return deRun;
	}

	public DataExchange getRepeatDataExchange() {
		return deRepeat;
	}

	public TaskContextFuture nextLater(DataExchange exchange, long delay,
			TimeUnit unit) {
		throw new UnsupportedOperationException();
	}

	public TaskContextFuture nextLater(DataExchange exchange, long deadline) {
		throw new UnsupportedOperationException();
	}

	public TaskContextFuture startLater(DataExchange exchange, long delay,
			TimeUnit unit) {
		throw new UnsupportedOperationException();
	}

	public TaskContextFuture startLater(DataExchange exchange, long deadline) {
		throw new UnsupportedOperationException();
	}
}
