package org.naw.integration;

import org.naw.core.AbstractEngine;
import org.springframework.context.ApplicationContext;

import rk.commons.inject.context.XmlIocContext;
import rk.commons.inject.factory.SingletonIocObjectFactory;
import rk.commons.ioc.integration.SpringIocObjectFactory;

public class SpringXmlEngine extends AbstractEngine {

    private static final String NAMESPACE_URI = "http://www.naw.org/schema/naw";

    private static final String NAMESPACE_HANDLER = "classpath:META-INF/naw/naw.handlers";

    private static final String NAMESPACE_SCHEMA = "classpath:META-INF/naw/naw.schemas";
    
    protected final ApplicationContext spring;
	
	protected final XmlIocContext context;

	public SpringXmlEngine(ApplicationContext spring, String... locations) {
		this.spring = spring;
		
		initialize();
		
		context = new XmlIocContext();
		
		context.setResourceLoader(resourceLoader);
		
		context.setXmlDefaultNamespace(NAMESPACE_URI);
		
		context.setNamespaceHandlerPath(NAMESPACE_HANDLER);
		context.setNamespaceSchemaPath(NAMESPACE_SCHEMA);
		
		context.setIocObjectFactory(iocFactory);
		context.setObjectDefinitionRegistry(iocFactory);
		
		context.setLocations(addDefaultImport(locations));

		context.refresh(false);
	}
	
	protected SingletonIocObjectFactory createIocObjectFactory() {
		return new SpringIocObjectFactory(resourceLoader, spring);
	}
}
