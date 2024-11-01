package io.mosip.manualVerificationService.dto;

import lombok.Data;

@Data
public class UserDto {
    private String userId;
    private String mobile;
    private String mail;
    private String langCode;
    private String userPassword;
    private String name;
    private String role;
    private String token;
    private String rid;
}
