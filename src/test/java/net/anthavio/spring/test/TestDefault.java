package net.anthavio.spring.test;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

/**
 * 
 * @author martin.vanek
 *
 */
@ContextConfiguration(loader = ContextRefLoader.class, locations = "test-xml-context")
public class TestDefault extends AbstractTestNGSpringContextTests {

	@Inject
	@Named("secret")
	private String secret;

	@Test
	public void test() {
		//FIXME secret is not injected  
		//Assertions.assertThat(this.secret).isEqualTo("SECRET!");
	}
}
