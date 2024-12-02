package in.tf.nira.manual.verification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import in.tf.nira.manual.verification.entity.MVSApplication;
import in.tf.nira.manual.verification.entity.MVSApplicationHistory;
import in.tf.nira.manual.verification.entity.MVSApplicationId;

public interface MVSApplicationHistoryRepo extends JpaRepository<MVSApplicationHistory, MVSApplicationId> {

	List<MVSApplicationHistory> findByVerifiedOfficerId(String verifiedOfficerId);
	MVSApplicationHistory findByRegIdAndVerifiedOfficerRole(String regId, String verifiedOfficerRole);
}
