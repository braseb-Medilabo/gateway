package com.medilab.gateway.security;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

@Service
public class JwtService {
    
    private final SecretKey key;
    
    /**
     * Constructs a JwtService using the secret key from application properties.
     *
     * @param jwtSecret the secret key used for signing JWT tokens
     */
    
    public JwtService(@Value("${jwt.secret}") String jwtSecret) {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    /**
     * Creates a JWT token for the given user.
     *
     * <p>The token includes the username as subject and the role as a claim prefixed with "ROLE_".
     * The token is signed using HS256 algorithm and expires in 10 minutes.</p>
     *
     * @param user the user for whom the token is created
     * @return a signed JWT token as a String
     */
    
    public String createToken(UserDetails user) {
        Date now = new Date();
                
        System.out.println("Create token for user " + user.getUsername() + " with role : " + user.getAuthorities());

        return Jwts.builder()
                //.claim("role", "ROLE_" + user.getAuthorities())
                .claim("roles", user.getAuthorities()
                                .stream()
                                .map(GrantedAuthority::getAuthority)
                                .toList())
                .subject(user.getUsername())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 60 * 10 * 1000))
                //.expiration(new Date(now.getTime() + 10 * 1000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
                
    }
    
    /**
     * Extracts the username (subject) from the given JWT token.
     *
     * @param token the JWT token
     * @return the username contained in the token
     */
    
    public String extractUsername(String token) {
        
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
                          
        
    }
    
    public String createRefreshToken(UserDetails user) {
        
        Date now = new Date();
        
        System.out.println("Create refresh token for user " + user.getUsername() + " with role : " + user.getAuthorities());

        return Jwts.builder()
                //.claim("role", "ROLE_" + user.getAuthorities())
                .claim("type", "refresh")
                .subject(user.getUsername())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 6000 * 10 * 1000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
                      
    
    }
    
    public Mono<Boolean> isRefreshToken(String token) {
            
        return Mono.fromCallable(() -> {
                    
                   return Jwts.parser()
                            .verifyWith(key)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload()
                            .getOrDefault("type", "").equals("refresh");

                });
    }
    
    /**
     * Extracts the role claim from the given JWT token.
     *
     * @param token the JWT token
     * @return the role stored in the token, or null if not present
     */
    
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority>  extractRoles(String token){
        System.out.println("extract roles : " + token);
            /*return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            //.get("role", String.class);*/
        List<String> roles = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("roles", List.class);

        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
            
                                        
                                    
    }
    
    /**
     * Validates the given JWT token by verifying its signature and structure.
     *
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    
    public Mono<Boolean> validateToken(String token) {

        System.out.println("validation token : " + token);
        return Mono.fromCallable(() -> {
            
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            return true;

        }).onErrorReturn(false);
    }
}
