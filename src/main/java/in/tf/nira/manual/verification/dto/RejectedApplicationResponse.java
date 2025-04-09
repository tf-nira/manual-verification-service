package in.tf.nira.manual.verification.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RejectedApplicationResponse {
    private String applicationId;
    private String service;
    private String serviceType;
    private String status;
    private LocalDateTime crDTimes;
    private EscalationDetailsDTO officerEscDetails;
    private EscalationDetailsDTO supervisorEscDetails;
    private EscalationDetailsDTO legalEscDetails;
    private String rejectionCategory;
    private String rejectionComment;
    private String lastAssignedOfficerRole;
    private String lastAssignedOfficerId;
    private LocalDateTime lastUpdatedTimes;
    private String statusComment;
    private String foundLink;
    private String ageGroup;
}