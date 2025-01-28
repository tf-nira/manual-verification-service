package in.tf.nira.manual.verification.entity;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Where;

import in.tf.nira.manual.verification.dto.EscalationDetailsDTO;
import in.tf.nira.manual.verification.util.EscalationDetailsConverter;
import lombok.Data;

@Entity(name = "mvs_application")
@Table
@Data
@Where(clause = "is_deleted is not true")
public class MVSApplication {
	@Id
	@Column(name = "reg_id")
	private String regId;

	@Column(name = "service")
	private String service;

	@Column(name = "service_type")
	private String serviceType;
	
	@Column(name = "reference_url")
	private String referenceURL;
	
	@Column(name = "source")
	private String source;
	
	@Column(name = "ref_id")
	private String refId;
	
	@Column(name = "schema_version")
	private String schemaVersion;
	
	@Column(name = "assigned_officer_id")
	private String assignedOfficerId;
	
	@Column(name = "assigned_officer_name")
	private String assignedOfficerName;
	
	@Column(name = "assigned_officer_role")
	private String assignedOfficerRole;
	
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
	
	@Column(name = "upd_by")
	private String updatedBy;

	@Column(name = "upd_dtimes")
	private LocalDateTime updatedTimes;
	
	@Column(name = "is_deleted")
	private Boolean isDeleted;
	
	@Column(name = "del_dtimes")
	private LocalDateTime deletedTimes;
	
	@Column(name = "status_comment")
	private String statusComment;
}
