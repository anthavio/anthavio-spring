package net.anthavio.spring.mail;

import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import javax.mail.AuthenticationFailedException;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * @author vanek
 * 
 * By default reading and deleting messages from pop3 INBOX folder
 */
public class JavaMailPoller implements Runnable, InitializingBean, DisposableBean {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public static final String DEFAULT_THREAD_NAME_PREFIX = ClassUtils
			.getShortName(JavaMailPoller.class) + "-";

	public static final String DEFAULT_PROTOCOL = "pop3";

	private String host;

	private int port;

	private String username;

	private String password;

	private String folderName = "INBOX";

	private Properties javaMailProperties = new Properties();

	private String protocol = DEFAULT_PROTOCOL;

	private Flags.Flag messageFlag = Flag.DELETED;

	private boolean expungeDeleted = true;

	private MailFetchHandler mailFetchHandler;

	private MailPollHandler mailPollHandler;

	private Executor taskExecutor;

	private int startDelay = 0;

	private int pollDelay = 30000;

	private String beanName;

	private Session session;

	private final Object lifecycleMonitor = new Object();

	private boolean running = false;

	private boolean polling = false;

	public void check() {
		if (polling) {
			//we need to protect from multiple thread execution because inbox is getting locked
			return;
		}
		try {
			polling = true;
			mailPollHandler.onPollStart();
			doCheck();
		} finally {
			polling = false;
			mailPollHandler.onPollEnd();
		}
	}

	private void doCheck() {
		Store store;
		try {
			log.debug("Opening store " + getProtocol());
			store = getStore(getSession());
			store.connect(getHost(), getPort(), getUsername(), getPassword());
		} catch (AuthenticationFailedException ax) {
			mailPollHandler.onPollException(ax);
			throw new MailAuthenticationException(ax);
		} catch (MessagingException mx) {
			mailPollHandler.onPollException(mx);
			throw new MailPollException(mx);
		}
		Folder folder = null;
		try {
			log.debug("Opening folder " + getFolderName());
			folder = store.getFolder(getFolderName());
			folder.open(Folder.READ_WRITE);
			int count = folder.getMessageCount();
			if (count > 0) {
				log.debug("Found {} new messages in folder {} for {}", new Object[] { count,
						getFolderName(), getUsername() });
			}
			for (int i = 1; i <= count; i++) {
				Message message = folder.getMessage(i);
				log.debug("Fetching message {}/{} subject \"{}\" from {} size {}", new Object[] { i, count,
						message.getSubject(), message.getFrom(), message.getSize() });
				mailFetchHandler.handle(message);
				if (messageFlag != null) {
					log.debug("Flagging message after fetch");
					message.setFlag(messageFlag, true);
				}
			}
		} catch (MessagingException mx) {
			mailPollHandler.onPollException(mx);
			log.error("Failed to fetch messages from folder " + getFolderName(), mx);
		} finally {
			try {
				if (folder != null) {
					log.debug("Closing folder " + getFolderName());
					folder.close(expungeDeleted);
				}
			} catch (MessagingException ex) {
				throw new MailPollException("Failed to close folder after message fetching", ex);
			}
			try {
				log.debug("Closing store " + getProtocol());
				store.close();
			} catch (MessagingException ex) {
				throw new MailPollException("Failed to close store after message fetching", ex);
			}
		}
	}

	public void run() {
		if (startDelay > 0) {
			try {
				Thread.sleep(startDelay);
			} catch (InterruptedException ix) {
				log.warn("Mail poller thread have been interrupted while onstart sleep");
			}
		}
		while (isRunning()) {
			try {
				check();
			} catch (RejectedExecutionException rx) {
				log.error("Executor rejected to execute mail poller task", rx);
			} catch (Exception x) {
				log.error("Executor execution failed", x);
			}
			try {
				synchronized (this.lifecycleMonitor) {
					this.lifecycleMonitor.wait(pollDelay);
				}
			} catch (InterruptedException ix) {
				log.warn("Mail poller thread have been interrupted while onpoll wait");
			}
		}
	}

	public void afterPropertiesSet() throws Exception {

		if (this.taskExecutor == null) {
			this.taskExecutor = createDefaultTaskExecutor();
		}
		if (this.mailPollHandler == null) {
			mailPollHandler = createDefaultPollHandler();
		}

		synchronized (this.lifecycleMonitor) {
			this.running = true;
			this.lifecycleMonitor.notifyAll();
		}
		log.info("Starting Mail Poller Thread");
		taskExecutor.execute(this);
	}

	public void destroy() throws Exception {
		log.info("Stopping Mail Poller Thread");
		synchronized (this.lifecycleMonitor) {
			this.running = false;
			this.lifecycleMonitor.notifyAll();
		}
	}

	private MailPollHandler createDefaultPollHandler() {
		return new MailPollHandler() {

			public void onPollStart() {
				//nothing
			}

			public void onPollEnd() {
				//nothing
			}

			public void onPollException(MessagingException mx) {
				//nothing
			}

		};
	}

	protected TaskExecutor createDefaultTaskExecutor() {
		String beanName = getBeanName();
		String threadNamePrefix = (beanName != null ? beanName + "-" : DEFAULT_THREAD_NAME_PREFIX);
		return new SimpleAsyncTaskExecutor(threadNamePrefix);
	}

	public final boolean isRunning() {
		synchronized (this.lifecycleMonitor) {
			return (this.running);
		}
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	protected final String getBeanName() {
		return this.beanName;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public void setJavaMailProperties(Properties javaMailProperties) {
		this.javaMailProperties = javaMailProperties;
		synchronized (this) {
			this.session = null;
		}
	}

	public Properties getJavaMailProperties() {
		return this.javaMailProperties;
	}

	public synchronized void setSession(Session session) {
		Assert.notNull(session, "Session must not be null");
		this.session = session;
	}

	public synchronized Session getSession() {
		if (this.session == null) {
			this.session = Session.getInstance(this.javaMailProperties);
		}
		return this.session;
	}

	protected Store getStore(Session session) throws NoSuchProviderException {
		return session.getStore(getProtocol());
	}

	public MailFetchHandler getMailFetchHandler() {
		return mailFetchHandler;
	}

	public void setMailFetchHandler(MailFetchHandler messageHandler) {
		this.mailFetchHandler = messageHandler;
	}

	public Flags.Flag getMessageFlag() {
		return messageFlag;
	}

	public void setMessageFlag(Flags.Flag messageFlag) {
		this.messageFlag = messageFlag;
	}

	public Executor getTaskExecutor() {
		return taskExecutor;
	}

	public void setTaskExecutor(Executor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public MailPollHandler getMailPollHandler() {
		return mailPollHandler;
	}

	public void setMailPollHandler(MailPollHandler mailPollHandler) {
		this.mailPollHandler = mailPollHandler;
	}

	public int getStartDelay() {
		return startDelay;
	}

	public void setStartDelay(int startDelay) {
		this.startDelay = startDelay;
	}

	public int getPollDelay() {
		return pollDelay;
	}

	public void setPollDelay(int pollDelay) {
		this.pollDelay = pollDelay;
	}

	public boolean getExpungeDeleted() {
		return expungeDeleted;
	}

	public void setExpungeDeleted(boolean expunge) {
		this.expungeDeleted = expunge;
	}

}
