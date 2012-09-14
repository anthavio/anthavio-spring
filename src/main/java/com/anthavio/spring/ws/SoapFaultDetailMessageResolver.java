/**
 * 
 */
package com.anthavio.spring.ws;

import java.io.IOException;
import java.util.Iterator;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.FaultMessageResolver;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.SoapFaultClientException;

import com.anthavio.xml.StringResult;

/**
 * SoapFaultMessageResolver extension. Logs SoapFaultDetail content
 * 
 * @author vanek
 *
 */
public class SoapFaultDetailMessageResolver implements FaultMessageResolver {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void resolveFault(WebServiceMessage message) throws IOException {
		SoapMessage soapMessage = (SoapMessage) message;
		SoapBody body = soapMessage.getSoapBody();
		SoapFault soapFault = body != null ? body.getFault() : null;
		if (soapFault != null && soapFault.getFaultDetail() != null) {
			try {
				logFaultDetail(soapFault);
			} catch (TransformerException trx) {
				log.error("Failed to extract SoapFaultDetail", trx);
			}
		}
		throw new SoapFaultClientException(soapMessage);
	}

	private void logFaultDetail(SoapFault soapFault) throws TransformerException {
		SoapFaultDetail faultDetail = soapFault.getFaultDetail();
		Iterator<SoapFaultDetailElement> detailEntries = faultDetail.getDetailEntries();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		while (detailEntries.hasNext()) {
			SoapFaultDetailElement detailElement = detailEntries.next();
			StringResult result = new StringResult();
			transformer.transform(detailElement.getSource(), result);
			log.warn("Found SoapFaultDetailElement " + detailElement.getName() + "\n" + result);
		}
	}

}
