package com.medilab.gateway.exception;

public class CredentialException extends RuntimeException {
    
    private static final long serialVersionUID = -4090679069144017577L;
    
    
    private final String message;
    
    
    public CredentialException(String message) {
        this.message = message;
    }



    @Override
    public String getMessage() {
        return message;
    }

    
   

    

}
