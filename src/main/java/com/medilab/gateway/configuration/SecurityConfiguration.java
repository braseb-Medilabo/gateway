package com.medilab.gateway.configuration;




import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
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
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    private final CorsConfigurationSource corsConfigurationSource;

    SecurityConfiguration(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

        return http
                .cors(cors -> {cors.configurationSource(corsConfigurationSource);}) // active CORS dans Security
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
                        .authenticationEntryPoint((exchange, ex) -> {
                                ServerHttpResponse response = exchange.getResponse();
                                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                                String body = "{\"message\":\"UNAUTHORIZED\"}";
                                DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
                                
                                return response.writeWith(Mono.just(buffer));
                                
                            }
                        )
                        .accessDeniedHandler((exchange, ex) -> {
                            ServerHttpResponse response = exchange.getResponse();
                            response.setStatusCode(HttpStatus.FORBIDDEN);
                            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                            String body = "{\"message\":\"ACCES DENIED\"}";
                            DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
                            
                            return response.writeWith(Mono.just(buffer));
                        })
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
