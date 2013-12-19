package net.anthavio.spring.security;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.User;

/**
 * @author vanek
 * 
 * Implementace ktera autentikuje jakehokoliv uzivatele a navic mu prideli nakonfigurovane role
 * Pouziti je mozne pri NTLM autentikaci, kdy se role stahnou z Active Directory 
 * nebo pri autentikaci X509 certifikatem a role jsou stejne pro vsechny uzivatele.
 */
public class DummyAuthenticationProvider implements AuthenticationProvider {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private List<GrantedAuthority> defaultAuthorities;

	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		log.info("Authenticationg: " + authentication);

		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		authorities.addAll(authentication.getAuthorities());

		if (this.defaultAuthorities != null) {
			for (GrantedAuthority authority : this.defaultAuthorities) {
				authorities.add(authority);
			}
		}

		User principal = new User(authentication.getName(), "", true, true, true, true, authorities);

		return new UsernamePasswordAuthenticationToken(principal, authentication.getCredentials(),
				authorities);
	}

	public boolean supports(Class<? extends Object> authentication) {
		return true; //bereme cokoliv tak opatrne!
	}

	public void setRoles(List<String> roles) {
		this.defaultAuthorities = new ArrayList<GrantedAuthority>();
		for (String role : roles) {
			this.defaultAuthorities.add(new GrantedAuthorityImpl(role));
		}
	}
}
