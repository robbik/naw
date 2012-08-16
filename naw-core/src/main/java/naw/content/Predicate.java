package naw.content;

import naw.os.Bundle;

public interface Predicate {

	<T> T eval(Bundle data, Class<? extends T> returnType) throws Exception;
	
	Object eval(Bundle data) throws Exception;
}
