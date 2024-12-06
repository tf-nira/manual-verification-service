package in.tf.nira.manual.verification.dto;

import java.util.List;

import lombok.Data;

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
