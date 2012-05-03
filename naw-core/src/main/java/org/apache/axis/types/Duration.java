/*
 * Copyright 2002-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis.types;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Implementation of the XML Schema type duration. Duration supports a minimum
 * fractional second precision of milliseconds.
 * 
 * @author Wes Moulder <wes@themindelectric.com>
 * @author Dominik Kacprzak (dominik@opentoolbox.com)
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#duration">XML Schema
 *      3.2.6</a>
 */
public class Duration implements Serializable {
	
	private static final long serialVersionUID = 6315612022064408974L;

	private boolean isNegative;
	
	private int years;
	
	private int months;
	
	private int days;
	
	private int hours;
	
	private int minutes;
	
	private double seconds;

	/**
	 * Constructs Duration from a String in an xsd:duration format -
	 * PnYnMnDTnHnMnS.
	 * 
	 * @param duration
	 *            String
	 * @throws SchemaException
	 *             if the string doesn't parse correctly.
	 */
	public Duration(String duration) throws IllegalArgumentException {
		int position = 1;
		int timePosition = duration.indexOf("T");

		// P is required but P by itself is invalid
		if (duration.indexOf("P") == -1 || duration.equals("P")) {
			throw new IllegalArgumentException("bad duration: " + duration);
		}

		// if present, time cannot be empty
		if (duration.lastIndexOf("T") == duration.length() - 1) {
			throw new IllegalArgumentException("bad duration: " + duration);
		}

		// check the sign
		if (duration.startsWith("-")) {
			isNegative = true;
			position++;
		} else {
			isNegative = false;
		}

		// parse time part
		if (timePosition != -1) {
			parseTime(duration.substring(timePosition + 1));
		} else {
			timePosition = duration.length();
		}

		// parse date part
		if (position != timePosition) {
			parseDate(duration.substring(position, timePosition));
		}
	}

