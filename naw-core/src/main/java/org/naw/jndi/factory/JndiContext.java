package org.naw.jndi.factory;

import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import rk.commons.inject.factory.support.InitializingObject;

public class JndiContext implements InitializingObject {
	
	private Map<String, Object> environment;
	
	private InitialContext context;
	
	public void setEnvironment(Map<String, Object> environment) {
		this.environment = environment;
	}

	public void initialize() throws Exception {
		context = new InitialContext();
		
		if (environment != null) {
			for (Map.Entry<String, Object> entry : environment.entrySet()) {
				context.addToEnvironment(entry.getKey(), entry.getValue());
			}
		}
	}
	
	public Object lookup(String jndiName) throws NamingException {
		return context.lookup(jndiName);
	}
}
