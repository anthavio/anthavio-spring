/**
 * 
 */
package com.anthavio.spring;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.io.support.PropertiesLoaderSupport;

/**
 * @author vanek
 *
 * Load properties file and set it's content as System.setProperty
 * 
 * Spring {@link PropertyPlaceholderConfigurer} umi nahrazovat placeholdres v souboru 
 * pouze pokud je nahrazovana promenna definovana lokalne nebo je to systemova promenna
 *  
 * Touto tridou se nejprve naloaduji promenne z prvniho souboru do systemovych promennych a 
 * nasledny PropertyPlaceholderConfigurer uz muze tyto systemove promenne pouzit 
 */
public class SystemPropertyLoader extends PropertiesLoaderSupport implements BeanFactoryPostProcessor, PriorityOrdered {

	private int order = Ordered.HIGHEST_PRECEDENCE; // before PropertyPlaceholderConfigurers

	public void setOrder(int order) {
		this.order = order;
	}

	public int getOrder() {
		return this.order;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		try {
			Properties props = mergeProperties();
			Enumeration<?> propertyNames = props.propertyNames();
			while (propertyNames.hasMoreElements()) {
				String propertyName = (String) propertyNames.nextElement();
				String propertyValue = props.getProperty(propertyName);
				if (logger.isDebugEnabled()) {
					logger.debug("Setting " + propertyName + "=" + propertyValue);
				}
				System.setProperty(propertyName, propertyValue);
			}
		} catch (IOException ex) {
			throw new BeanInitializationException("Could not load properties", ex);
		}
	}

}
