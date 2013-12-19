package net.anthavio.spring.mail;

import javax.mail.MessagingException;

/**
 * @author vanek
 *
 */
public interface MailPollHandler {

	public void onPollStart();

	public void onPollEnd();

	public void onPollException(MessagingException mx);

	//public void onFetchException(MessagingException mx);

	//public void onCloseException(MessagingException mx);
}
