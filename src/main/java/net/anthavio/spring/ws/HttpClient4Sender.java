package net.anthavio.spring.ws;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import net.anthavio.spring.ssl.JksSslSocketFactory;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.http.AbstractHttpWebServiceMessageSender;
import org.springframework.ws.transport.http.CommonsHttpMessageSender;
import org.springframework.ws.transport.http.HttpTransportConstants;



/**
 * @author vanek
 *
 * Puvodni {@link CommonsHttpMessageSender} rozsireny o SSLSocketFactory a HttpClient 4.1
 * 
 * Pouziva Springlifecycle metody {@link #init()} a {@link #destroy()}
 * 
 * Je treba dat pozor pokud by se menilo uri a pritom byla pouzivana BASIC authentikace
 */
public class HttpClient4Sender extends AbstractHttpWebServiceMessageSender implements InitializingBean, DisposableBean {

	//private Logger log = LoggerFactory.getLogger(getClass());

	private static final int DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS = (10 * 1000);

	private static final int DEFAULT_READ_TIMEOUT_MILLISECONDS = (60 * 1000);

	//internaly used instances

	private DefaultHttpClient httpClient;

	private HttpContext httpContext;

	//configuration instances

	private final HttpParams clientParams = new BasicHttpParams();

	private int maxTotalConnections = 10;

	private int maxPerRouteConnections = 10;

	private Credentials credentials;

	private AuthScope authScope = AuthScope.ANY;

	private boolean authPreemptive = true;

	private JksSslSocketFactory sslSocketFactory;

	public HttpClient4Sender() {
		clientParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS);
		clientParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, DEFAULT_READ_TIMEOUT_MILLISECONDS);

		HttpProtocolParams.setVersion(clientParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(clientParams, "UTF-8");
	}

	public void afterPropertiesSet() {
		init();
	}

	public void init() {
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));

		ThreadSafeClientConnManager conMan = new ThreadSafeClientConnManager(schemeRegistry);
		conMan.setMaxTotal(maxTotalConnections);
		conMan.setDefaultMaxPerRoute(maxPerRouteConnections);

		httpClient = new DefaultHttpClient(conMan, clientParams);

		if (credentials != null) {
			CredentialsProvider credentialsProvider = httpClient.getCredentialsProvider();
			credentialsProvider.setCredentials(getAuthScope(), credentials);

			//preemptive basic auth hc 4.0 style
			if(authPreemptive) {
				BasicScheme basicAuth = new BasicScheme();
				httpClient.addRequestInterceptor(new PreemptiveAuthInterceptor(basicAuth), 0);
			}

			List<String> authpref = new ArrayList<String>();
			authpref.add(AuthPolicy.BASIC);
			//authpref.add(AuthPolicy.DIGEST);
			httpClient.getParams().setParameter(AuthPNames.TARGET_AUTH_PREF, authpref);
		}

		if (sslSocketFactory != null) {
			SSLSocketFactory sslSf = new SSLSocketFactory(sslSocketFactory.getSSLContext(),
					SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
			Scheme https = new Scheme("https", 443, sslSf);
			schemeRegistry.register(https);
		}
	}

	public void destroy() {
		httpClient.getConnectionManager().shutdown();
	}

	public WebServiceConnection createConnection(URI uri) throws IOException {

		//preemptive basic auth part 1 hc 4.1 style
		/*
		if (credentials != null && authPreemptive && httpContext == null) {
			synchronized (credentials) {
				//This is Double-checked locking which is not safe (I've tested it and it fails sometimes)
				//Use preemptive authentication at your own risk
				//http://www.javaworld.com/javaworld/jw-05-2001/jw-0525-double.html
				if(httpContext == null) {
					log.info("Preemptive BasicAuthCache created for " + credentials.getUserPrincipal() + " to " + uri);
					AuthCache authCache = new BasicAuthCache();
					BasicScheme basicAuth = new BasicScheme();
					HttpHost targetHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
					authCache.put(targetHost, basicAuth);
					httpContext = new BasicHttpContext();
					httpContext.setAttribute(ClientContext.AUTH_CACHE, authCache);
				}
			}
		}
		 */
		HttpPost post = new HttpPost(uri);
		if (isAcceptGzipEncoding()) {
			post.addHeader(HttpTransportConstants.HEADER_ACCEPT_ENCODING, HttpTransportConstants.CONTENT_ENCODING_GZIP);
		}
		return new HttpClient4Connection(httpClient, post, httpContext);
	}

	public Credentials getCredentials() {
		return credentials;
	}

	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}

	public AuthScope getAuthScope() {
		return authScope != null ? authScope : AuthScope.ANY;
	}

	public void setAuthScope(AuthScope authScope) {
		this.authScope = authScope;
	}

	public JksSslSocketFactory getSslSocketFactory() {
		return sslSocketFactory;
	}

	public void setSslSocketFactory(JksSslSocketFactory sslSocketFactory) {
		this.sslSocketFactory = sslSocketFactory;
	}

	public void setAuthPreemptive(boolean authPreemptive) {
		this.authPreemptive = authPreemptive;
	}

	public void setConnectionTimeout(int timeout) {
		if (timeout < 0) {
			throw new IllegalArgumentException("timeout must be a non-negative value");
		}
		clientParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout);
	}

	public void setReadTimeout(int timeout) {
		if (timeout < 0) {
			throw new IllegalArgumentException("timeout must be a non-negative value");
		}
		clientParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout);
	}

	public void setMaxTotalConnections(int maxTotalConnections) {
		if (maxTotalConnections <= 0) {
			throw new IllegalArgumentException("maxTotalConnections must be a positive value");
		}
		this.maxTotalConnections = maxTotalConnections;
	}

	public void setMaxPerRouteConnections(int maxPerRouteConnections) {
		if (maxPerRouteConnections <= 0) {
			throw new IllegalArgumentException("maxPerRouteConnections must be a positive value");
		}
		this.maxPerRouteConnections = maxPerRouteConnections;
	}

}

//preemptive basic auth hc 4.0 style
class PreemptiveAuthInterceptor implements HttpRequestInterceptor {

	private final AuthScheme authScheme;

	public PreemptiveAuthInterceptor(AuthScheme authScheme) {
		this.authScheme = authScheme;
	}

	public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {

		AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);

		// If no auth scheme avaialble yet, try to initialize it preemptively
		if (authState.getAuthScheme() == null) {
			CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(ClientContext.CREDS_PROVIDER);
			HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
			Credentials creds = credsProvider.getCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()));
			if (creds == null) {
				throw new HttpException("No credentials for preemptive authentication");
			}
			authState.setAuthScheme(authScheme);
			authState.setCredentials(creds);
		}
	}
}
