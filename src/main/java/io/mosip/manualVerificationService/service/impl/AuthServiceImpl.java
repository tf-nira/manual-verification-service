package io.mosip.manualVerificationService.service.impl;

import io.mosip.manualVerificationService.dto.*;
import io.mosip.manualVerificationService.service.AuthService;
import io.mosip.manualVerificationService.service.AuthTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;


@Service
public class AuthServiceImpl implements AuthService {

    @Value("${io.mosip.manualVerificationService.authUrl}")
    private String authenticationUrl;

    @Value("${io.mosip.manualVerificationService.getUsersUrl}")
    private String fetchUsersUrl;

    @Autowired
    private AuthTokenManager authTokenManager;

    @Override
    public AuthenticationResponse loginClient(AuthenticationRequest authRequest) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        HttpEntity<AuthenticationRequest> entity = new HttpEntity<>(authRequest, headers);

        ResponseEntity<AuthenticationResponse> response = restTemplate.exchange(
                authenticationUrl,
                HttpMethod.POST,
                entity,
                AuthenticationResponse.class
        );

        authTokenManager.setToken(Objects.requireNonNull(response.getBody()).getAuthToken());

        return response.getBody();
    }

    @Override
    public UserResponse fetchUsersByRole(String role) {

        RestTemplate restTemplate = new RestTemplate();

        String url = String.format(fetchUsersUrl + "?roleName=" + role);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Cookie", "Authorization=" + authTokenManager.getToken());

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<UserResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                UserResponse.class
        );

        return response.getBody();

    }
}