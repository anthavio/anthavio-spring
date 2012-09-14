package com.anthavio.spring.ws;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.axiom.AxiomSoapMessageFactory;
import org.springframework.ws.transport.TransportInputStream;
/**
 * Axiom verze dualni SOAP 1.1 a 1.2 SoapMessageFactory
 * 
 * http://forum.springsource.org/showthread.php?t=56560
 * http://panbhatt.blogspot.com/2011/04/spring-web-service-part-iii-creating.html
 * 
 * @author vanek
 *
 */
public class AxiomDualMessageFactory implements SoapMessageFactory, InitializingBean {

	//private static final Log logger = LogFactory.getLog(AxiomDualMessageFactory.class);

	private static final String REQUEST_CONTEXT_ATTRIBUTE = "DualVersionSoapMessageFactory";

	private AxiomSoapMessageFactory soap11MessageFactory = new AxiomSoapMessageFactory();

	private AxiomSoapMessageFactory soap12MessageFactory = new AxiomSoapMessageFactory();

	private boolean payloadCaching = true;

	// This Object, will be responsible for choosing the Protocol on Runtime, it can be application/xml or text/xml (SOAP 1.2 & SOAP 1.1)
	private SoapProtocolChooser soapProtocolChooser = new HeaderContentTypeSoapProtocolChooser();

	public void setPayloadCaching(boolean payloadCaching) {
		this.payloadCaching = payloadCaching;
	}

	public void setSoapVersion(SoapVersion version) {
		throw new UnsupportedOperationException("Use setSoapProtocolChooser() method");
	}

	public void setSoapProtocolChooser(SoapProtocolChooser soapProtocolChooser) {
		this.soapProtocolChooser = soapProtocolChooser;
	}

	// Function will be invoked, when Spring will create the Bean.
	public void afterPropertiesSet() throws Exception {
		soap11MessageFactory.setSoapVersion(SoapVersion.SOAP_11);
		soap11MessageFactory.setPayloadCaching(payloadCaching);
		soap11MessageFactory.afterPropertiesSet();

		soap12MessageFactory.setSoapVersion(SoapVersion.SOAP_12);
		soap12MessageFactory.setPayloadCaching(payloadCaching);
		soap12MessageFactory.afterPropertiesSet();
	}

	public SoapMessage createWebServiceMessage() {
		return getMessageFactoryForRequestContext().createWebServiceMessage();
	}

	public SoapMessage createWebServiceMessage(InputStream inputStream) throws IOException {
		setMessageFactoryForRequestContext(soap12MessageFactory);
		if (inputStream instanceof TransportInputStream) {
			TransportInputStream transportInputStream = (TransportInputStream) inputStream;
			if (soapProtocolChooser.useSoap11(transportInputStream)) {
				setMessageFactoryForRequestContext(soap11MessageFactory);
			}
		}
		AxiomSoapMessageFactory mf = getMessageFactoryForRequestContext();
		/*
		if (mf == soap11MessageFactory) {
			System.out.println("Final soapMessageFactory? " + soap11MessageFactory);
		} else {
			System.out.println("Final soapMessageFactory? " + soap12MessageFactory);
		}
		 */
		return mf.createWebServiceMessage(inputStream);
	}

	private void setMessageFactoryForRequestContext(AxiomSoapMessageFactory mf) {
		RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
		attrs.setAttribute(REQUEST_CONTEXT_ATTRIBUTE, mf, RequestAttributes.SCOPE_REQUEST);
	}

	private AxiomSoapMessageFactory getMessageFactoryForRequestContext() {
		RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
		AxiomSoapMessageFactory mf = (AxiomSoapMessageFactory) attrs.getAttribute(REQUEST_CONTEXT_ATTRIBUTE,
				RequestAttributes.SCOPE_REQUEST);
		return mf;
	}

}
