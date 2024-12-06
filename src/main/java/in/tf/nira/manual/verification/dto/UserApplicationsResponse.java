package in.tf.nira.manual.verification.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserApplicationsResponse {
	private String applicationId;
	private String service;
	private String serviceType;
	private String status;
	private LocalDateTime crDTimes;
	private EscalationDetailsDTO officerEscDetails;
	private EscalationDetailsDTO supervisorEscDetails;
}
