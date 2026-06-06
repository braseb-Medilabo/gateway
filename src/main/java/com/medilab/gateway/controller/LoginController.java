package com.medilab.gateway.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebSession;
import com.medilab.gateway.entity.CredentialDto;
import com.medilab.gateway.security.AuthResponse;
import com.medilab.gateway.security.AuthService;
import com.medilab.gateway.security.RefreshTokenDto;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestMapping;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("${api.prefix}" +  "/auth")
public class LoginController {
    
    private AuthService authService;
    
    public LoginController(AuthService authService) {
        this.authService = authService;
    }
   

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody @Valid CredentialDto creds) {
        return authService.authenticate(creds)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity
                                                .status(HttpStatus.UNAUTHORIZED)
                                                   .body(new AuthResponse(null, null,  "Authenticate error"))
                                               )
                                );
    }
    
    @PostMapping("/refresh")
    public Mono<ResponseEntity<AuthResponse>> refresh(@RequestBody @Valid RefreshTokenDto refreshToken){
        return authService.refreshToken(refreshToken)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity
                                                .status(HttpStatus.UNAUTHORIZED)
                                                   .body(new AuthResponse(null, null, e.getMessage()))
                                               )
                                );
    }
  
    
    @PostMapping("/logout")
    public Mono<Void> logout(WebSession session) {
        session.invalidate(); // supprime la session côté serveur
        return Mono.empty(); // 200 OK
    }
    
    
}

