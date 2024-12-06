package in.tf.nira.manual.verification.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

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