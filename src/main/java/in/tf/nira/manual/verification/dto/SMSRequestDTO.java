package in.tf.nira.manual.verification.dto;

import lombok.Data;

@Data
public class SMSRequestDTO {

	private String message;
	
	private String number;
}