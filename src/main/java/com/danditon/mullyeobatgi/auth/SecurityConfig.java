package com.danditon.mullyeobatgi.auth;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, TokenService tokenService,
			ObjectProvider<ClientRegistrationRepository> clientRegistrations,
			KakaoLoginSuccessHandler kakaoLoginSuccessHandler,
			KakaoLoginFailureHandler kakaoLoginFailureHandler) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults())
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/auth/**", "/actuator/health/**", "/actuator/info",
								"/oauth2/**", "/login/oauth2/**", "/error",
								"/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
						.requestMatchers("/api/**").authenticated()
						.anyRequest().permitAll())
				.exceptionHandling(e -> e.authenticationEntryPoint((request, response, ex) -> {
					response.setStatus(HttpStatus.UNAUTHORIZED.value());
					response.setContentType(MediaType.APPLICATION_JSON_VALUE);
					response.setCharacterEncoding(StandardCharsets.UTF_8.name());
					response.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"로그인이 필요해요\"}");
				}))
				.addFilterBefore(new TokenAuthenticationFilter(tokenService), UsernamePasswordAuthenticationFilter.class);

		// 카카오 키가 설정된 kakao 프로필에서만 카카오 로그인을 켠다
		if (clientRegistrations.getIfAvailable() != null) {
			http.oauth2Login(oauth -> oauth
					.successHandler(kakaoLoginSuccessHandler)
					.failureHandler(kakaoLoginFailureHandler));
		}
		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins}") List<String> origins) {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(origins);
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

}
