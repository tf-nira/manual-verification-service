package in.tf.nira.manual.verification.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class PacketInfoResponseDTO {
    private String id;
    private String version;
    private String responsetime;
    private Object metadata;
    private PacketInfoResponse response;
    private List<Object> errors;

    @Data
    public static class PacketInfoResponse {
        private String applicationId;
        private String packetId;
        private String requestToken;
        private List<PacketInfo> info;
        private Map<String, String> tags;
    }

    @Data
    public static class PacketInfo {
        private String source;
        private String process;
        private Long lastModified;
        private List<String> demographics;
        private List<BiometricInfo> biometrics;
        private List<Object> documents;
    }

    @Data
    public static class BiometricInfo {
        private String type;
        private List<String> subtypes;
    }
}