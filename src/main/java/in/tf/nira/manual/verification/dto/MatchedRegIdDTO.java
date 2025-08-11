package in.tf.nira.manual.verification.dto;

import lombok.Data;

@Data
public class MatchedRegIdDTO {
	private String surname;
	private String givenName;
	private String gender;
	private String dateOfBirth;
	private String phone;
	private String email;
}
