package com.enlice.challenges.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

/**
 * Mappe les rôles Keycloak ({@code realm_access.roles} et {@code resource_access.*.roles})
 * vers des autorités Spring Security {@code ROLE_<role>}.
 */
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter delegate = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>(delegate.convert(jwt));
        Set<String> existing = new LinkedHashSet<>();
        authorities.forEach(a -> existing.add(a.getAuthority()));
        for (String role : extractRoles(jwt)) {
            String authority = "ROLE_" + role;
            if (existing.add(authority)) {
                authorities.add(new SimpleGrantedAuthority(authority));
            }
        }
        String principalName = jwt.getClaimAsString("preferred_username");
        if (principalName == null || principalName.isBlank()) {
            principalName = jwt.getSubject();
        }
        return new JwtAuthenticationToken(jwt, authorities, principalName);
    }

    /** Extrait les rôles Keycloak, sans doublon. */
    public static List<String> extractRoles(Jwt jwt) {
        Set<String> roles = new LinkedHashSet<>();
        Object realmAccess = jwt.getClaim("realm_access");
        if (realmAccess instanceof Map<?, ?> realm) {
            collectRoles(realm.get("roles"), roles);
        }
        Object resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess instanceof Map<?, ?> clients) {
            for (Object value : clients.values()) {
                if (value instanceof Map<?, ?> client) {
                    collectRoles(client.get("roles"), roles);
                }
            }
        }
        return new ArrayList<>(roles);
    }

    private static void collectRoles(Object rawRoles, Set<String> target) {
        if (rawRoles instanceof Collection<?> collection) {
            for (Object role : collection) {
                if (role != null && !String.valueOf(role).isBlank()) {
                    target.add(String.valueOf(role));
                }
            }
        }
    }
}