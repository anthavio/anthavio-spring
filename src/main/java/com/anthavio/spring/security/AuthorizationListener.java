/**
 * 
 */
package com.anthavio.spring.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationListener;
import org.springframework.security.access.event.AbstractAuthorizationEvent;
import org.springframework.security.access.event.AuthenticationCredentialsNotFoundEvent;
import org.springframework.security.access.event.AuthorizationFailureEvent;
import org.springframework.security.access.event.AuthorizedEvent;
import org.springframework.security.access.event.LoggerListener;
import org.springframework.security.access.event.PublicInvocationEvent;

/**
 * @author vanek
 * 
 * Vykradene @see {@link LoggerListener} ale 
 * - loguje na jinych urovnich
 * - updatuje org.slf4j.MDC se jmenem principala 
 */
public class AuthorizationListener implements ApplicationListener<AbstractAuthorizationEvent> {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public void onApplicationEvent(AbstractAuthorizationEvent event) {
		if (event instanceof AuthenticationCredentialsNotFoundEvent) {
			AuthenticationCredentialsNotFoundEvent authEvent = (AuthenticationCredentialsNotFoundEvent) event;

			MDC.remove("MdcUser");

			if (logger.isWarnEnabled()) {
				logger.warn("Security interception failed due to: "
						+ authEvent.getCredentialsNotFoundException() + "; secure object: "
						+ authEvent.getSource() + "; configuration attributes: "
						+ authEvent.getConfigAttributes());
			}
		}

		if (event instanceof AuthorizationFailureEvent) {
			AuthorizationFailureEvent authEvent = (AuthorizationFailureEvent) event;

			MDC.put("MdcUser", authEvent.getAuthentication().getName());

			if (logger.isWarnEnabled()) {
				logger.warn("Security authorization failed due to: " + authEvent.getAccessDeniedException()
						+ "; authenticated principal: " + authEvent.getAuthentication() + "; secure object: "
						+ authEvent.getSource() + "; configuration attributes: "
						+ authEvent.getConfigAttributes());
			}
		}

		if (event instanceof AuthorizedEvent) {
			AuthorizedEvent authEvent = (AuthorizedEvent) event;

			MDC.put("MdcUser", authEvent.getAuthentication().getName());

			if (logger.isDebugEnabled()) {
				logger.debug("Security authorized for authenticated principal: "
						+ authEvent.getAuthentication() + "; secure object: " + authEvent.getSource()
						+ "; configuration attributes: " + authEvent.getConfigAttributes());
			}
		}

		if (event instanceof PublicInvocationEvent) {
			PublicInvocationEvent authEvent = (PublicInvocationEvent) event;

			if (logger.isDebugEnabled()) {
				logger.debug("Security interception not required for public secure object: "
						+ authEvent.getSource());
			}
		}
	}

}
