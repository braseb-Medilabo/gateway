package com.medilab.gateway.exception;



import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /*@ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleValidation(AccessDeniedException ex){
               
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
    }*/
    
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<?> handleValidation(WebExchangeBindException ex){
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getFieldErrors()
                               .forEach(err-> errors.put(err.getField(), err.getDefaultMessage()));
        
        
        return ResponseEntity.badRequest().body(Map.of(
                "message", "Validation credential failed",
                "errors", errors));
    }
    
    @ExceptionHandler(CredentialException.class)
    public ResponseEntity<?> handleCredentialException(CredentialException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }
    
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handlerException(Exception ex){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Internal error"));
    }
    
    
}
