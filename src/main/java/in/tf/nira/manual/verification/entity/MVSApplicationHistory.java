package in.tf.nira.manual.verification.entity;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import in.tf.nira.manual.verification.dto.EscalationDetailsDTO;
import in.tf.nira.manual.verification.util.EscalationDetailsConverter;
import lombok.Data;

@Entity(name = "mvs_application_h")
@Table
@IdClass(MVSApplicationId.class)
@Data
public class MVSApplicationHistory {
	@Id
	@Column(name = "reg_id")
	private String regId;

	@Column(name = "service")
	private String service;

	@Column(name = "service_type")
	private String serviceType;
	
	@Column(name = "verified_officer_id")
	private String verifiedOfficerId;
	
	@Column(name = "verified_officer_name")
	private String verifiedOfficerName;
	
	@Id
	@Column(name = "verified_officer_role")
	private String verifiedOfficerRole;
	
	@Column(name = "stage")
	private String stage;
	
	@Column(name = "comments")
	private String comments;
	
	@Column(name = "rejection_category")
	private String rejectionCategory;

	@Convert(converter = EscalationDetailsConverter.class)
    @Column(name = "escalation_details")
	private List<EscalationDetailsDTO> escalationDetails;

	@NotNull
	@Column(name = "cr_by")
	private String createdBy;

	@NotNull
	@Column(name = "cr_dtimes")
	private LocalDateTime crDTimes;
	
	@Column(name = "status_comment")
	private String statusComment;
}
