package org.naw.executables.factory.xml;

import org.naw.expression.factory.xml.ExpressionDefinitionParser;
import org.naw.links.factory.xml.LinkFactoryDefinitionParser;
import org.naw.tasks.factory.xml.EmptyDefinitionParser;
import org.naw.tasks.factory.xml.ForkDefinitionParser;
import org.naw.tasks.factory.xml.IfDefinitionParser;
import org.naw.tasks.factory.xml.LogDefinitionParser;
import org.naw.tasks.factory.xml.MergeDefinitionParser;
import org.naw.tasks.factory.xml.ReceiveDefinitionParser;
import org.naw.tasks.factory.xml.SendDefinitionParser;
import org.naw.tasks.factory.xml.WaitDefinitionParser;
import org.naw.tasks.factory.xml.WhileDefinitionParser;

import rk.commons.ioc.factory.xml.NamespaceHandlerSupport;

public class NamespaceHandlerImpl extends NamespaceHandlerSupport {

	public void init() {
		registerObjectDefinitionParser(
				ExecutableDefinitionParser.ELEMENT_LOCAL_NAME,
				new ExecutableDefinitionParser());

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
				ReceiveDefinitionParser.ELEMENT_LOCAL_NAME,
				new ReceiveDefinitionParser());

		registerObjectDefinitionParser(
				LinkFactoryDefinitionParser.ELEMENT_LOCAL_NAME,
				new LinkFactoryDefinitionParser());

		registerObjectDefinitionParser(
				LogDefinitionParser.ELEMENT_LOCAL_NAME,
				new LogDefinitionParser());
	}
}
