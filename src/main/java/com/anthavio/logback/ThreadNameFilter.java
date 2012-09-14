package com.anthavio.logback;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * @author vanek
 * 
 * Logback Appender Filter rozhodujici podle jmena vlakna 
 */
public class ThreadNameFilter extends AbstractMatcherFilter<ILoggingEvent> {

	private String regex;

	private Pattern pattern;

	@Override
	public FilterReply decide(ILoggingEvent event) {
		Matcher matcher = pattern.matcher(Thread.currentThread().getName());
		if (matcher.matches()) {
			return onMatch;
		} else {
			return onMismatch;
		}
	}

	public void setRegex(String regex) {
		this.regex = regex;
		pattern = Pattern.compile(regex);
	}

	public String getRegex() {
		return regex;
	}

	public static void main(String[] args) {
		ThreadNameFilter filter = new ThreadNameFilter();
		filter.setOnMatch(FilterReply.ACCEPT);
		filter.setOnMismatch(FilterReply.DENY);
		filter.setRegex("^autorun.*|^sis2timer.*");
		//filter.setRegex("^(?!autorun|sis2timer).*");

		Thread.currentThread().setName("sis2timer-thread");
		FilterReply decide = filter.decide(null);
		System.out.println(decide);

		Thread.currentThread().setName("xxx");
		decide = filter.decide(null);
		System.out.println(decide);
	}

}
