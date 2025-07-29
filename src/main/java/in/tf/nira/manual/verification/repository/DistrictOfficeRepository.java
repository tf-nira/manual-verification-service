package in.tf.nira.manual.verification.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.tf.nira.manual.verification.entity.DistrictOffice;

public interface DistrictOfficeRepository extends JpaRepository<DistrictOffice, Integer>{
	@Query("SELECT d FROM DistrictOffice d WHERE UPPER(d.districtName) = UPPER(:districtName)")
	Optional<DistrictOffice> findByDistrictNameIgnoreCase(@Param("districtName") String districtName);
}
