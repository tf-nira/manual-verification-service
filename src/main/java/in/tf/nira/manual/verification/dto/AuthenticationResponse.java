package in.tf.nira.manual.verification.dto;

import lombok.Data;

@Data
public class AuthenticationResponse {
	private String token;
    private String message;
    private String refreshToken;
    private Integer expiryTime;
    private String userId;
    private String status;
    private Integer refreshExpiryTime;
}