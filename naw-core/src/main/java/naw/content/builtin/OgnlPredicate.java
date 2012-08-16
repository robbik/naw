package naw.content.builtin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import naw.content.Predicate;
import naw.os.Bundle;
import ognl.Ognl;
import rk.commons.inject.factory.MapWrapper;
import rk.commons.inject.factory.ObjectFactory;

public class OgnlPredicate implements Predicate {
	
	private final String str;
	
	private final Object compiled;
	
	private Map<String, Object> objects;
	
	public OgnlPredicate(String str) throws Exception {
		this.str = str;
		
		compiled = Ognl.parseExpression(str);
		objects = Collections.emptyMap();
	}
	
	public void initialize(ObjectFactory factory) {
		objects = new MapWrapper(factory);
	}

	@SuppressWarnings("unchecked")
	public <T> T eval(Bundle data, Class<? extends T> returnType) throws Exception {
		Map<String, Object> root = new HashMap<String, Object>();
		
		root.put("data", data);
		root.put("object", objects);
		
		return (T) Ognl.getValue(compiled, (Object) root, returnType);
	}

	public Object eval(Bundle data) throws Exception {
		Map<String, Object> root = new HashMap<String, Object>();
		
		root.put("data", data);
		root.put("object", objects);
		
		return Ognl.getValue(compiled, root);
	}
	
	@Override
	public String toString() {
		return OgnlPredicate.class + " [ expression: " + str + " ]";
	}
}
