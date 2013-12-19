package net.anthavio.spring.ws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.http.AbstractHttpSenderConnection;

/**
 * @author vanek
 *
 * Implementace spring-ws {@link AbstractHttpSenderConnection} pro HttpClient 4.1
 */
public class HttpClient4Connection extends AbstractHttpSenderConnection {

	private static final String NULL_RESPONSE_MSG = "onSendAfterWrite not called. response is null";

	private final DefaultHttpClient client;

	private final HttpPost request;

	private HttpContext context;

	private HttpResponse response;

	private ByteArrayOutputStream requestBuffer;

	protected HttpClient4Connection(DefaultHttpClient httpClient, HttpPost postMethod, HttpContext context) {
		Assert.notNull(httpClient, "httpClient must not be null");
		Assert.notNull(postMethod, "postMethod must not be null");
		this.client = httpClient;
		this.request = postMethod;
		this.context = context;
	}

	public HttpPost getRequest() {
		return request;
	}

	/*
	 * URI
	 */
	public URI getUri() throws URISyntaxException {
		return new URI(request.getURI().toString());
	}

	/*
	 * Sending request
	 */

	@Override
	protected void addRequestHeader(String name, String value) throws IOException {
		request.addHeader(name, value);
	}

	@Override
	protected void onSendBeforeWrite(WebServiceMessage message) throws IOException {
		requestBuffer = new ByteArrayOutputStream();
	}

	@Override
	protected OutputStream getRequestOutputStream() throws IOException {
		return requestBuffer;
	}

	@Override
	protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
		ByteArrayEntity entity = new ByteArrayEntity(requestBuffer.toByteArray());
		request.setEntity(entity);
		requestBuffer = null;
		if(context!=null) {
			response = client.execute(request, context);
		} else {
			response = client.execute(request);
		}
	}

	/*
	 * Receiving response
	 */

	@Override
	public void onClose() throws IOException {
		if (response != null) {
			response.getEntity().getContent().close();
		}
	}

	@Override
	protected int getResponseCode() throws IOException {
		if (response == null) {
			throw new IllegalStateException(NULL_RESPONSE_MSG);
		}
		return response.getStatusLine().getStatusCode();
	}

	@Override
	protected String getResponseMessage() throws IOException {
		if (response == null) {
			throw new IllegalStateException(NULL_RESPONSE_MSG);
		}
		return response.getStatusLine().getReasonPhrase();
	}

	@Override
	protected long getResponseContentLength() throws IOException {
		if (response == null) {
			throw new IllegalStateException(NULL_RESPONSE_MSG);
		}
		return response.getEntity().getContentLength();
	}

	@Override
	protected InputStream getRawResponseInputStream() throws IOException {
		if (response == null) {
			throw new IllegalStateException(NULL_RESPONSE_MSG);
		}
		return response.getEntity().getContent();
	}

	@Override
	protected Iterator<String> getResponseHeaderNames() throws IOException {
		if (response == null) {
			throw new IllegalStateException(NULL_RESPONSE_MSG);
		}
		Header[] headers = response.getAllHeaders();
		String[] names = new String[headers.length];
		for (int i = 0; i < headers.length; i++) {
			names[i] = headers[i].getName();
		}
		return Arrays.asList(names).iterator();
	}

	@Override
	protected Iterator<String> getResponseHeaders(String name) throws IOException {
		if (response == null) {
			throw new IllegalStateException(NULL_RESPONSE_MSG);
		}
		Header[] headers = response.getHeaders(name);
		String[] values = new String[headers.length];
		for (int i = 0; i < headers.length; i++) {
			values[i] = headers[i].getValue();
		}
		return Arrays.asList(values).iterator();
	}
}
