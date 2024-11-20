package in.tf.nira.manual.verification.dto;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class OfficerDetailDTO {
	private String id;
	private String userId;
	private String userName;
	private String userRole;
}
