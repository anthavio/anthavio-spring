package net.anthavio.spring.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestAnnotationConfig {

	@Bean
	public String secret() {
		return "Secret!!!";
	}
}
