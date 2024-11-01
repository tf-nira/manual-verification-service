package io.mosip.manualVerificationService.service;

import io.mosip.manualVerificationService.dto.AuthenticationRequest;
import io.mosip.manualVerificationService.dto.AuthenticationResponse;
import io.mosip.manualVerificationService.dto.UserResponse;

public interface AuthService {

    AuthenticationResponse loginClient (AuthenticationRequest authRequest) ;

    UserResponse fetchUsersByRole (String role) ;
}
