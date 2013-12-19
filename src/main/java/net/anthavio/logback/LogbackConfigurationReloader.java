package net.anthavio.logback;

import java.io.File;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

/**
 * A {@link Runnable } class suitable for reconfiguring logback: it should be used with Spring's scheduling infrastructure (via one of the various
 * adapters accepting a Runnable) for creating a recurrent job that periodically checks if the file has changed and starts the reconfiguration if
 * needed. 
 *
 * Not realy needed since logback already features built-in configuration file autoscan
 * 
 * @author Davide Baroncelli
 * @since 28-set-2007 15.01.42
 */
public class LogbackConfigurationReloader extends TimerTask implements Runnable {
	protected final Logger log = LoggerFactory.getLogger(getClass());

	private Resource configResource;

	private long lastReloaded;

	private boolean reconfigureOnFirstRun;

	@Override
	public void run() {
		try {
			log.debug("checking configuration resource '{}'", configResource);
			File configFile = configResource.getFile();
			if (!configFile.exists() || !configFile.isFile()) {
				log.error("file not found for file: '" + configFile + "'");
				return;
			}

			if (shouldReconfigure(configFile)) {
				log.debug("logback reconfiguration needed");
				LogbackConfigurer.initLogging(configFile.toURL());
				lastReloaded = System.currentTimeMillis();
				log.info("logback reconfiguration performed from resource '{}'", configResource);
			}
		} catch (Exception e) {
			log.error("unexpected error while trying to re-configure logback with resource: '" + configResource + "'", e);
		}
	}

	/**
	 * @param configFile The configuration file to check.
	 * @return true if the file last modification date is greater than the last reloading date or the last reloading date is 0 and
	 * {@link #reconfigureOnFirstRun } is true.   
	 */
	protected boolean shouldReconfigure(File configFile) {
		long configLastModified = configFile.lastModified();
		log.debug("last modified='{}', last reloaded='{}'", configLastModified, lastReloaded);
		if (lastReloaded == 0) {
			lastReloaded = configLastModified;
			return reconfigureOnFirstRun;
		} else {
			return configLastModified > lastReloaded;
		}
	}

	/**
	 * @param configResource The resource pointing to the logback configuration file whose modification date must be checked at every run.
	 */
	public void setConfigResource(Resource configResource) {
		this.configResource = configResource;
	}

	/**
	 * @param reconfigureOnFirstRun If set to true forces a logback reconfiguration at runtime. Default is false (i.e. a reconfiguration will only
	 * occur if the file changes <b>after</b> the first run).
	 */
	public void setReconfigureOnFirstRun(boolean reconfigureOnFirstRun) {
		this.reconfigureOnFirstRun = reconfigureOnFirstRun;
	}

}
