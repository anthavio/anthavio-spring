package net.anthavio.spring.test;

import java.util.ArrayList;
import java.util.List;

import net.anthavio.spring.ContextHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextLoader;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.SmartContextLoader;
import org.springframework.test.context.support.AbstractGenericContextLoader;
import org.springframework.util.ObjectUtils;



/**
 * @author vanek
 *
 * {@link ContextLoader} implementation loading context via {@link ContextSingletonBeanFactoryLocator}
 * 
 * Use in {@link ContextConfiguration} annotation to override default {@link AbstractGenericContextLoader}
 * @ContextConfiguration(loader = ContextRefLoader.class, locations = "example-child-context")
 */
public class ContextRefLoader implements SmartContextLoader {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void processContextConfiguration(ContextConfigurationAttributes configAttributes) {
		String[] locations = configAttributes.getLocations();
		locations = processLocations(configAttributes.getDeclaringClass(), locations);
		configAttributes.setLocations(locations);
	}

	public String[] processLocations(Class<?> testClass, String... locations) {
		String[] modifiedLocations = new String[2];
		if (ObjectUtils.isEmpty(locations)) {
			throw new IllegalArgumentException("No location specified");
		} else if (locations.length == 1) {
			modifiedLocations[0] = locations[0];
			modifiedLocations[1] = ContextHelper.DEFAULT_SELECTOR;
		} else if (locations.length == 2) {
			modifiedLocations[0] = locations[0];
			modifiedLocations[1] = locations[1];
		} else {
			throw new IllegalArgumentException("Only 1 or 2 locations are supported");
		}
		return modifiedLocations;
	}

	@Override
	public ApplicationContext loadContext(MergedContextConfiguration mergedConfig) throws Exception {
		AbstractApplicationContext context = loadContext(mergedConfig.getLocations());
		context.getEnvironment().setActiveProfiles(mergedConfig.getActiveProfiles());
		return context;
	}

	@Override
	public AbstractApplicationContext loadContext(String... locations) throws Exception {

		logger.info("Loading " + locations[0] + " context with selector " + locations[1]);
		BeanFactoryLocator locator = ContextSingletonBeanFactoryLocator.getInstance(locations[1]);
		BeanFactoryReference reference = locator.useBeanFactory(locations[0]);
		AbstractApplicationContext context = (AbstractApplicationContext) reference.getFactory();
		//context.registerShutdownHook(); closes only context itself but not parent
		Runtime.getRuntime().addShutdownHook(new ContexClosingHook(context));
		return context;
	}

	private class ContexClosingHook extends Thread {

		protected final Logger logger = LoggerFactory.getLogger(getClass());

		private List<AbstractApplicationContext> contexts;

		public ContexClosingHook(AbstractApplicationContext context) {
			contexts = new ArrayList<AbstractApplicationContext>();
			while (context != null) {
				contexts.add(context);//close parent after child
				if (context.getParent() instanceof AbstractApplicationContext) {
					context = (AbstractApplicationContext) context.getParent();
				} else {
					context = null;
				}
			}
		}

		@Override
		public void run() {
			for (AbstractApplicationContext context : contexts) {
				try {
					//logger.info("Closing " + context);
					context.close();
				} catch (Exception x) {
					logger.warn("Failed to close " + context, x);
				}
			}
		}
	}
}