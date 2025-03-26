package in.tf.nira.manual.verification.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import in.tf.nira.manual.verification.entity.CountryRegionMapping;

@Repository
public interface CountryRegionMappingRepo extends JpaRepository <CountryRegionMapping, String>{
	
	@Query("SELECT c.region FROM CountryRegionMapping c WHERE UPPER(c.countryName) = UPPER(:countryName)")
	Optional<String> findRegionByCountryName(@Param("countryName")String countryName);
}
