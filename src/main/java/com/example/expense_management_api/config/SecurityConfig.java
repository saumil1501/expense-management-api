package com.example.expense_management_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.expense_management_api.security.JwtAuthenticationFilter;
import com.example.expense_management_api.security.SecurityErrorHandler;

@Configuration
public class SecurityConfig {
	
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	
	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityErrorHandler securityErrors) throws Exception {
		http.csrf(csrf -> csrf.disable())
		.sessionManagement(session -> session.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS
        ))
		.authorizeHttpRequests(auth -> auth
			    .requestMatchers(
			        "/api/auth/**",
			        "/swagger-ui/**",
			        "/swagger-ui.html",
			        "/v3/api-docs/**"
			    ).permitAll()
			    .anyRequest().authenticated()
			)
		.exceptionHandling(errors -> errors
				.authenticationEntryPoint(securityErrors)
				.accessDeniedHandler(securityErrors))
		.addFilterBefore(jwtAuthenticationFilter,UsernamePasswordAuthenticationFilter.class);

		return http.build();
		
	}

}
