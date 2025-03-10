package in.tf.nira.manual.verification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.tf.nira.manual.verification.entity.MVSApplication;

public interface MVSApplicationRepo extends JpaRepository<MVSApplication, String> {

	List<MVSApplication> findByAssignedOfficerId(String assignedOfficerId);
	
	@Query("SELECT COUNT(a) FROM mvs_application a WHERE a.assignedOfficerId = :userId")
	int countByAssignedOfficerId(@Param("userId") String userId);

	@Query(value = "SELECT * FROM mvs_application a WHERE a.stage = 'REJECTED'", nativeQuery = true)
	List<MVSApplication> getAllRejectedApplications();

	@Query(value = "SELECT * FROM mvs_application a WHERE a.reg_id = :applicationId", nativeQuery = true)
	MVSApplication getRejectedApplicationById(@Param("applicationId") String applicationId);

}
