package com.medilab.gateway.security;


public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String message;
    
    public AuthResponse(String accessToken, String refreshToken, String message) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.message = message;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getMessage() {
        return message;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
    
    
}
