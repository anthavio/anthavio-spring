package com.anthavio.logback;

import org.slf4j.MDC;
import org.slf4j.Marker;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * @author vanek
 * 
 * Adds Spring transaction info into ILoggingEvent
 */
public class SpringTxMdcFilter extends TurboFilter /*extends AbstractMatcherFilter<ILoggingEvent>*/{

	private boolean verbose = true;

	private String MDCKey = "TxInfo";

	@Override
	public FilterReply decide(Marker marker, Logger logger, Level level, String format,
			Object[] params, Throwable t) {
		String txInfo = getTransactionStatus(verbose);
		MDC.put(MDCKey, txInfo);
		//event.getMdc().put(MDCKey, string);
		return FilterReply.NEUTRAL;
	}

	//@Override
	public FilterReply decide(ILoggingEvent event) {
		//String txInfo = getTransactionStatus(verbose);
		//MDC.put(MDCKey, txInfo); //nefunguje - nepropadne se do aktualniho eventu
		//event.getMdc().put(MDCKey, txInfo); //nefunguje - nelze menit
		return FilterReply.NEUTRAL;
	}

	public static String getTransactionStatus(boolean verbose) {
		boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
		String status = null;
		if (verbose) {
			if (txActive) {
				String txName = TransactionSynchronizationManager.getCurrentTransactionName();
				if (txName != null) {
					status = txName;
				} else {
					status = "null";
				}

			} else {
				status = "notx";
			}
		} else {
			status = (txActive) ? "[+] " : "[-] ";
		}
		return status;
	}

	public Boolean getVerbose() {
		return verbose;
	}

	public void setVerbose(Boolean verbose) {
		this.verbose = verbose;
	}

	public String getMDCKey() {
		return MDCKey;
	}

	public void setMDCKey(String mdcName) {
		this.MDCKey = mdcName;
	}

}
