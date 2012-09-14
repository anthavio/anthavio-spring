/**
 * 
 */
package com.anthavio.spring;

import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationScopeMetadataResolver;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopeMetadataResolver;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.Assert;

/**
 * @author vanek
 * 
 * ClassPathXmlApplicationContext -> AbstractXmlApplicationContext -> AbstractRefreshableConfigApplicationContext -> AbstractRefreshableApplicationContext -> AbstractApplicationContext
 * AnnotationConfigHierarchyApplicationContext -> GenericApplicationContext -> AbstractApplicationContext
 */
public class AnnotationConfigHierarchyApplicationContext2 extends GenericApplicationContext {

	private final AnnotatedBeanDefinitionReader reader = new AnnotatedBeanDefinitionReader(this);

	private final ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(this);


	/**
	 * Create a new AnnotationConfigApplicationContext that needs to be populated
	 * through {@link #register} calls and then manually {@linkplain #refresh refreshed}.
	 */
	public AnnotationConfigHierarchyApplicationContext2(ApplicationContext parent) {
		setParent(parent);
	}

	/**
	 * Create a new AnnotationConfigApplicationContext, deriving bean definitions
	 * from the given annotated classes and automatically refreshing the context.
	 * @param annotatedClasses one or more annotated classes,
	 * e.g. {@link Configuration @Configuration} classes
	 */
	public AnnotationConfigHierarchyApplicationContext2(Class<?>[] annotatedClasses, ApplicationContext parent) {
		this(parent);
		register(annotatedClasses);
		refresh();
	}

	/**
	 * Create a new AnnotationConfigApplicationContext, scanning for bean definitions
	 * in the given packages and automatically refreshing the context.
	 * @param basePackages the packages to check for annotated classes
	 */
	public AnnotationConfigHierarchyApplicationContext2(String[] basePackages, ApplicationContext parent) {
		this(parent);
		scan(basePackages);
		refresh();
	}


	/**
	 * {@inheritDoc}
	 * <p>Delegates given environment to underlying {@link AnnotatedBeanDefinitionReader}
	 * and {@link ClassPathBeanDefinitionScanner} members.
	 */
	@Override
	public void setEnvironment(ConfigurableEnvironment environment) {
		super.setEnvironment(environment);
		this.reader.setEnvironment(environment);
		this.scanner.setEnvironment(environment);
	}

	/**
	 * Provide a custom {@link BeanNameGenerator} for use with {@link AnnotatedBeanDefinitionReader}
	 * and/or {@link ClassPathBeanDefinitionScanner}, if any.
	 * <p>Default is {@link org.springframework.context.annotation.AnnotationBeanNameGenerator}.
	 * <p>Any call to this method must occur prior to calls to {@link #register(Class...)}
	 * and/or {@link #scan(String...)}.
	 * @see AnnotatedBeanDefinitionReader#setBeanNameGenerator
	 * @see ClassPathBeanDefinitionScanner#setBeanNameGenerator
	 */
	public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		this.reader.setBeanNameGenerator(beanNameGenerator);
		this.scanner.setBeanNameGenerator(beanNameGenerator);
	}

	/**
	 * Set the {@link ScopeMetadataResolver} to use for detected bean classes.
	 * <p>The default is an {@link AnnotationScopeMetadataResolver}.
	 * <p>Any call to this method must occur prior to calls to {@link #register(Class...)}
	 * and/or {@link #scan(String...)}.
	 */
	public void setScopeMetadataResolver(ScopeMetadataResolver scopeMetadataResolver) {
		this.reader.setScopeMetadataResolver(scopeMetadataResolver);
		this.scanner.setScopeMetadataResolver(scopeMetadataResolver);
	}

	/**
	 * Register one or more annotated classes to be processed.
	 * Note that {@link #refresh()} must be called in order for the context to fully
	 * process the new class.
	 * <p>Calls to {@link #register} are idempotent; adding the same
	 * annotated class more than once has no additional effect.
	 * @param annotatedClasses one or more annotated classes,
	 * e.g. {@link Configuration @Configuration} classes
	 * @see #scan(String...)
	 * @see #refresh()
	 */
	public void register(Class<?>... annotatedClasses) {
		Assert.notEmpty(annotatedClasses, "At least one annotated class must be specified");
		this.reader.register(annotatedClasses);
	}

	/**
	 * Perform a scan within the specified base packages.
	 * Note that {@link #refresh()} must be called in order for the context to
	 * fully process the new class.
	 * @param basePackages the packages to check for annotated classes
	 * @see #register(Class...)
	 * @see #refresh()
	 */
	public void scan(String... basePackages) {
		Assert.notEmpty(basePackages, "At least one base package must be specified");
		this.scanner.scan(basePackages);
	}

}
