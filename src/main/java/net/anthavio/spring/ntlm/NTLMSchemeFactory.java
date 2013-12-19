package net.anthavio.spring.ntlm;

import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeFactory;
import org.apache.http.impl.auth.NTLMScheme;
import org.apache.http.params.HttpParams;

/**
 * @author vanek
 * 
 * HttpClient 4.0 does not provide support for the NTLM authentication scheme out of the box
 * 
 * http://hc.apache.org/httpcomponents-client/ntlm.html
 */
public class NTLMSchemeFactory implements AuthSchemeFactory {

	public AuthScheme newInstance(HttpParams params) {
		return new NTLMScheme(new JCIFSEngine());

	}

}
