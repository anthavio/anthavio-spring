package net.anthavio.spring.web;

import net.anthavio.enums.EnumStr;
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
public class EnumStrConverterFactory implements ConverterFactory<String, EnumStr> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public <T extends EnumStr> Converter<String, T> getConverter(Class<T> targetType) {
		return new StringToEnumStrConverter<T>(targetType);
	}

	private final class StringToEnumStrConverter<T extends EnumStr> implements Converter<String, T> {

		private final Class<T> enumType;

		public StringToEnumStrConverter(Class<T> enumType) {
			this.enumType = enumType;
		}

		public T convert(String source) {
			if (StringUtils.hasText(source)) {
				try {
					return EnumUtil.getEnum(this.enumType, source.trim());
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
