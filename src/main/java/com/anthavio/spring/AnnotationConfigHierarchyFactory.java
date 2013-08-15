/**
 * 
 */
package com.anthavio.spring;

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

	private String[] packages;

	private Class<?>[] annotatedClasses;

	private ApplicationContext parent;

	public AnnotationConfigHierarchyFactory(String[] packages, ApplicationContext parent) {
		this.packages = packages;
		this.parent = parent;
	}

	public AnnotationConfigHierarchyFactory(Class<?>[] annotatedClasses, ApplicationContext parent) {
		this.annotatedClasses = annotatedClasses;
		this.parent = parent;
	}

	@Override
	public ApplicationContext getObject() throws Exception {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.setParent(parent);
		if (packages != null) {
			context.scan(packages);
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

	public String[] getPackages() {
		return packages;
	}

	public void setPackages(final String... packages) {
		this.packages = packages;
	}

	public void setParent(final ApplicationContext parent) {
		this.parent = parent;
	}

	public ApplicationContext getParent() {
		return parent;
	}

}
