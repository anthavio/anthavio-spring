/**
 * 
 */
package net.anthavio.spring.web;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.time.FastDateFormat;
import org.springframework.format.Formatter;

/**
 * @author vanek
 *
 */
public class PatternDateFormatter implements Formatter<Date> {

	private String pattern;

	private FastDateFormat fdf;

	public PatternDateFormatter(String pattern) {
		this.pattern = pattern;
		this.fdf = FastDateFormat.getInstance(pattern);
	}

	@Override
	public String print(Date date, Locale locale) {
		return fdf.format(date);
	}

	@Override
	public Date parse(String text, Locale locale) throws ParseException {
		return new SimpleDateFormat(pattern).parse(text);
	}

}
