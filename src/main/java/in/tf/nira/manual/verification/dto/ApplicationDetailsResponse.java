package in.tf.nira.manual.verification.dto;

import java.util.Map;

import lombok.Data;

@Data
public class ApplicationDetailsResponse {
	private String applicationId;
	private String service;
	private String serviceType;
	private Map<String, String> demographics;
	private Map<String, String> documents;
	private Map<String, Object> biometricAttributes;
}
