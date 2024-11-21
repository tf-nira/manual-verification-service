package in.tf.nira.manual.verification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import in.tf.nira.manual.verification.entity.MVSApplication;
import in.tf.nira.manual.verification.entity.MVSApplicationHistory;

public interface MVSApplicationHistoryRepo extends JpaRepository<MVSApplicationHistory, String> {

	List<MVSApplicationHistory> findByAssignedOfficerId(String assignedOfficerId);
	MVSApplicationHistory findByRegIdAndAssignedOfficerRole(String regId, String assignedOfficerRole);
}
