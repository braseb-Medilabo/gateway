package com.medilab.gateway.security;


import java.util.Collection;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@Component
public class JwtAuthentificationFilter implements WebFilter {
    
    private static final String BEARER_PREFIX = "Bearer ";
    
    private JwtService jwtService;
    
    public JwtAuthentificationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String token = extractToken(exchange.getRequest());

        if (token == null) {
            return chain.filter(exchange);
        }

        return jwtService.validateToken(token)
                .flatMap(valid -> {

                    if (!valid) {
                        return chain.filter(exchange);
                    }
                    String userName = jwtService.extractUsername(token);

                    Collection<GrantedAuthority> roles = jwtService.extractRoles(token);

                    Authentication auth = new UsernamePasswordAuthenticationToken(
                                                                    userName,
                                                                    null,
                                                                    roles
                                                                    //List.of(new SimpleGrantedAuthority(role))
                                                                    );
                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth)
                            );
                });
    }
    
    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }

}
