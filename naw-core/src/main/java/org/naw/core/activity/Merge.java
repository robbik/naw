package org.naw.core.activity;

import java.util.Map;

import org.naw.core.Process;
import org.naw.core.exchange.Message;

/**
 * MERGE
 */
public class Merge extends AbstractActivity {

	private String fromVariable;

	private String toVariable;

	public Merge(String name) {
		super(name);
	}

	public void setFromVariable(String fromVariable) {
		this.fromVariable = fromVariable;
	}

	public void setToVariable(String toVariable) {
		this.toVariable = toVariable;
	}

	public void execute(Process process) throws Exception {
		Message message = process.getMessage();
		
		Map<String, Object> fromMap = message.get(fromVariable);
		Map<String, Object> toMap = message.get(toVariable);
		
		if (fromMap != null) {
			if (toMap == null) {
				message.set(toVariable, fromMap);
			} else {
				toMap.putAll(fromMap);
			}
		}

		ctx.execute(process);
	}
}
