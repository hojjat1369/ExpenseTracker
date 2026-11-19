package ir.expense.tracker.makharej;


import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.yaml.MappingJackson2YamlHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;


/**
 * Keeps the request handling behaviour of the Spring Boot 2.7 / Spring 5.3 version of the application.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	/**
	 * Spring 6 no longer maps "/api/category/" to the handler of "/api/category". It used to.
	 */
	@Override
	@SuppressWarnings("deprecation")
	public void configurePathMatch(PathMatchConfigurer configurer) {

		configurer.setUseTrailingSlashMatch(true);
	}

	/**
	 * Spring 6.2 registers a YAML message converter as soon as jackson-dataformat-yaml is on the classpath
	 * (springdoc brings it). The API only ever spoke JSON, so do not start accepting/producing application/yaml.
	 */
	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {

		converters.removeIf(converter -> converter instanceof MappingJackson2YamlHttpMessageConverter);
	}
}
