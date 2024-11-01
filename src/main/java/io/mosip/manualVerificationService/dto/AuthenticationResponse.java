package io.mosip.manualVerificationService.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
public class AuthenticationResponse {
    private String id;

    private String version;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime responsetime = LocalDateTime.now(ZoneId.of("UTC"));

    @NotNull
    @Valid
    private AuthenticationResponseData response;

    public AuthenticationResponse() {
    }

    public String getAuthToken () {
        return this.response.getToken();
    }
}

@Data
class AuthenticationResponseData {
    private String token;
    private String message;
    private String refreshToken;
    private Integer expiryTime;
    private String userId;
    private String status;
    private Integer refreshExpiryTime;
}
