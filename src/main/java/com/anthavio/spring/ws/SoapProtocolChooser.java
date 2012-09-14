/**
 * 
 */
package com.anthavio.spring.ws;

import java.io.IOException;

import org.springframework.ws.transport.TransportInputStream;

/**
 * @author vanek
 *
 */
public interface SoapProtocolChooser {

	public boolean useSoap11(TransportInputStream transportInputStream) throws IOException;
}
