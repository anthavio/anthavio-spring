package net.anthavio.spring.test;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import net.anthavio.spring.test.ContextRefLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.annotations.Test;


@ContextConfiguration(locations = { "example-child-context", "classpath:spring/spring-example-ref-ctx.xml" }, loader = ContextRefLoader.class)
public class TestContextLoaderTest extends AbstractTestNGSpringContextTests {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Inject
	@Named("BlahBlah")
	private String blah;

	@Inject
	private DataSource dataSource;

	@Inject
	private PlatformTransactionManager tm;

	@Test
	public void test() {
		TransactionTemplate tt = new TransactionTemplate(tm);
		tt.afterPropertiesSet();
		//tt.setName("xxxx");
		tt.execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				log.info("Heeereee1");
				boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
				String txName = TransactionSynchronizationManager.getCurrentTransactionName();
				System.out.println(txName + " " + txActive);
				JdbcTemplate jdbc = new JdbcTemplate(dataSource);
				jdbc.execute("SELECT * FROM DUAL");
			}
		});
		log.info("Heeereee2");
		assertThat(this.blah).isEqualTo("BlahText");
	}
}
