package in.tf.nira.manual.verification.entity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "district_offices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistrictOffice {
	
	@Id
	@Column(name = "district_office_code")
	private Integer districtOfficeCode;
	
	@Column(name = "district_office_name")
	private String districtOfficeName;
	
	@Column(name = "district_code")
	private Integer districtCode;
	
	@Column(name = "district_name")
	private String districtName;
	
	@Column(name = "region_name")
	private String regionName;

	@Column(name = "district_name_with_code")
	private String districtNameWithCode;
	
}