package com.anthavio.logback;

import java.io.FileNotFoundException;
import java.net.URL;

import org.springframework.util.ResourceUtils;
import org.springframework.util.SystemPropertyUtils;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.selector.ContextSelector;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.classic.util.ContextSelectorStaticBinder;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * Convenience class that features simple methods for custom Log4J configuration.
 * 
 * <p>Only needed for non-default Logback initialization with a custom config location. By default, Logback will simply
 * read its configuration from a "logback.xml" or "logback_test.xml" file in the root of the class path.
 * 
 * <p>For web environments, the analogous LogbackWebConfigurer class can be found in the web package, reading in its
 * configuration from context-params in web.xml. In a J2EE web application, Logback is usually set up via
 * LogbackConfigListener or LogbackConfigServlet, delegating to LogbackWebConfigurer underneath.
 * 
 * @author Juergen Hoeller
 * @author Davide Baroncelli
 * @since 27-set-2007 11.42.07
 */
public class LogbackConfigurer {
	private LogbackConfigurer() {
	}

	/**
	 * Initialize logback from the given file.
	 *
	 * @param location the location of the config file: either a "classpath:" location
	 *                 (e.g. "classpath:logback.xml"), an absolute file URL
	 *                 (e.g. "file:C:/logback.xml), or a plain absolute path in the file system
	 *                 (e.g. "C:/logback.xml")
	 * @throws java.io.FileNotFoundException if the location specifies an invalid file path
	 */
	public static void initLogging(String location) throws FileNotFoundException, JoranException {
		String resolvedLocation = SystemPropertyUtils.resolvePlaceholders(location);
		URL url = ResourceUtils.getURL(resolvedLocation);
		initLogging(url);
	}

	/**
	 * Initialize logback from the given URL.
	 *
	 * @param url the url pointing to the location of the config file.
	 * @throws JoranException if the url points to a non existing location or an error occurs during the parsing operation. 
	 */
	public static void initLogging(URL url) throws JoranException {
		ContextSelector selector = ContextSelectorStaticBinder.getSingleton().getContextSelector();
		LoggerContext loggerContext = selector.getLoggerContext();
		// in the current version logback automatically configures at startup the context, so we have to reset it
		loggerContext.reset();
		new ContextInitializer(loggerContext).configureByResource(url);
		// ContextInitializer.configureByResource(loggerContext, url);
	}

	/**
	 * Shut down logback.
	 * <p>This isn't strictly necessary, but recommended for shutting down
	 * logback in a scenario where the host VM stays alive (for example, when
	 * shutting down an application in a J2EE environment).
	 */
	public static void shutdownLogging() {
		ContextSelector selector = ContextSelectorStaticBinder.getSingleton().getContextSelector();
		LoggerContext loggerContext = selector.getLoggerContext();
		String loggerContextName = loggerContext.getName();
		LoggerContext context = selector.detachLoggerContext(loggerContextName);
		context.reset();
	}
}
