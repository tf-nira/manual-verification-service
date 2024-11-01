package io.mosip.manualVerificationService.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class AuthenticationRequest {

    private String id;
    private String version;
    private LocalDateTime requesttime;
    private Map<String, Object> metadata;
    private RequestDataDTO request;
}

@Data
class RequestDataDTO {
    @NotNull
    private String userName;

    @NotNull
    private String password;

    @NotNull
    private String appId;

    @NotNull
    private String clientId;

    @NotNull
    private String clientSecret;
}