package org.naw.core;

import org.naw.core.exchange.Message;

public interface ProcessFactory {

	Process newProcess(ProcessContext ctx, Message msg);
}
