package com.medilab.gateway.security;

import java.util.List;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.medilab.gateway.entity.CredentialDto;
import com.medilab.gateway.entity.UserDto;
import com.medilab.gateway.exception.CredentialException;
import com.medilab.gateway.exception.InvalidRefreshTokenException;


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
    
    public Mono<AuthResponse> authenticate(CredentialDto creds){
        String username = creds.username();
        String password = creds.password();
                  
        return mapReactiveUserDetailsService.findByUsername(username)
                .switchIfEmpty(Mono.error(new CredentialException("No autorisation")))
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .switchIfEmpty(Mono.error(new CredentialException("Bad credentials")))
                .map(user -> {
                    String accestoken = jwtService.createToken(user);
                    String refreshToken = jwtService.createRefreshToken(user);
                    
                    return new AuthResponse(accestoken, refreshToken, null);
                            }
                );
                
    }
    
    public Mono<AuthResponse> refreshToken(RefreshTokenDto refreshToken){
        String valueRefreshToken = refreshToken.refreshToken();
                                
        return jwtService.isRefreshToken(valueRefreshToken)
                .filter(isRefresh -> isRefresh)
                .switchIfEmpty(Mono.error(new InvalidRefreshTokenException("Invalid refresh token")))
                .map(isRefresh -> jwtService.extractUsername(valueRefreshToken))
                .flatMap(user -> mapReactiveUserDetailsService.findByUsername(user))
                .switchIfEmpty(Mono.error(new CredentialException("No autorisation")))
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
        return mapReactiveUserDetailsService.findByUsername(username)
                                        
                .map(u -> { List<String> roles = u.getAuthorities().stream()
                                                    .map(a -> a.getAuthority().replaceFirst("^ROLE_", ""))
                                                    .toList();
                                                    
                            return new UserDto(u.getUsername(), roles);});
                
                
    }
    
}


