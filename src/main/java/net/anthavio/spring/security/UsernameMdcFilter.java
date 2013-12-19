package net.anthavio.spring.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

/**
 * @author vanek
 * 
 * Implementace javax.servlet.Filter, ktera ulozi do org.slf4j.MDC 
 * jmeno autentizovaneho usera ze Spring SecurityContextu
 * Musi bezet az po org.springframework.web.filter.DelegatingFilterProxy
 * 
 * Obvykle je vhodnejsi pouzit {@link AuthenticationListener}, ktery 
 */
public class UsernameMdcFilter implements Filter {

	private static final String MDC_KEY = "MdcUser";

	public void destroy() {
		// nic
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			String userId = "???";
			SecurityContext sc = SecurityContextHolder.getContext();
			if (sc != null && sc.getAuthentication() != null) {
				Object principal = sc.getAuthentication().getPrincipal();
				if (principal != null) {
					if (principal instanceof User) {
						User uprinc = (User) principal;
						userId = uprinc.getUsername();
					} else {
						userId = principal.toString();
					}
				}
			}
			MDC.put(MDC_KEY, userId);
			chain.doFilter(request, response);
		} finally {
			MDC.remove(MDC_KEY);
		}
	}

	public void init(FilterConfig arg0) throws ServletException {
		// nic
	}

}
