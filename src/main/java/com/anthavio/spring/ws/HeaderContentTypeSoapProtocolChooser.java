package com.anthavio.spring.ws;

/**
 *
 * @author vanek
 */
import java.io.IOException;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.transport.TransportInputStream;

public class HeaderContentTypeSoapProtocolChooser implements SoapProtocolChooser {

	private final Logger log = LoggerFactory.getLogger(getClass());

	//private static final Pattern userAgentPattern = Pattern.compile("html", Pattern.CASE_INSENSITIVE);

	public boolean useSoap11(TransportInputStream transportInputStream) throws IOException {
		for (Iterator<String> headerNames = transportInputStream.getHeaderNames(); headerNames
				.hasNext();) {
			String headerName = headerNames.next();
			//logger.debug("found headerName: " + headerName);
			for (Iterator<String> headerValues = transportInputStream.getHeaders(headerName); headerValues
					.hasNext();) {
				String headerValue = headerValues.next();
				//logger.debug("     headerValue? " + headerValue);
				// This is the code written in order to support multiple Endpints by selection of SOAP
				if (headerName.toLowerCase().contains("content-type")) {
					//logger.debug("Content Type  - " + headerValue);
					if (headerValue.trim().toLowerCase().contains("text/xml")) {
						log.debug("Found text/xml in header.  Using SOAP 1.1");
						return true;
					}

				}
			}
		}
		return false;
	}
}
