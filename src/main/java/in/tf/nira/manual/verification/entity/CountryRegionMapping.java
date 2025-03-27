package in.tf.nira.manual.verification.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "country_region_mapping")
public class CountryRegionMapping {
	@Id
	@Column(name = "country_name", nullable = false, length = 128)
	private String countryName;
	
	@Column(name = "region", nullable = false, length = 64)
	private String region;
	
	@Column(name = "cr_by", nullable = false)
	private String createdBy;
	
	@Column(name = "cr_dtimes", nullable = false)
	private LocalDateTime crDtimes;
	
	@Column(name = "upd_by")
	private String updatedBy;
	
	@Column(name = "upd_dtimes")
	private LocalDateTime updDtimes;

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public LocalDateTime getCrDtimes() {
		return crDtimes;
	}

	public void setCrDtimes(LocalDateTime crDtimes) {
		this.crDtimes = crDtimes;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public LocalDateTime getUpdDtimes() {
		return updDtimes;
	}

	public void setUpdDtimes(LocalDateTime updDtimes) {
		this.updDtimes = updDtimes;
	}
	
	
	
}
