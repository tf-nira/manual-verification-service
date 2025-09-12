package in.tf.nira.manual.verification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistrictOfficeResponseDTO {
	
	@JsonProperty("district_office_code")
	private Integer districtOfficeCode;
	
	@JsonProperty("district_office_name")
	private String districtOfficeName;
}
