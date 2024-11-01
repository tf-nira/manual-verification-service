package io.mosip.manualVerificationService.dto;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
public class UserResponse {
    private String id;
    private String version;
    private String responsetime;
    private Object metadata;
    private Response response;
    private List<Error> errors;

    @Data
    public static class Response {
        private List<UserDto> mosipUserDtoList;
    }
}
