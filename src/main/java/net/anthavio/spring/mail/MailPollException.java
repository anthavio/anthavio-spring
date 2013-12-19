package net.anthavio.spring.mail;

import org.springframework.mail.MailException;

/**
 * @author vanek
 *
 */
public class MailPollException extends MailException {

	private static final long serialVersionUID = 1L;

	public MailPollException(String message, Throwable cause) {
		super(message, cause);
	}

	public MailPollException(String message) {
		super(message);
	}

	public MailPollException(Throwable cause) {
		super("Mail poll failed", cause);
	}

}
