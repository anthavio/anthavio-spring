package net.anthavio.spring.ws;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.AbstractEndpointExceptionResolver;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;
import org.springframework.ws.soap.SoapMessage;

/**
 * 
 * @author vanek
 * 
 * Server endpoint ExceptionResolver
 * 
 * Logs exception on WARN level and (if sendStackTrace == true) sends server stracktrace inside SoapFaultDetail to the clinet.
 */
public class SoapFaultExceptionResolver extends AbstractEndpointExceptionResolver {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private boolean sendStackTrace = false;

	public boolean isSendStackTrace() {
		return sendStackTrace;
	}

	public void setSendStackTrace(boolean sendStackTrace) {
		this.sendStackTrace = sendStackTrace;
	}

	@Override
	protected boolean resolveExceptionInternal(MessageContext messageContext, Object endpoint, Exception ex) {
		log.warn("Exception while invoking webservice", ex);
		Assert.isInstanceOf(SoapMessage.class, messageContext.getResponse(),
		"SimpleSoapExceptionResolver requires a SoapMessage");
		SoapMessage response = (SoapMessage) messageContext.getResponse();
		String faultString = StringUtils.hasLength(ex.getMessage()) ? ex.getMessage() : ex.toString();
		SoapBody body = response.getSoapBody();
		SoapFault fault = body.addServerOrReceiverFault(faultString, Locale.ENGLISH);
		if (sendStackTrace) {
			SoapFaultDetail detail = fault.addFaultDetail();
			SoapFaultDetailElement detailElement = detail.addFaultDetailElement(new QName("ServerException"));
			detailElement.addText(buildStrackTrace(ex));

		}
		return true;
	}

	private String buildStrackTrace(Exception ex) {
		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter(sw);
		//writer.print(ex);
		ex.printStackTrace(writer);
		writer.close();
		return sw.toString();
	}
}