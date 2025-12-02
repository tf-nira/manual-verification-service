package in.tf.nira.manual.verification.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ConfigResponseDTO {

    private List<AgeGroupRangeDTO> ageGroupRanges;
    private List<String> districtList;
    private Map<String, String[]> ESCALATION_CATEGORIES;
    private Map<String, String[]> REJECTION_CATEGORIES;

}
