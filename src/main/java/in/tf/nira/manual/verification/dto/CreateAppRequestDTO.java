package in.tf.nira.manual.verification.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CreateAppRequestDTO {
	private String id;
	private String version;
	private String requestId;
	private LocalDateTime requesttime;
	private String regId;
	private String service;
	private String serviceType;
	private String referenceURL;
	private String source;
	private String refId;
	private String schemaVersion;
	private String statusComment;
	private String foundLink;
	private String ageGroup;
}
