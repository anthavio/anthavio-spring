/**
 * 
 */
package net.anthavio.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * @author vanek
 * 
 * From unknown reason 3.1.0 org.springframework.context.annotation.AnnotationConfigApplicationContext
 * does not have constructor with parent ApplicationContext so parent chaining like in
 * ClassPathXmlApplicationContext does not work. This little extension should do the trick
 * 
 * https://jira.springsource.org/browse/SPR-7791
 */
public class AnnotationConfigHierarchyApplicationContext extends
		org.springframework.context.annotation.AnnotationConfigApplicationContext {

	public AnnotationConfigHierarchyApplicationContext(ApplicationContext parent, Class<?>... annotatedClasses)
			throws BeansException {
		super();
		setParent(parent);
		register(annotatedClasses);
		refresh();
	}

	public AnnotationConfigHierarchyApplicationContext(ApplicationContext parent, String... basePackages)
			throws BeansException {
		super();
		setParent(parent);
		scan(basePackages);
		refresh();
	}
}
