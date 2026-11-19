package ir.expense.tracker.makharej.security;


import ir.expense.tracker.makharej.dto.filter.AuthenticationTokenFilter;
import ir.expense.tracker.makharej.service.UserDetailServiceImpl;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
public class SecurityConfig {

	@Autowired
	UserDetailServiceImpl userDetailsService;

	@Autowired
	private CustomeAuthPoint unauthorizedHandler;
	@Bean
	public AuthenticationTokenFilter authenticationJwtTokenFilter() {
		return new AuthenticationTokenFilter();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.cors(Customizer.withDefaults())
			.csrf(AbstractHttpConfigurer::disable)
			.exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(unauthorizedHandler))
			.sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorize -> authorize
				// Spring Security 6 authorizes the container's ERROR dispatch too, which would turn every error page
				// (404, ...) into a 401. Spring Boot 2.7 let them through, except for 401s, which stay an empty body.
				// Direct calls to /error are still protected.
				.requestMatchers(SecurityConfig::isErrorDispatchOtherThanUnauthorized).permitAll()
				.requestMatchers("/api/auth/**").permitAll()
				.requestMatchers("/actuator/**").permitAll()
				.requestMatchers("/v3/api-docs",
						 "/v3/api-docs/**",
						 "/swagger-ui.html",
						 "/swagger-ui/**",
						 "/webjars/**").permitAll()
				.anyRequest().authenticated()
			)
		;

		http.authenticationProvider(authenticationProvider());
		http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	private static boolean isErrorDispatchOtherThanUnauthorized(HttpServletRequest request) {
		return request.getDispatcherType() == DispatcherType.ERROR
				&& !Integer.valueOf(HttpServletResponse.SC_UNAUTHORIZED).equals(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE));
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);

		authProvider.setPasswordEncoder(passwordEncoder());

		return authProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
