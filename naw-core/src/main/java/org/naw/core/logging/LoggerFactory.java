package org.naw.core.logging;

public abstract class LoggerFactory {

	private static final String[] loggers = {
			"org.naw.core.logging.Slf4jLogger",
			"org.naw.core.logging.CommonsLogger",
			"org.naw.core.logging.JdkLogger" };

	public static Logger getLogger(Class<?> clazz) {
		for (int i = 0, length = loggers.length; i < length; ++i) {
			try {
				return (Logger) Class.forName(loggers[i])
						.getConstructor(Class.class).newInstance(clazz);
			} catch (Throwable t) {
				// do nothing
			}
		}

		return NoopLogger.INSTANCE;
	}

	public static Logger getLogger(String name) {
		for (int i = 0, length = loggers.length; i < length; ++i) {
			try {
				return (Logger) Class.forName(loggers[i])
						.getConstructor(String.class).newInstance(name);
			} catch (Throwable t) {
				// do nothing
			}
		}

		return NoopLogger.INSTANCE;
	}
}
