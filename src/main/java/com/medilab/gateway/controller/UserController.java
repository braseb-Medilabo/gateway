package com.medilab.gateway.controller;

import java.util.Collections;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medilab.gateway.entity.UserDto;
import com.medilab.gateway.security.AuthService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("${api.prefix}")
public class UserController {
    
    private AuthService authService;
        
    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<UserDto>> getCurrentUser(@AuthenticationPrincipal String username){
        
        return authService.getCurrentUser(username)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity
                                        .status(HttpStatus.UNAUTHORIZED)
                                        .body(new UserDto("Not authentificated", Collections.emptyList())))
                    );
    }
}
