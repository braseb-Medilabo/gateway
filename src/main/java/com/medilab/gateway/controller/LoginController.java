package com.medilab.gateway.controller;

import java.nio.file.AccessDeniedException;
import java.util.Collection;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import org.springframework.web.bind.annotation.RequestMapping;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("${api.prefix}")
public class LoginController {

    private final ReactiveAuthenticationManager authenticationManager;
    private final ServerSecurityContextRepository securityContextRepository = new WebSessionServerSecurityContextRepository();
    

    LoginController(ReactiveAuthenticationManager authenticationManager, MapReactiveUserDetailsService mapReactiveUserDetailsService) {
        this.authenticationManager = authenticationManager;
        
    }
    
    


    @PostMapping("/login")
    public Mono<ResponseEntity<Object>> login(@RequestBody Map<String,String> creds, ServerWebExchange webExchange) {
        String username = creds.get("ident");
        String password = creds.get("pass");
       
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, password);

        return authenticationManager.authenticate(auth)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED)))
                
                .flatMap(a -> {
                       SecurityContextImpl securityContext = new SecurityContextImpl(a);
                       Collection<? extends GrantedAuthority> authorities = a.getAuthorities();
                       Object principal = a.getPrincipal();
                       System.out.println("principal : " + principal);
                       System.out.println("authorities : " + authorities);
                       return securityContextRepository.save(webExchange, securityContext)
                               .thenReturn(ResponseEntity.ok().build());
                       
                                              
                       // Stocke dans la session Spring Security
                       //session.getAttributes().put("SPRING_SECURITY_CONTEXT", securityContext);
                       //return ResponseEntity.ok().build();
                   })
                .onErrorResume(AccessDeniedException.class, e -> 
                Mono.just(
                        ResponseEntity
                            .status(HttpStatus.UNAUTHORIZED)
                            .body((Object) Map.of("error", "No autorisation"))
                    )
                )
                .onErrorResume(AuthenticationException.class, e ->
                Mono.just(
                        ResponseEntity
                            .status(HttpStatus.UNAUTHORIZED)
                            .body((Object) Map.of("error", "Bad credentials"))
                    )
                );
                   
    }
  
    
    @PostMapping("/logout")
    public Mono<Void> logout(WebSession session) {
        session.invalidate(); // supprime la session côté serveur
        return Mono.empty(); // 200 OK
    }
}

