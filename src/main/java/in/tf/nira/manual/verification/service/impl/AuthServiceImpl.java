package in.tf.nira.manual.verification.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import in.tf.nira.manual.verification.dto.*;
import in.tf.nira.manual.verification.service.AuthService;
import in.tf.nira.manual.verification.service.AuthTokenManager;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;

import java.util.Objects;


@Service
public class AuthServiceImpl implements AuthService {

    @Value("${manual.verification.auth.url}")
    private String authenticationUrl;
	/*
	 * @Value("${io.mosip.manualVerificationService.getUsersUrl}") private String
	 * fetchUsersUrl;
	 */
    @Value("${manual.verification.auth.app.id}")
    private String appId;
    
    @Value("${manual.verification.auth.client.id}")
    private String clientId;
    
    @Value("${manual.verification.auth.client.secret}")
    private String clientSecret;

    @Autowired
    private AuthTokenManager authTokenManager;

    @Override
    public AuthenticationResponse loginClient(RequestWrapper<AuthenticationRequest> authRequest) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        authRequest.getRequest().setAppId(appId);
        authRequest.getRequest().setClientId(clientId);
        authRequest.getRequest().setClientSecret(clientSecret);
        HttpEntity<RequestWrapper<AuthenticationRequest>> entity = new HttpEntity<>(authRequest, headers);

        ResponseEntity<ResponseWrapper<AuthenticationResponse>> response = restTemplate.exchange(
                authenticationUrl,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseWrapper<AuthenticationResponse>>() {}
        );

        authTokenManager.setToken(Objects.requireNonNull(response.getBody().getResponse()).getToken());

        return response.getBody().getResponse();
    }

	/*
	 * @Override public UserResponse fetchUsersByRole(String role) {
	 * 
	 * RestTemplate restTemplate = new RestTemplate();
	 * 
	 * String url = String.format(fetchUsersUrl + "?roleName=" + role);
	 * 
	 * HttpHeaders headers = new HttpHeaders(); headers.set("Content-Type",
	 * "application/json"); headers.set("Cookie", "Authorization=" +
	 * authTokenManager.getToken());
	 * 
	 * HttpEntity<String> entity = new HttpEntity<>(headers);
	 * 
	 * ResponseEntity<UserResponse> response = restTemplate.exchange( url,
	 * HttpMethod.GET, entity, UserResponse.class );
	 * 
	 * return response.getBody();
	 * 
	 * }
	 */
}