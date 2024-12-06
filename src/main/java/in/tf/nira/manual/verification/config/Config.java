package in.tf.nira.manual.verification.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.tf.nira.manual.verification.util.EscalationDetailsConverter;

@Configuration
public class Config {
	
	@Autowired
	private ObjectMapper mapper;
	
	@Bean
    public EscalationDetailsConverter escalationDetailsConverter() {
		EscalationDetailsConverter.setObjectMapper(mapper);
		
        return new EscalationDetailsConverter();
    }
}
