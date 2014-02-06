/**
 * 
 */
package net.anthavio.spring;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * org.springframework.context.annotation.AnnotationConfigApplicationContext lacks support for parent context 
 * 
 * https://jira.springsource.org/browse/SPR-7791
 * 
 * @author vanek
 *
 */
public class AnnotationConfigHierarchyFactory implements FactoryBean<ApplicationContext> {

	private String[] basePackages;

	private Class<?>[] annotatedClasses;

	private ApplicationContext parent;

	public AnnotationConfigHierarchyFactory(ApplicationContext parent, String... basePackages) {
		this.parent = parent;
		this.basePackages = basePackages;
	}

	public AnnotationConfigHierarchyFactory(ApplicationContext parent, Class<?>... annotatedClasses) {
		this.parent = parent;
		this.annotatedClasses = annotatedClasses;
	}

	@Override
	public ApplicationContext getObject() throws Exception {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.setParent(parent);
		if (basePackages != null) {
			context.scan(basePackages);
		}
		if (annotatedClasses != null) {
			context.register(annotatedClasses);
		}
		context.refresh();
		return context;
	}

	@Override
	public Class<?> getObjectType() {
		return ApplicationContext.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public Class<?>[] getAnnotatedClasses() {
		return annotatedClasses;
	}

	public void setAnnotatedClasses(Class<?>[] annotatedClasses) {
		this.annotatedClasses = annotatedClasses;
	}

	public String[] getBasePackages() {
		return basePackages;
	}

	public void setBasePackages(final String... basePackages) {
		this.basePackages = basePackages;
	}

	public void setParent(final ApplicationContext parent) {
		this.parent = parent;
	}

	public ApplicationContext getParent() {
		return parent;
	}

}
