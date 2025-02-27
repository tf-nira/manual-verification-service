package in.tf.nira.manual.verification.entity;

import java.time.LocalDateTime;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import in.tf.nira.manual.verification.util.StringToMapConverter;
import lombok.Data;

@Entity(name = "mvs_application")
@Table
@Data
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
	
	@Column(name = "assigned_officer_id")
	private String assignedOfficerId;
	
	@Column(name = "assigned_officer_name")
	private String assignedOfficerName;
	
	@Column(name = "assigned_officer_role")
	private String assignedOfficerRole;
	
	@Column(name = "stage")
	private String stage;
	
	@Convert(converter = StringToMapConverter.class)
	@Column(name = "comments")
	private Map<String, String> comments;
	
	@Column(name = "rejection_category")
	private String rejectionCategory;

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
}
