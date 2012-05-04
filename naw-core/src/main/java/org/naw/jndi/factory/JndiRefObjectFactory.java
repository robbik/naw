package org.naw.jndi.factory;

import javax.naming.NamingException;

import rk.commons.ioc.factory.support.InitializingObject;
import rk.commons.ioc.factory.support.ObjectFactory;

public class JndiRefObjectFactory extends ObjectFactory<Object> implements InitializingObject {

	private JndiContext jndiContext;

	private String jndiName;

	public void setJndiContext(JndiContext jndiContext) {
		this.jndiContext = jndiContext;
	}

	public void setJndiName(String jndiName) {
		this.jndiName = jndiName;
	}

	public void initialize() throws Exception {
		if (jndiContext == null) {
			jndiContext = new JndiContext();
		}
	}

	protected Object createInstance() {
		try {
			return jndiContext.lookup(jndiName);
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}
}
