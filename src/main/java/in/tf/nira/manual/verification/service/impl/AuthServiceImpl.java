package in.tf.nira.manual.verification.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Qualifier;

import in.tf.nira.manual.verification.constant.ErrorCode;
import in.tf.nira.manual.verification.dto.AuthenticationRequest;
import in.tf.nira.manual.verification.dto.AuthenticationResponse;
import in.tf.nira.manual.verification.exception.RequestException;
import in.tf.nira.manual.verification.service.AuthService;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;

@Service
public class AuthServiceImpl implements AuthService {
	private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Value("${manual.verification.auth.url}")
    private String authenticationUrl;
	
    @Value("${mosip.iam.adapter.appid}")
    private String appId;
    
    @Value("${mosip.iam.adapter.clientid}")
    private String clientId;
    
    @Value("${mosip.iam.adapter.clientsecret}")
    private String clientSecret;
    
    @Autowired
    @Qualifier("selfTokenRestTemplate")
    RestTemplate localRestTemplate;

    @Override
    public AuthenticationResponse loginClient(RequestWrapper<AuthenticationRequest> authRequest) {
    	logger.info("Authenticating user: {}", authRequest.getRequest().getUserName());
    	
        try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Content-Type", "application/json");

			authRequest.getRequest().setAppId(appId);
			authRequest.getRequest().setClientId(clientId);
			authRequest.getRequest().setClientSecret(clientSecret);
			HttpEntity<RequestWrapper<AuthenticationRequest>> entity = new HttpEntity<>(authRequest, headers);

			ResponseEntity<ResponseWrapper<AuthenticationResponse>> response = localRestTemplate.exchange(
			        authenticationUrl,
			        HttpMethod.POST,
			        entity,
			        new ParameterizedTypeReference<ResponseWrapper<AuthenticationResponse>>() {}
			);
			
			if (response.getBody() == null) {
				logger.error("Failed to authenticate. Status code: " + response.getStatusCodeValue());
			    throw new RequestException(ErrorCode.AUTHENTICATION_FAILED.getErrorCode(),
						ErrorCode.AUTHENTICATION_FAILED.getErrorMessage() + "with status code: " + response.getStatusCodeValue());
			}
			
			ResponseWrapper<AuthenticationResponse> authResponse = response.getBody();
			
			if (authResponse.getErrors() != null && !authResponse.getErrors().isEmpty()) {
				logger.error("Authentication error: {}", authResponse.getErrors().get(0));
			    throw new RequestException(ErrorCode.FAILED_AUTHENTICATION_RESPONSE.getErrorCode(),
						ErrorCode.FAILED_AUTHENTICATION_RESPONSE.getErrorMessage() + "with error: " + authResponse.getErrors().get(0));
			}
			
			logger.info("User authenticated successfully");
			
			return authResponse.getResponse();
		} catch (RequestException ex) {
	        throw ex;
	    } catch (Exception ex) {
	    	logger.error("Failed to authenticate, {}", ex);
            throw new RequestException(ErrorCode.AUTHENTICATION_FAILED.getErrorCode(),
					ErrorCode.AUTHENTICATION_FAILED.getErrorMessage() + "with error: " + ex.getMessage());
	    }
    }
}
