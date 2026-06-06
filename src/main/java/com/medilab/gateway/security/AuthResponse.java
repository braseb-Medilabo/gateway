package com.medilab.gateway.security;


public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String error;
    
    public AuthResponse(String accessToken, String refreshToken, String error) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.error = error;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getError() {
        return error;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
    
    
}
