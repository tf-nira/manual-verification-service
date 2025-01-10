package in.tf.nira.manual.verification.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
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
    public ResponseWrapper<AuthenticationResponse> authenticate(@Valid @RequestBody RequestWrapper<AuthenticationRequest> authRequest, HttpServletResponse httpServletResponse) {
    	ResponseWrapper<AuthenticationResponse> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setId("");
		responseWrapper.setVersion("");
        AuthenticationResponse authenticationResponse = authService.loginClient(authRequest);

        String token = authenticationResponse.getToken();
        Cookie authCookie = new Cookie("Authorization", token);
        authCookie.setHttpOnly(true);
        authCookie.setSecure(true);
        authCookie.setPath("/");
        authCookie.setMaxAge(authenticationResponse.getExpiryTime()); // Sets the cookie expiration time
        httpServletResponse.addCookie(authCookie);

        responseWrapper.setResponse(authenticationResponse);
        return responseWrapper;
    }
}
