package in.tf.nira.manual.verification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import in.tf.nira.manual.verification.entity.OfficerAssignment;

public interface OfficerAssignmentRepo extends JpaRepository<OfficerAssignment, String> {

	OfficerAssignment findByUserRole(String userRole);

}
