package org.naw.integration;

import org.naw.core.impl.AbstractEngine;
import org.springframework.context.ApplicationContext;

import rk.commons.inject.context.XmlContext;
import rk.commons.inject.factory.AbstractObjectFactory;
import rk.commons.inject.integration.SpringObjectFactory;

public class SpringXmlEngine extends AbstractEngine {

    private static final String NAMESPACE_URI = "http://www.naw.org/schema/naw";

    private static final String NAMESPACE_HANDLER = "classpath:META-INF/naw/naw.handlers";

    private static final String NAMESPACE_SCHEMA = "classpath:META-INF/naw/naw.schemas";
    
    protected final ApplicationContext spring;
	
	protected final XmlContext context;

	public SpringXmlEngine(ApplicationContext spring, String... locations) {
		this.spring = spring;
		
		initialize();
		
		context = new XmlContext();
		
		context.setResourceLoader(resourceLoader);
		
		context.setXmlDefaultNamespace(NAMESPACE_URI);
		
		context.setNamespaceHandlerPath(NAMESPACE_HANDLER);
		context.setNamespaceSchemaPath(NAMESPACE_SCHEMA);
		
		context.setObjectFactory(objectFactory);
		context.setObjectDefinitionRegistry(objectFactory);
		
		context.setLocations(addDefaultImport(locations));

		context.refresh(false);
	}
	
	protected AbstractObjectFactory createObjectFactory() {
		return new SpringObjectFactory(resourceLoader, spring);
	}
}