	/**
	 * This method parses the time portion of a String that represents
	 * xsd:duration - nHnMnS.
	 * 
	 * @param time
	 * @throws IllegalArgumentException
	 *             if time does not match pattern
	 * 
	 */
	public void parseTime(String time) throws IllegalArgumentException {
		if (time.length() == 0 || time.indexOf("-") != -1) {
			throw new IllegalArgumentException("bad time duration: " + time);
		}

		// check if time ends with either H, M, or S
		if (!time.endsWith("H") && !time.endsWith("M") && !time.endsWith("S")) {
			throw new IllegalArgumentException("bad time duration: " + time);
		}

		try {
			// parse string and extract hours, minutes, and seconds
			int start = 0;

			// Hours
			int end = time.indexOf("H");

			// if there is H in a string but there is no value for hours,
			// throw an exception
			if (start == end) {
				throw new IllegalArgumentException("bad time duration: " + time);
			}

			if (end != -1) {
				hours = Integer.parseInt(time.substring(0, end));
				start = end + 1;
			}

			// Minutes
			end = time.indexOf("M");
			// if there is M in a string but there is no value for hours,
			// throw an exception
			if (start == end) {
				throw new IllegalArgumentException("bad time duration: " + time);
			}

			if (end != -1) {
				minutes = Integer.parseInt(time.substring(start, end));
				start = end + 1;
			}

			// Seconds
			end = time.indexOf("S");
			// if there is S in a string but there is no value for hours,
			// throw an exception
			if (start == end) {
				throw new IllegalArgumentException("bad time duration: " + time);
			}

			if (end != -1) {
				setSeconds(Double.parseDouble(time.substring(start, end)));
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("bad time duration: " + time);
		}
	}

	/**
	 * This method parses the date portion of a String that represents
	 * xsd:duration - nYnMnD.
	 * 
	 * @param date
	 * @throws IllegalArgumentException
	 *             if date does not match pattern
	 * 
	 */
	public void parseDate(String date) throws IllegalArgumentException {
		if (date.length() == 0 || date.indexOf("-") != -1) {
			throw new IllegalArgumentException("bad date duration: " + date);
		}

		// check if date string ends with either Y, M, or D
		if (!date.endsWith("Y") && !date.endsWith("M") && !date.endsWith("D")) {
			throw new IllegalArgumentException("bad date duration: " + date);
		}

		// catch any parsing exception
		try {
			// parse string and extract years, months, days
			int start = 0;
			int end = date.indexOf("Y");

			// if there is Y in a string but there is no value for years,
			// throw an exception
			if (start == end) {
				throw new IllegalArgumentException("bad date duration: " + date);
			}
			if (end != -1) {
				years = Integer.parseInt(date.substring(0, end));
				start = end + 1;
			}

			// months
			end = date.indexOf("M");
			// if there is M in a string but there is no value for months,
			// throw an exception
			if (start == end) {
				throw new IllegalArgumentException("bad date duration: " + date);
			}
			if (end != -1) {
				months = Integer.parseInt(date.substring(start, end));
				start = end + 1;
			}

			end = date.indexOf("D");
			// if there is D in a string but there is no value for days,
			// throw an exception
			if (start == end) {
				throw new IllegalArgumentException("bad date duration: " + date);
			}
			if (end != -1) {
				days = Integer.parseInt(date.substring(start, end));
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("bad date duration: " + date);
		}
	}

	public boolean isNegative() {
		return isNegative;
	}

	public int getYears() {
		return years;
	}

	public int getMonths() {
		return months;
	}

	public int getDays() {
		return days;
	}

	public int getHours() {
		return hours;
	}

	public int getMinutes() {
		return minutes;
	}

	public double getSeconds() {
		return seconds;
	}

	public void setNegative(boolean negative) {
		isNegative = negative;
	}

	public void setYears(int years) {
		this.years = years;
	}

	public void setMonths(int months) {
		this.months = months;
	}

	public void setDays(int days) {
		this.days = days;
	}

	public void setHours(int hours) {
		this.hours = hours;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	public void setSeconds(double seconds) {
		this.seconds = ((double) (Math.round(seconds * 100))) / 100;
	}

	/**
	 * This returns the xml representation of an xsd:duration object.
	 */
	public String toString() {
		StringBuffer duration = new StringBuffer();

		duration.append("P");

		if (years != 0) {
			duration.append(years + "Y");
		}
		if (months != 0) {
			duration.append(months + "M");
		}
		if (days != 0) {
			duration.append(days + "D");
		}
		if (hours != 0 || minutes != 0 || seconds != 0.0) {
			duration.append("T");

			if (hours != 0) {
				duration.append(hours + "H");

			}
			if (minutes != 0) {
				duration.append(minutes + "M");

			}
			if (seconds != 0) {
				if (seconds == (int) seconds) {
					duration.append((int) seconds + "S");
				} else {
					duration.append(seconds + "S");
				}
			}
		}

		if (duration.length() == 1) {
			duration.append("T0S");
		}

		if (isNegative) {
			duration.insert(0, "-");
		}

		return duration.toString();
	}

	/**
	 * Returns duration as a calendar. Due to the way a Calendar class works,
	 * the values for particular fields may not be the same as obtained through
	 * getter methods. For example, if a Duration's object getMonths returns 20,
	 * a similar call on a Calendar object will return 1 year and 8 months.
	 * 
	 * @param cal
	 *            Calendar
	 */
	public Calendar add(Calendar cal) {
		cal.add(Calendar.YEAR, years);
		cal.add(Calendar.MONTH, months);
		cal.add(Calendar.DATE, days);
		cal.add(Calendar.HOUR, hours);
		cal.add(Calendar.MINUTE, minutes);
		cal.add(Calendar.SECOND, (int) seconds);
		cal.add(Calendar.MILLISECOND,
				(int) (seconds * 100 - Math.round(seconds) * 100));
		
		return cal;
	}
}
