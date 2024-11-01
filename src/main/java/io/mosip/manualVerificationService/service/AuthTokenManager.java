package io.mosip.manualVerificationService.service;

import org.springframework.stereotype.Component;

@Component
public class AuthTokenManager {
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
