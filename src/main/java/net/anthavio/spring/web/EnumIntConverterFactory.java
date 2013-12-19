package net.anthavio.spring.web;

import net.anthavio.enums.EnumInt;
import net.anthavio.enums.EnumUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.util.StringUtils;


/**
 * @author vanek
 *
 */
public class EnumIntConverterFactory implements ConverterFactory<String, EnumInt> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public <T extends EnumInt> Converter<String, T> getConverter(Class<T> targetType) {
		return new StringToEnumIntConverter<T>(targetType);
	}

	private final class StringToEnumIntConverter<T extends EnumInt> implements Converter<String, T> {

		private final Class<T> enumType;

		public StringToEnumIntConverter(Class<T> enumType) {
			this.enumType = enumType;
		}

		public T convert(String source) {
			if (StringUtils.hasText(source)) {
				try {
					return EnumUtil.getEnum(this.enumType, Integer.parseInt(source.trim()));
				} catch (Exception x) {
					log.warn("Exception while converting " + source + " to " + enumType.getSimpleName(), x);
					return null;
				}
			} else {
				return null;
			}

		}
	}
}
