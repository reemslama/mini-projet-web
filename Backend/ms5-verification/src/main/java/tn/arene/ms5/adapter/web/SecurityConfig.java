package tn.arene.ms5.adapter.web;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * ADR-003 : validation JWT locale. Le mode « open » sert uniquement le démarrage local S5 ;
 * en production, les rôles Keycloak sont convertis en autorités ROLE_* (matrice § 11.1).
 */
@Configuration
public class SecurityConfig {

	@Bean
	@ConditionalOnProperty(name = "ms5.security.enabled", havingValue = "false", matchIfMissing = true)
	public SecurityFilterChain openFilterChain(HttpSecurity http) throws Exception {
		return http.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
				.build();
	}

	@Bean
	@ConditionalOnProperty(name = "ms5.security.enabled", havingValue = "true")
	public SecurityFilterChain securedFilterChain(HttpSecurity http,
			JwtAuthenticationConverter converter) throws Exception {
		return http.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**")
						.permitAll()
						.anyRequest().authenticated())
				.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))
				.build();
	}

	@Bean
	@ConditionalOnProperty(name = "ms5.security.enabled", havingValue = "true")
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(this::authorities);
		return converter;
	}

	@Bean
	@ConditionalOnProperty(name = "ms5.security.enabled", havingValue = "true")
	public JwtDecoder jwtDecoder(@Value("${ms5.jwk-set-uri}") String jwkSetUri) {
		return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
	}

	@SuppressWarnings("unchecked")
	private Collection<GrantedAuthority> authorities(Jwt jwt) {
		Object claim = jwt.getClaims().get("realm_access");
		if (!(claim instanceof Map<?, ?> realmAccess) || !(realmAccess.get("roles") instanceof List<?> roles)) {
			return List.of();
		}
		return roles.stream().filter(String.class::isInstance).map(role -> new SimpleGrantedAuthority("ROLE_" + role))
				.map(GrantedAuthority.class::cast).toList();
	}
}
