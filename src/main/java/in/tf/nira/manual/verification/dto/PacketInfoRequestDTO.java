package in.tf.nira.manual.verification.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PacketInfoRequestDTO {
    private String id;
    private String version;
    private LocalDateTime requesttime;
    private Map<String, Object> metadata;

    @JsonProperty("request")
    private PacketInfoRequest request;

    @Data
    public static class PacketInfoRequest {
        private String id;
    }
}