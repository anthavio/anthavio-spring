package com.anthavio.spring.mail;

import javax.mail.Message;

/**
 * @author vanek
 *
 */
public interface MailFetchHandler {

	public void handle(Message message);
}
