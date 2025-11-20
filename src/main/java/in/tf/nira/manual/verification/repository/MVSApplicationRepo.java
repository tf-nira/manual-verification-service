package in.tf.nira.manual.verification.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
	MVSApplication getApplicationById(@Param("applicationId") String applicationId);

	@Query(value = "SELECT * FROM mvs_application a WHERE a.reg_id = :applicationId AND a.stage = 'REJECTED'", nativeQuery = true)
	MVSApplication getRejectedApplicationById(@Param("applicationId") String applicationId);

	@Query("SELECT e FROM mvs_application e " +
			"WHERE e.assignedOfficerRole IN ('MVS_OFFICER','MVS_SUPERVISOR','MVS_LEGAL_OFFICER') " +
			"AND ((e.updatedTimes IS NOT NULL AND e.updatedTimes < :dateThreshold) " +
			"OR (e.updatedTimes IS NULL AND e.crDTimes < :dateThreshold))")
	List<MVSApplication> findRecordsOlderThanXDays(@Param("dateThreshold") LocalDateTime dateThreshold);
	
	@Query("SELECT m FROM mvs_application m WHERE m.stage = :stage AND m.updatedTimes < :dateThreshold")
	List<MVSApplication> findInterviewExpiredApplications(@Param("stage") String stage, @Param("dateThreshold") LocalDateTime dateThreshold);

	@Query(value = "SELECT * FROM mvs_application a WHERE a.reg_id = :regId", nativeQuery = true)
	List<MVSApplication> findApplicationById(@Param("regId") String applicationId);
}
