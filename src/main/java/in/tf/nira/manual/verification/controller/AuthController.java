package in.tf.nira.manual.verification.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${mosip.security.secure-cookie:false}")
    private boolean isSecureCookie;

    @Autowired
    AuthService authService;

    private Cookie createCookie(final String content, final int expirationTimeSeconds) {
        final Cookie cookie = new Cookie("Authorization", content);
        cookie.setMaxAge(expirationTimeSeconds);
        cookie.setHttpOnly(true);
        cookie.setSecure(isSecureCookie);
        cookie.setPath("/");
        return cookie;
    }

    @PostMapping(value = "/login")
    public ResponseWrapper<AuthenticationResponse> authenticate(@Valid @RequestBody RequestWrapper<AuthenticationRequest> authRequest, HttpServletResponse httpServletResponse) {
    	ResponseWrapper<AuthenticationResponse> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setId("");
		responseWrapper.setVersion("");
        AuthenticationResponse authenticationResponse = authService.loginClient(authRequest);

        String token = authenticationResponse.getToken();
        String cookieValue = String.format("Authorization=%s; SameSite=None: Secure; Path=/", token);
//        String cookieValue = String.format("Authorization=%s; Domain=mvs.niradev.idencode.link", token);
        Cookie cookie = createCookie(token, 7 * 24 * 60 * 60);

        httpServletResponse.addHeader("Set-Cookie", cookieValue);
//        httpServletResponse.addCookie(cookie);

        responseWrapper.setResponse(authenticationResponse);
        return responseWrapper;
    }
}
