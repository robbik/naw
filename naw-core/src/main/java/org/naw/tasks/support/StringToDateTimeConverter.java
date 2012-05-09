package org.naw.tasks.support;

import org.apache.axis.types.DateTime;

import rk.commons.inject.factory.type.converter.TypeConverter;

public class StringToDateTimeConverter implements TypeConverter {

	public Object convert(Object from) {
		return new DateTime((String) from);
	}
}
