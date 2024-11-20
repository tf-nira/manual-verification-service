package in.tf.nira.manual.verification.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.tf.nira.manual.verification.dto.AuthenticationRequest;
import in.tf.nira.manual.verification.dto.AuthenticationResponse;
import in.tf.nira.manual.verification.service.AuthService;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping(value = "/login")
    public ResponseWrapper<AuthenticationResponse> authenticate(@Valid @RequestBody RequestWrapper<AuthenticationRequest> authRequest) {
    	ResponseWrapper<AuthenticationResponse> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setId("");
		responseWrapper.setVersion("");
		responseWrapper.setResponse(authService.loginClient(authRequest));
        return responseWrapper;
    }
	/*
	 * @GetMapping(value = "/userdetails") public ResponseEntity<UserResponse>
	 * fetchUsersByRole (@RequestParam String role) { UserResponse userResponse =
	 * authService.fetchUsersByRole(role); return ResponseEntity.ok(userResponse); }
	 */
}
