package in.tf.nira.manual.verification.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AuthenticationRequest {

	@NotNull
    private String userName;

    @NotNull
    private String password;

    private String appId;

    private String clientId;

    private String clientSecret;
}