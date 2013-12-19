package net.anthavio.logback.web;

import java.io.FileNotFoundException;

import javax.servlet.ServletContext;

import net.anthavio.logback.LogbackConfigurer;

import org.springframework.util.ResourceUtils;
import org.springframework.util.SystemPropertyUtils;
import org.springframework.web.util.WebUtils;


import ch.qos.logback.core.joran.spi.JoranException;

/**
 * Convenience class that performs custom Logback initialization for web environments,
 * allowing for log file paths within the web application.
 *
 * <p>Supports two init parameters at the servlet context level (that is,
 * context-param entries in web.xml):
 *
 * <ul>
 * <li><i>"logbackConfigLocation":</i><br>
 * Location of the Logback config file; either a "classpath:" location (e.g.
 * "classpath:myLogback.xml"), an absolute file URL (e.g. "file:C:/logback.properties),
 * or a plain path relative to the web application root directory (e.g.
 * "/WEB-INF/logback.xml"). If not specified, default Logback initialization will
 * apply ("logback.xml" or "logback_test.xml" in the class path; see Logback documentation for details).
 * <li><i>"logbackExposeWebAppRoot":</i><br>
 * Whether the web app root system property should be exposed, allowing for log
 * file paths relative to the web application root directory. Default is "true";
 * specify "false" to suppress expose of the web app root system property. See
 * below for details on how to use this system property in log file locations.
 * </ul>
 *
 * <p>Note: <code>initLogging</code> should be called before any other Spring activity
 * (when using Logback), for proper initialization before any Spring logging attempts.
 *
 * <p>By default, this configurer automatically sets the web app root system property,
 * for "${key}" substitutions within log file locations in the Logback config file,
 * allowing for log file paths relative to the web application root directory.
 * The default system property key is "webapp.root", to be used in a Logback config
 * file like as follows:
 *
 * <p><code>
 *       <appender name="FILE" class="ch.qos.logback.core.FileAppender">
 *           <layout class="ch.qos.logback.classic.PatternLayout">
 *               <pattern>%-4relative [%thread] %-5level %class - %msg%n</pattern>
 *           </layout>
 *           <File>${webapp.root}/WEB-INF/demo.log</File>
 *       </appender>
 * </code><p>
 * Alternatively, specify a unique context-param "webAppRootKey" per web application.
 * For example, with "webAppRootKey = "demo.root":
 *
 * <p><code>
 *       <appender name="FILE" class="ch.qos.logback.core.FileAppender">
 *           <layout class="ch.qos.logback.classic.PatternLayout">
 *               <pattern>%-4relative [%thread] %-5level %class - %msg%n</pattern>
 *           </layout>
 *           <File>${demo.root}/WEB-INF/demo.log</File>
 *       </appender>
 * </code><p>
 *
 * <p><b>WARNING:</b> Some containers (like Tomcat) do <i>not</i> keep system properties
 * separate per web app. You have to use unique "webAppRootKey" context-params per web
 * app then, to avoid clashes. Other containers like Resin do isolate each web app's
 * system properties: Here you can use the default key (i.e. no "webAppRootKey"
 * context-param at all) without worrying.
 *
 * @author Juergen Hoeller
 * @since 12.08.2003
 * @see org.springframework.util.Log4jConfigurer
 * @see org.springframework.web.util.Log4jConfigListener
 * @see org.springframework.web.util.Log4jConfigServlet
 * @author Davide Baroncelli
 * @since 27-set-2007 13.58.50
 */
public class LogbackWebConfigurer {

	/**
	 * Parameter specifying the location of the logback config file
	 */
	public static final String CONFIG_LOCATION_PARAM = "logbackConfigLocation";

	/**
	 * Parameter specifying whether to expose the web app root system property
	 */
	public static final String EXPOSE_WEB_APP_ROOT_PARAM = "logbackExposeWebAppRoot";

	private LogbackWebConfigurer() {
	}

	/**
	 * Initialize Logback, including setting the web app root system property.
	 *
	 * @param servletContext the current ServletContext
	 * @see org.springframework.web.util.WebUtils#setWebAppRootSystemProperty
	 */
	public static void initLogging(ServletContext servletContext) {
		// Expose the web app root system property.
		if (exposeWebAppRoot(servletContext)) {
			WebUtils.setWebAppRootSystemProperty(servletContext);
		}

		// Only perform custom Logback initialization in case of a config file.
		String location = servletContext.getInitParameter(CONFIG_LOCATION_PARAM);
		if (location != null) {
			// Perform actual Logback initialization; else rely on Logback's default initialization.
			try {
				// Return a URL (e.g. "classpath:" or "file:") as-is;
				// consider a plain file path as relative to the web application root directory.
				if (!ResourceUtils.isUrl(location)) {
					// Resolve system property placeholders before resolving real path.
					location = SystemPropertyUtils.resolvePlaceholders(location);
					location = WebUtils.getRealPath(servletContext, location);
				}

				// Write log message to server log.
				servletContext.log("Initializing Logback from [" + location + "]");

				// Initialize
				LogbackConfigurer.initLogging(location);
			} catch (FileNotFoundException ex) {
				throw new IllegalArgumentException("Invalid 'logbackConfigLocation' parameter: " + ex.getMessage());
			} catch (JoranException e) {
				throw new RuntimeException("Unexpected error while configuring logback", e);
			}
		}
	}

	/**
	 * Shut down Logback, properly releasing all file locks
	 * and resetting the web app root system property.
	 *
	 * @param servletContext the current ServletContext
	 * @see WebUtils#removeWebAppRootSystemProperty
	 */
	public static void shutdownLogging(ServletContext servletContext) {
		servletContext.log("Shutting down Logback");
		try {
			LogbackConfigurer.shutdownLogging();
		} finally {
			// Remove the web app root system property.
			if (exposeWebAppRoot(servletContext)) {
				WebUtils.removeWebAppRootSystemProperty(servletContext);
			}
		}
	}

	/**
	 * Return whether to expose the web app root system property,
	 * checking the corresponding ServletContext init parameter.
	 *
	 * @see #EXPOSE_WEB_APP_ROOT_PARAM
	 */
	@SuppressWarnings({ "BooleanMethodNameMustStartWithQuestion" })
	private static boolean exposeWebAppRoot(ServletContext servletContext) {
		String exposeWebAppRootParam = servletContext.getInitParameter(EXPOSE_WEB_APP_ROOT_PARAM);
		return exposeWebAppRootParam != null && Boolean.valueOf(exposeWebAppRootParam);
	}

}
