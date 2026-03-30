package com.medilab.gateway.controller;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.WebSession;

import reactor.core.publisher.Mono;

@RestController
public class LoginController {

    @PostMapping("/login")
    public Mono<Void> login(@RequestBody Map<String,String> creds, WebSession session) {
        String username = creds.get("ident");
        String password = creds.get("pass");

        if (checkUserPassword(username, password)) {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
            SecurityContextImpl securityContext = new SecurityContextImpl(auth);

            // Stocke dans la session Spring Security
            session.getAttributes().put("SPRING_SECURITY_CONTEXT", securityContext);
            
            return Mono.empty(); // 200 OK
        }

        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    
    private boolean checkUserPassword(String username, String password) {
        return "user".equals(username) && "user".equals(password);
    }
    
    @PostMapping("/logout")
    public Mono<Void> logout(WebSession session) {
        session.invalidate(); // supprime la session côté serveur
        return Mono.empty(); // 200 OK
    }
}

