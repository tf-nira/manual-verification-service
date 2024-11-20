package in.tf.nira.manual.verification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import in.tf.nira.manual.verification.entity.MVSApplication;

public interface MVSApplicationRepo extends JpaRepository<MVSApplication, String> {

	List<MVSApplication> findByAssignedOfficerId(String assignedOfficerId);
}
