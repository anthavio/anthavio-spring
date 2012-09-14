package com.anthavio.spring.ssl;

import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;

import javax.net.ssl.SSLSocketFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * @author vanek
 * 
 * Spring wrapper {@link SSLSocketFactory} nacitajici certifikaty z JKS keystoru
 * 
 */
public class JksSslSocketFactory extends com.anthavio.ssl.JksSslSocketFactory implements InitializingBean {

	private Resource keyStoreLocation;

	private String keyStorePassword;

	private String privateKeyPassword;

	private Resource trustStoreLocation;

	private String trustStorePassword;

	public JksSslSocketFactory() {
		//default
	}

	public JksSslSocketFactory(KeyStore keyStore, String keyPassword, KeyStore trustStore) {
		super(keyStore, keyPassword, trustStore);
	}

	public JksSslSocketFactory(KeyStore keyStore, String keyPassword) {
		super(keyStore, keyPassword);
	}

	public JksSslSocketFactory(KeyStore trustStore) {
		super(trustStore);
	}

	public JksSslSocketFactory(URL keyStoreUrl, String keyStorePassword, String keyPassword, URL trustStoreUrl,
			String trustStorePassword) {
		super(keyStoreUrl, keyStorePassword, keyPassword, trustStoreUrl, trustStorePassword);
	}

	public JksSslSocketFactory(URL storeUrl, String storePassword, String keyPassword) {
		super(storeUrl, storePassword, keyPassword);
	}

	public JksSslSocketFactory(URL storeUrl, String storePassword) {
		super(storeUrl, storePassword);
	}

	public void afterPropertiesSet() throws Exception {
		init();
	}

	public void init() {

		if (getSSLContext() != null) {
			return; //contructor initialized
		}

		if (keyStoreLocation == null && trustStoreLocation == null) {
			throw new IllegalArgumentException("at least one of keyStoreLocation or trustStoreLocation must be specified");
		}
		KeyStore keyStore;
		KeyStore trustStore;

		if (keyStoreLocation != null) {
			if (keyStorePassword == null) {
				throw new IllegalArgumentException("keyStorePassword must be specified");
			}
			if (privateKeyPassword == null) {
				throw new IllegalArgumentException("privateKeyPassword must be specified");
			}

			keyStore = loadKeyStore(keyStoreLocation, keyStorePassword);

			if (trustStoreLocation != null) {
				if (trustStorePassword == null) {
					throw new IllegalArgumentException("trustStorePassword must be specified");
				}
				trustStore = loadKeyStore(trustStoreLocation, trustStorePassword);
			} else {
				//keystore je zaroven truststore
				trustStore = keyStore;
			}
		} else {
			//pouze trustStore (neni client cert autentikace)
			trustStore = loadKeyStore(trustStoreLocation, trustStorePassword);
			keyStore = null;
		}

		createSSLContext(keyStore, privateKeyPassword, trustStore);
	}

	private KeyStore loadKeyStore(Resource location, String password) {
		try {
			KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
			InputStream storeStream = location.getInputStream();
			try {
				store.load(storeStream, password != null ? password.toCharArray() : null);
			} finally {
				storeStream.close();
			}
			return store;
		} catch (Exception x) {
			throw new IllegalArgumentException("Cannot initialize keyStore " + location, x);
		}
	}

	public Resource getKeyStoreLocation() {
		return keyStoreLocation;
	}

	public void setKeyStoreLocation(Resource keyStoreLocation) {
		this.keyStoreLocation = keyStoreLocation;
	}

	public String getKeyStorePassword() {
		return keyStorePassword;
	}

	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	public String getPrivateKeyPassword() {
		return privateKeyPassword;
	}

	public void setPrivateKeyPassword(String privateKeyPassword) {
		this.privateKeyPassword = privateKeyPassword;
	}

	public Resource getTrustStoreLocation() {
		return trustStoreLocation;
	}

	public void setTrustStoreLocation(Resource trustStoreLocation) {
		this.trustStoreLocation = trustStoreLocation;
	}

	public String getTrustStorePassword() {
		return trustStorePassword;
	}

	public void setTrustStorePassword(String trustStorePassword) {
		this.trustStorePassword = trustStorePassword;
	}

}
