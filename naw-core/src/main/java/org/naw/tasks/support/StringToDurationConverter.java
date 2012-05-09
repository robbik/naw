package org.naw.tasks.support;

import org.apache.axis.types.Duration;

import rk.commons.inject.factory.type.converter.TypeConverter;

public class StringToDurationConverter implements TypeConverter {

	public Object convert(Object from) {
		return new Duration((String) from);
	}
}
