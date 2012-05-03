package org.apache.axis.types;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateTime {

	private int year;

	private int month;

	private int day;

	private int hour;

	private int minite;

	private int second;

	private int ms;

	private int timeZoneOffSet;

	private int era;

	public DateTime(String source) {
		if (source == null) {
			throw new NullPointerException("dateTime");
		}

		if (source.length() < 19) {
			throw new IllegalArgumentException("bad datetime: " + source);
		}

		if ((source.charAt(4) != '-') || (source.charAt(7) != '-')
				|| (source.charAt(10) != 'T') || (source.charAt(13) != ':')
				|| (source.charAt(16) != ':')) {
			throw new RuntimeException("bad datetime format (" + source
					+ ") with out - s at correct place ");
		}
		
		parseDateTime(source);
	}

	private void parseDateTime(String source) {
		if (source.startsWith("-")) {
			source = source.substring(1);
			era = GregorianCalendar.BC;
		} else {
			era = GregorianCalendar.AD;
		}

		year = Integer.parseInt(source.substring(0, 4));
		month = Integer.parseInt(source.substring(5, 7));
		day = Integer.parseInt(source.substring(8, 10));
		hour = Integer.parseInt(source.substring(11, 13));
		minite = Integer.parseInt(source.substring(14, 16));
		second = Integer.parseInt(source.substring(17, 19));
		ms = 0;

		timeZoneOffSet = TimeZone.getDefault().getRawOffset();

		int msPartLength = 0;

		if (source.length() > 19) {
			String rest = source.substring(19);

			if (rest.startsWith(".")) {
				// i.e this have the ('.'s+) part
				if (rest.endsWith("Z")) {
					timeZoneOffSet = 0;

					ms = Integer.parseInt(rest.substring(1, rest.lastIndexOf("Z")));
					msPartLength = rest.substring(1, rest.lastIndexOf("Z")).trim().length();
				} else if ((rest.lastIndexOf("+") > 0) || (rest.lastIndexOf("-") > 0)) {
					// this is given in a general time zione
					String timeOffSet = null;

					if (rest.lastIndexOf("+") > 0) {
						timeOffSet = rest.substring(rest.lastIndexOf("+") + 1);
						ms = Integer.parseInt(rest.substring(1, rest.lastIndexOf("+")));
						msPartLength = rest.substring(1, rest.lastIndexOf("+")).trim().length();

						// we keep +1 or -1 to finally calculate the value
						timeZoneOffSet = 1;
					} else if (rest.lastIndexOf("-") > 0) {
						timeOffSet = rest.substring(rest.lastIndexOf("-") + 1);

						ms = Integer.parseInt(rest.substring(1, rest.lastIndexOf("-")));
						msPartLength = rest.substring(1, rest.lastIndexOf("-")).trim().length();

						// we keep +1 or -1 to finally calculate the value
						timeZoneOffSet = -1;
					}

					if (timeOffSet.charAt(2) != ':') {
						throw new RuntimeException("invalid time zone format ("
								+ source + ") without : at correct place");
					}

					int hours = Integer.parseInt(timeOffSet.substring(0, 2));
					int minits = Integer.parseInt(timeOffSet.substring(3, 5));

					timeZoneOffSet = ((hours * 60) + minits) * 60000 * timeZoneOffSet;
				} else {
					// i.e it does not have time zone
					ms = Integer.parseInt(rest.substring(1));
					msPartLength = rest.substring(1).trim().length();
				}
			} else {
				if (rest.startsWith("Z")) {
					timeZoneOffSet = 0;
				} else if (rest.startsWith("+") || rest.startsWith("-")) {
					// this is given in a general time zione
					if (rest.charAt(3) != ':') {
						throw new RuntimeException("invalid time zone format ("
								+ source + ") without : at correct place");
					}
					
					int hours = Integer.parseInt(rest.substring(1, 3));
					int minits = Integer.parseInt(rest.substring(4, 6));
					
					timeZoneOffSet = ((hours * 60) + minits) * 60000;
					if (rest.startsWith("-")) {
						timeZoneOffSet = timeZoneOffSet * -1;
					}
				} else {
					throw new NumberFormatException("in valid time zone attribute");
				}
			}
		}

		if (msPartLength != 3) {
			ms = ms * 1000;

			for (int i = 0; i < msPartLength; i++) {
				ms = ms / 10;
			}
		}
	}

	public Calendar getCalendar() {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.setLenient(false);

		calendar.set(Calendar.ERA, era);

		calendar.set(Calendar.YEAR, year);

		// xml month is started from 1 and calendar month is started from 0
		calendar.set(Calendar.MONTH, month - 1);

		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minite);
		calendar.set(Calendar.SECOND, second);

		calendar.set(Calendar.MILLISECOND, ms);
		calendar.set(Calendar.ZONE_OFFSET, timeZoneOffSet);
		calendar.set(Calendar.DST_OFFSET, 0);
		
		if (timeZoneOffSet == 0) {
			calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
		}

		return calendar;
	}
}
