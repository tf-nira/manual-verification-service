package io.mosip.manualVerificationService.controller;

import io.mosip.manualVerificationService.dto.AuthenticationRequest;
import io.mosip.manualVerificationService.dto.AuthenticationResponse;
import io.mosip.manualVerificationService.dto.UserResponse;
import io.mosip.manualVerificationService.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @RequestMapping(value = "/login")
    public AuthenticationResponse authenticate(@Valid @RequestBody AuthenticationRequest authRequest) {
        return authService.loginClient(authRequest);
    }

    @RequestMapping(value = "/userdetails")
    public ResponseEntity<UserResponse> fetchUsersByRole (@RequestParam String role) {
        UserResponse userResponse = authService.fetchUsersByRole(role);
        return ResponseEntity.ok(userResponse);
    }

}
