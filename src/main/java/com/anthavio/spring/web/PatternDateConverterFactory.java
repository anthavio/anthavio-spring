/**
 * 
 */
package com.anthavio.spring.web;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.util.StringUtils;

/**
 * @author vanek
 *
 */
public class PatternDateConverterFactory implements ConverterFactory<String, Date> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private String pattern;

	public PatternDateConverterFactory(String pattern) {
		this.pattern = pattern;
	}

	@Override
	public <T extends Date> Converter<String, T> getConverter(Class<T> targetType) {
		return new PatternDateConvertor<T>(pattern);
	}

	private final class PatternDateConvertor<T extends Date> implements Converter<String, T> {

		private SimpleDateFormat sdf;

		public PatternDateConvertor(String pattern) {
			this.sdf = new SimpleDateFormat(pattern);
		}

		public T convert(String source) {
			if (StringUtils.hasText(source)) {
				try {
					return (T) sdf.parse(source.trim());
				} catch (ParseException px) {
					log.warn("Exception while converting " + source, px);
					return null;
				}
			} else {
				return null;
			}

		}
	}

}
