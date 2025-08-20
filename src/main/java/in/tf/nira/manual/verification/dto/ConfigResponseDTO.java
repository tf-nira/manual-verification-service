package in.tf.nira.manual.verification.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConfigResponseDTO {

    private List<AgeGroupRangeDTO> ageGroupRanges;
    private List<String> districtList;

}
