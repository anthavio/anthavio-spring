package net.anthavio.spring;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * @author vanek
 * 
 */
public class ContextHelper {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public static final String DEFAULT_SELECTOR = "classpath*:spring/context-locator.xml";

	public static final ContextHelper i = new ContextHelper();

	public static ContextHelper i() {
		return i;
	}

	private final String selector;

	private final BeanFactoryLocator locator;

	private final Map<String, ApplicationContext> springContexts;

	private final Map<String, BeanFactoryReference> springReferences;

	private ContextHelper() {
		this(DEFAULT_SELECTOR);
	}

	public ContextHelper(String selector) {
		this.springContexts = new HashMap<String, ApplicationContext>();
		this.springReferences = new HashMap<String, BeanFactoryReference>();
		this.selector = selector;
		this.locator = ContextSingletonBeanFactoryLocator.getInstance(selector);
	}

	public ApplicationContext locateContext(String name) {
		ApplicationContext context = springContexts.get(name);
		if (context == null) {
			BeanFactoryReference reference = springReferences.get(name);
			if (reference == null) {
				reference = locator.useBeanFactory(name); //reference counting inside !!!
				springReferences.put(name, reference);
			}
			context = (ApplicationContext) reference.getFactory();
			springContexts.put(name, context);
		}
		return context;
	}

	public void releaseContext(String name) {
		BeanFactoryReference reference = springReferences.get(name);
		if (reference == null) {
			log.warn("Cannot release unknown context " + name);
		} else {
			reference.release();
			springReferences.remove(name);
		}
	}

	public String getSelector() {
		return selector;
	}

	/**
	 * By default {@link ApplicationContext#getBean(Class)} searches only itself
	 * for bean definitions and not parent contexts
	 */
	public static <T> T getBean(ListableBeanFactory ctx, Class<T> clazz) {
		return BeanFactoryUtils.beanOfTypeIncludingAncestors(ctx, clazz);
	}

	/**
	 * Closes whole hierarchy of ApplicationContexts
	 * (If ApplicationContext is AbstractApplicationContext)
	 */
	public static void closeContext(ApplicationContext context) {
		while (context != null) {
			if (context instanceof AbstractApplicationContext) {
				((AbstractApplicationContext) context).close();
			}
			context = context.getParent();
		}
	}
}
