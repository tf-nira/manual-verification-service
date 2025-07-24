package in.tf.nira.manual.verification.dto;

import lombok.Data;

@Data
public class AgeGroupRangeDTO {
    private String groupName;
    private String range;

    public AgeGroupRangeDTO(String key, String value) {
        this.groupName = key;
        this.range = value;
    }
}