package org.naw.core;

import rk.commons.inject.context.XmlIocContext;

public class XmlEngine extends AbstractEngine {

    private static final String NAMESPACE_URI = "http://www.naw.org/schema/naw";

    private static final String NAMESPACE_HANDLER = "classpath:META-INF/naw/naw.handlers";

    private static final String NAMESPACE_SCHEMA = "classpath:META-INF/naw/naw.schemas";
	
	protected final XmlIocContext context;

	public XmlEngine(String... locations) {
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
}
