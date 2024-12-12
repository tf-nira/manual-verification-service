package in.tf.nira.manual.verification.dto;

import java.util.Map;

import lombok.Data;

@Data
public class OfficerDetailDTO {
	private String userId;
	private String userName;
	private String userRole;
	private String email;
	private Map<String, String> attributes;
}
