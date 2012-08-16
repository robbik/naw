package naw.content.builtin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import naw.content.Predicate;
import naw.os.Bundle;

import org.mvel2.MVEL;

import rk.commons.inject.factory.MapWrapper;
import rk.commons.inject.factory.ObjectFactory;

public class MvelPredicate implements Predicate {
	
	private final String str;
	
	private final Object compiled;
	
	private Map<String, Object> objects;
	
	public MvelPredicate(String str) {
		this.str = str;
		
		compiled = MVEL.compileExpression(str);
		objects = Collections.emptyMap();
	}
	
	public void initialize(ObjectFactory factory) {
		objects = new MapWrapper(factory);
	}

	public <T> T eval(Bundle data, Class<? extends T> returnType) throws Exception {
		Map<String, Object> root = new HashMap<String, Object>();
		
		root.put("data", data);
		root.put("object", objects);
		
		return MVEL.executeExpression(compiled, root, returnType);
	}

	public Object eval(Bundle data) throws Exception {
		Map<String, Object> root = new HashMap<String, Object>();
		
		root.put("data", data);
		root.put("object", objects);
		
		return MVEL.executeExpression(compiled, root);
	}
	
	@Override
	public String toString() {
		return MvelPredicate.class + " [ expression: " + str + " ]";
	}
}
