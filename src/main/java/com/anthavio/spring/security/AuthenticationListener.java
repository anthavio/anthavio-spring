/**
 * 
 */
package com.anthavio.spring.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LoggerListener;
import org.springframework.util.ClassUtils;

/**
 * @author vanek
 * 
 * Vykradeny {@link LoggerListener} ale navic ulozi username do {@link MDC}
 */
public class AuthenticationListener implements ApplicationListener<AbstractAuthenticationEvent> {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private boolean logInteractiveAuthenticationSuccessEvents = true;

	public void onApplicationEvent(AbstractAuthenticationEvent event) {

		String username = event.getAuthentication().getName();
		MDC.put("MdcUser", username);

		if (!logInteractiveAuthenticationSuccessEvents
				&& event instanceof InteractiveAuthenticationSuccessEvent) {
			return;
		}

		if (logger.isInfoEnabled()) {
			final StringBuilder builder = new StringBuilder();
			builder.append("Authentication event ");
			builder.append(ClassUtils.getShortName(event.getClass()));
			builder.append(": ");
			builder.append(event.getAuthentication().getName());
			builder.append("; details: ");
			builder.append(event.getAuthentication().getDetails());

			if (event instanceof AbstractAuthenticationFailureEvent) {
				builder.append("; exception: ");
				builder.append(((AbstractAuthenticationFailureEvent) event).getException().getMessage());
			}

			logger.info(builder.toString());
		}

	}

	public boolean isLogInteractiveAuthenticationSuccessEvents() {
		return logInteractiveAuthenticationSuccessEvents;
	}

	public void setLogInteractiveAuthenticationSuccessEvents(
			boolean logInteractiveAuthenticationSuccessEvents) {
		this.logInteractiveAuthenticationSuccessEvents = logInteractiveAuthenticationSuccessEvents;
	}
}
