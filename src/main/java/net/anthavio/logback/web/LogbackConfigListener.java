package net.anthavio.logback.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Bootstrap listener for custom Logback initialization in a web environment.
 * Delegates to LogbackWebConfigurer (see its javadoc for configuration details).
 * <p/>
 * <p>This listener should be registered before ContextLoaderListener in web.xml,
 * when using custom Logback initialization.
 * <p/>
 *
 * @author Juergen Hoeller
 * @author Davide Baroncelli
 */
public class LogbackConfigListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
		LogbackWebConfigurer.initLogging(event.getServletContext());
	}

	public void contextDestroyed(ServletContextEvent event) {
		LogbackWebConfigurer.shutdownLogging(event.getServletContext());
	}

}
