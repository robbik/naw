package org.naw.core.xml;

import org.naw.executables.factory.xml.ExecutableDefinitionParser;
import org.naw.expression.factory.xml.ExpressionDefinitionParser;
import org.naw.jndi.factory.xml.JndiContextDefinitionParser;
import org.naw.jndi.factory.xml.JndiRefDefinitionParser;
import org.naw.links.factory.xml.LinkFactoryDefinitionParser;
import org.naw.tasks.factory.xml.EmptyDefinitionParser;
import org.naw.tasks.factory.xml.ForkDefinitionParser;
import org.naw.tasks.factory.xml.IfDefinitionParser;
import org.naw.tasks.factory.xml.LogDefinitionParser;
import org.naw.tasks.factory.xml.MergeDefinitionParser;
import org.naw.tasks.factory.xml.ReceiveDefinitionParser;
import org.naw.tasks.factory.xml.ReceiveReplyDefinitionParser;
import org.naw.tasks.factory.xml.ReplyDefinitionParser;
import org.naw.tasks.factory.xml.SendDefinitionParser;
import org.naw.tasks.factory.xml.WaitDefinitionParser;
import org.naw.tasks.factory.xml.WhileDefinitionParser;

import rk.commons.inject.factory.xml.NamespaceHandlerSupport;

public class NamespaceHandlerImpl extends NamespaceHandlerSupport {

	public void init() {
	    ///// process
		registerObjectDefinitionParser(
				ExecutableDefinitionParser.ELEMENT_LOCAL_NAME,
				new ExecutableDefinitionParser());

		///// tasks...
		registerObjectDefinitionParser(
				ExpressionDefinitionParser.ELEMENT_LOCAL_NAME,
				new ExpressionDefinitionParser());

		registerObjectDefinitionParser(
				EmptyDefinitionParser.ELEMENT_LOCAL_NAME,
				new EmptyDefinitionParser());

		registerObjectDefinitionParser(
				MergeDefinitionParser.ELEMENT_LOCAL_NAME,
				new MergeDefinitionParser());

		registerObjectDefinitionParser(
				WhileDefinitionParser.ELEMENT_LOCAL_NAME,
				new WhileDefinitionParser());

		registerObjectDefinitionParser(
				IfDefinitionParser.ELEMENT_LOCAL_NAME,
				new IfDefinitionParser());

		registerObjectDefinitionParser(
				ForkDefinitionParser.ELEMENT_LOCAL_NAME,
				new ForkDefinitionParser());

		registerObjectDefinitionParser(
				WaitDefinitionParser.ELEMENT_LOCAL_NAME,
				new WaitDefinitionParser());

		registerObjectDefinitionParser(
				SendDefinitionParser.ELEMENT_LOCAL_NAME,
				new SendDefinitionParser());

		registerObjectDefinitionParser(
				ReplyDefinitionParser.ELEMENT_LOCAL_NAME,
				new ReplyDefinitionParser());

		registerObjectDefinitionParser(
				ReceiveDefinitionParser.ELEMENT_LOCAL_NAME,
				new ReceiveDefinitionParser());

		registerObjectDefinitionParser(
				ReceiveReplyDefinitionParser.ELEMENT_LOCAL_NAME,
				new ReceiveReplyDefinitionParser());

		registerObjectDefinitionParser(
				LinkFactoryDefinitionParser.ELEMENT_LOCAL_NAME,
				new LinkFactoryDefinitionParser());

		registerObjectDefinitionParser(
				LogDefinitionParser.ELEMENT_LOCAL_NAME,
				new LogDefinitionParser());

		///// add-ons (jndi)
		try {
			registerObjectDefinitionParser(
					JndiContextDefinitionParser.ELEMENT_LOCAL_NAME,
					new JndiContextDefinitionParser());
			
			registerObjectDefinitionParser(
					JndiRefDefinitionParser.ELEMENT_LOCAL_NAME,
					new JndiRefDefinitionParser());
		} catch (Throwable t) {
			// not supported
		}
	}
}
