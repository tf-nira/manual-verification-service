package in.tf.nira.manual.verification.service;

import in.tf.nira.manual.verification.dto.AuthenticationRequest;
import in.tf.nira.manual.verification.dto.AuthenticationResponse;
import in.tf.nira.manual.verification.dto.UserResponse;
import io.mosip.kernel.core.http.RequestWrapper;

public interface AuthService {
    AuthenticationResponse loginClient (RequestWrapper<AuthenticationRequest> authRequest);
    //UserResponse fetchUsersByRole (String role);
}
