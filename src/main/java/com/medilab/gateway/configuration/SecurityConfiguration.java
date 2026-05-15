package com.medilab.gateway.configuration;




import org.springframework.beans.factory.annotation.Value;
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
    
    @Value("${api.prefix}")
    private String apiPrefix;

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
                        .pathMatchers(  apiPrefix + "/login", 
                                        apiPrefix + "/logout",
                                        // Swagger UI gateway
                                        "/swagger-ui/**",
                                        //"/webjars/**",
                                        "/doc",
        
                                        // OpenAPI docs gateway
                                        apiPrefix + "/patient/v3/api-docs",
                                        apiPrefix + "/patient/note/v3/api-docs",
                                        apiPrefix + "/patient/risk/v3/api-docs",
        
                                        // swagger config
                                        "/v3/api-docs/swagger-config").permitAll()
                        .pathMatchers(HttpMethod.GET, apiPrefix + "/patient/**")
                            .hasAnyRole("ADMIN", "USER")
                        .pathMatchers(HttpMethod.POST, apiPrefix + "/patient/**")
                            .hasAnyRole("ADMIN")
                            .pathMatchers(HttpMethod.PUT, apiPrefix + "/patient/**")
                            .hasAnyRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, apiPrefix + "/patient/**")
                            .hasAnyRole("ADMIN")
                        .pathMatchers(apiPrefix + "/patient/note/**")
                            .hasAnyRole("ADMIN", "USER")
                        .pathMatchers(apiPrefix + "/patient/risk/**")
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
