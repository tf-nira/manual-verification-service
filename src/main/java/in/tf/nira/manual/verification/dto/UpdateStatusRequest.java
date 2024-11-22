package in.tf.nira.manual.verification.dto;

import lombok.Data;

@Data
public class UpdateStatusRequest {
	private String status;
	private String comment;
	private String rejectionCategory;
	private Boolean insufficientDocuments;
}
