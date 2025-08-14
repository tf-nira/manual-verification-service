package in.tf.nira.manual.verification.entity;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Where;

import com.vladmihalcea.hibernate.type.array.ListArrayType;

import in.tf.nira.manual.verification.dto.EscalationDetailsDTO;
import in.tf.nira.manual.verification.util.EscalationDetailsConverter;
import in.tf.nira.manual.verification.util.StringListConverter;
import lombok.Data;

@TypeDef(name = "list-array", typeClass = ListArrayType.class)

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
	
	@Column(name = "foundlink")
	private String foundLink;
	
	@Column(name = "age_group")
	private String ageGroup;

	@Column(name = "res_district")
	private String resDistrict;
	
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
	
	@Convert(converter = StringListConverter.class)
	@Column(name = "upload_doc_list")
	private List<String> uploadDocList;
	
	@Column(name = "assigned_date")
	private LocalDateTime assignedDate;
	
	@Type(type="list-array")
	@Column(name="matched_reg_ids", columnDefinition="text[]")
	private List<String> matchedRegIds;
	
	@Column(name = "surname")
	private String surname;
	
	@Column(name = "given_name")
	private String givenName;
	
	@Column(name = "date_of_birth")
	private String dateOfBirth;

	@Column(name = "enrolment_district")
	private String applicantPlaceOfEnrolmentDistrict;
	
	
}
