package com.medilab.gateway.configuration;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS).permitAll() // autorise les preflight CORS
                        .pathMatchers("/login", "/logout").permitAll()
                        .pathMatchers(HttpMethod.GET, "/patient/**")
                            .hasAnyRole("ADMIN", "USER")
                        .pathMatchers(HttpMethod.POST, "/patient/**")
                            .hasAnyRole("ADMIN")
                            .pathMatchers(HttpMethod.PUT, "/patient/**")
                            .hasAnyRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/patient/**")
                            .hasAnyRole("ADMIN")
                        .pathMatchers("/patient/note/**")
                            .hasAnyRole("ADMIN", "USER")
                        .anyExchange().authenticated()
                )
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((exchange, ex) -> 
                            Mono.fromRunnable(() -> exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED))
                        )
                    )
                .build();
    }
    
    @Bean
    MapReactiveUserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
       UserDetails organisateur = User.builder()
                                   .username("organisateur")
                                   .passwordEncoder(passwordEncoder::encode)
                                   .password("organisateur")
                                   .roles("ADMIN")
                                   .build();
       UserDetails praticien = User.builder()
               .username("praticien")
               .passwordEncoder(passwordEncoder::encode)
               .password("praticien")
               .roles("USER")
               .build();
       
       return new MapReactiveUserDetailsService(organisateur, praticien);
                                   
    }
    
    
    @Bean 
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    ReactiveAuthenticationManager authenticationManager(ReactiveUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {

        UserDetailsRepositoryReactiveAuthenticationManager manager =
                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);

       manager.setPasswordEncoder(passwordEncoder);

        return manager;
    }
}
