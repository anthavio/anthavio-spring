/**
 * 
 */
package com.anthavio.spring.web;

import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.util.StringUtils;

/**
 * @author vanek
 *
 * Typical use case
 * binder.registerCustomEditor(Date.class, new MultiFormatDateEditor("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd"));
 */
public class MultiFormatDateEditor extends PropertyEditorSupport {

	private final DateFormat main;

	private final DateFormat[] others;

	private boolean allowEmpty = true;

	public MultiFormatDateEditor(String mainPattern, String... otherPatterns) {
		this.main = new SimpleDateFormat(mainPattern);
		if (otherPatterns != null && otherPatterns.length != 0) {
			others = new SimpleDateFormat[otherPatterns.length];
			for (int i = 0; i < otherPatterns.length; i++) {
				others[i] = new SimpleDateFormat(otherPatterns[i]);
			}
		} else {
			others = null;
		}
	}

	/**
	 * Parse the Date from the given text, using the specified DateFormat.
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (this.allowEmpty && !StringUtils.hasText(text)) {
			// Treat empty String as null value.
			setValue(null);
		} else {
			Date date = null;
			try {
				date = this.main.parse(text);
			} catch (ParseException ex) {
				if (others == null) {
					throw new IllegalArgumentException("Could not parse date: " + text + "with format " + main, ex);
				} else {
					for (DateFormat format : others) {
						try {
							date = format.parse(text);
							break;
						} catch (ParseException ex2) {

						}
					}
				}

			}
			if (date != null) {
				setValue(date);
			} else {
				throw new IllegalArgumentException("Could not parse date with none of formats: " + text);
			}
		}
	}

	/**
	 * Format the Date as String, using the specified DateFormat.
	 */
	@Override
	public String getAsText() {
		Date value = (Date) getValue();
		return (value != null ? this.main.format(value) : "");
	}
}
