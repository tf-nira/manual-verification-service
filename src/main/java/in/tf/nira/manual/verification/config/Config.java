package in.tf.nira.manual.verification.config;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.tf.nira.manual.verification.dto.AccessTokenResponse;
import in.tf.nira.manual.verification.interceptor.MemoryCache;
import in.tf.nira.manual.verification.interceptor.RestInterceptor;
import in.tf.nira.manual.verification.util.EscalationDetailsConverter;

@Configuration
public class Config {
	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private RestInterceptor restInterceptor;
	
	@Bean
    public EscalationDetailsConverter escalationDetailsConverter() {
		EscalationDetailsConverter.setObjectMapper(mapper);
		
        return new EscalationDetailsConverter();
    }
	
	@Bean
	public MemoryCache<String, AccessTokenResponse> memoryCache() {
		return new MemoryCache<>(1);
	}

	@Bean(name = "keycloakRestTemplate")
	public RestTemplate getRestTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setInterceptors(Collections.singletonList(restInterceptor));
		return restTemplate;
	}
}
