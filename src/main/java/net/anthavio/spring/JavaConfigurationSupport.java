/**
 * 
 */
package net.anthavio.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author vanek
 * 
 * Little helper class
 */
@Configuration
public class JavaConfigurationSupport {

	@Autowired
	protected Environment environment;

	public String getEnvStr(String key) {
		return environment.getRequiredProperty(key);
	}

	public String getEnvStr(String key, String defval) {
		String property = environment.getProperty(key);
		if (property != null) {
			return property;
		} else {
			return defval;
		}
	}

	public int getEnvInt(String key) {
		return environment.getRequiredProperty(key, Integer.class);
	}

	public int getEnvInt(String key, int defval) {
		Integer property = environment.getProperty(key, Integer.class);
		if (property != null) {
			return property;
		} else {
			return defval;
		}
	}

	public boolean getEnvBool(String key) {
		return environment.getRequiredProperty(key, Boolean.class);
	}

	public boolean getEnvBool(String key, boolean defval) {
		Boolean property = environment.getProperty(key, Boolean.class);
		if (property != null) {
			return property;
		} else {
			return defval;
		}
	}
}
