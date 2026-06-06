package com.medilab.gateway.exception;

public class InvalidRefreshTokenException extends RuntimeException {
        
    private static final long serialVersionUID = -7639396463020551575L;
    private final String message;
    
    
    public InvalidRefreshTokenException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    
   

    

}
