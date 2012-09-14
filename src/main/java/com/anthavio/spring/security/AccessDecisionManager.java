/**
 * 
 */
package com.anthavio.spring.security;

import java.util.Collection;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.core.Authentication;

/**
 * @author vanek
 * 
 * Tato trida navic pouze loguje neuspesne pokusy o pristup
 */
public class AccessDecisionManager extends AffirmativeBased {

	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(getClass());

	@Override
	public void decide(Authentication authentication, Object object,
			Collection<ConfigAttribute> configAttributes) throws AccessDeniedException {
		try {
			super.decide(authentication, object, configAttributes);
		} catch (AccessDeniedException adx) {
			StringBuilder sb = new StringBuilder();
			sb.append("Access denied for ");
			sb.append(authentication);
			sb.append(" Needed: ");
			sb.append(configAttributes);
			sb.append(" Location: ");
			sb.append(object);
			log.warn(sb.toString());
			throw adx;
		}
	}
}
