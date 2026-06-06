package com.medilab.gateway.security;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.medilab.gateway.entity.UserDto;

import reactor.core.publisher.Mono;


@Service
public class AuthService {
    
    private MapReactiveUserDetailsService mapReactiveUserDetailsService;
    private JwtService jwtService;
    private PasswordEncoder passwordEncoder;
    
    public AuthService(MapReactiveUserDetailsService mapReactiveUserDetailsService,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.mapReactiveUserDetailsService = mapReactiveUserDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    
    public Mono<AuthResponse> authenticate(Map<String,String> creds){
        String username = creds.get("ident");
        String password = creds.get("pass");
        
        
        
        return mapReactiveUserDetailsService.findByUsername(username)
                .switchIfEmpty(Mono.error(new RuntimeException("No autorisation")))
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .switchIfEmpty(Mono.error(new RuntimeException("Bad credentials")))
                .map(user -> {
                    String accestoken = jwtService.createToken(user);
                    String refreshToken = jwtService.createRefreshToken(user);
                    
                    return new AuthResponse(accestoken, refreshToken, null);
                            }
                );
                
    }
    
    public Mono<AuthResponse> refreshToken(Map<String, String> refreshToken){
        String valueRefreshToken = refreshToken.get("refreshToken");
        System.out.println(valueRefreshToken);
                        
        return jwtService.isRefreshToken(valueRefreshToken)
                .filter(isRefresh -> isRefresh)
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid refresh token")))
                .map(isRefresh -> jwtService.extractUsername(valueRefreshToken))
                .flatMap(user -> mapReactiveUserDetailsService.findByUsername(user))
                .switchIfEmpty(Mono.error(new RuntimeException("No autorisation")))
                .map(user -> {
                    String accesToken = jwtService.createToken(user);
                    System.out.println(accesToken);
                    String newRefreshToken = jwtService.createRefreshToken(user);
                    System.out.println(newRefreshToken);
                    
                    return new AuthResponse(accesToken, newRefreshToken, null);
                            }
                );
    }
    
    public Mono<UserDto> getCurrentUser(String username){
        System.out.println("getCurrentUser");
        return mapReactiveUserDetailsService.findByUsername(username)
                                        
                //.switchIfEmpty(Mono.error(new RuntimeException("User not found: " + username)))
                .map(u -> { List<String> roles = u.getAuthorities().stream()
                                                    .map(a -> a.getAuthority().replaceFirst("^ROLE_", ""))
                                                    .toList();
                                                    
                            return new UserDto(u.getUsername(), roles);});
                
                
    }
    
}


