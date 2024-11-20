package in.tf.nira.manual.verification.dto;

import lombok.Data;

import java.util.Map;

@Data
public class DataShareResponseDto {

    private String biometrics;

    private Map<String, String> identity;

    private Map<String, String> documents;

    private String metaInfo;

    private String audits;
}
