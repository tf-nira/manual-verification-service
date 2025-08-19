package in.tf.nira.manual.verification.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
	private EscalationDetailsDTO legalEscDetails;
	private EscalationDetailsDTO districtEscDetails;
	private String statusComment;
	private String foundLink;
	private String ageGroup;
	private List<String> matchedRegIds;
	private String surname;
	private String givenName;
	private LocalDateTime dateOfBirth;
	private String resDistrict;
	private String applicantPlaceOfEnrolmentDistrict;
}
