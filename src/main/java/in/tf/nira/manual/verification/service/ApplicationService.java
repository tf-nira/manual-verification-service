package in.tf.nira.manual.verification.service;

import java.util.List;

import javax.validation.Valid;

import in.tf.nira.manual.verification.dto.ApplicationDetailsResponse;
import in.tf.nira.manual.verification.dto.AuthenticationResponse;
import in.tf.nira.manual.verification.dto.UserApplicationsResponse;
import in.tf.nira.manual.verification.dto.CreateAppRequestDTO;
import in.tf.nira.manual.verification.dto.UpdateStatusRequest;

public interface ApplicationService {

    AuthenticationResponse createApplication(CreateAppRequestDTO verifyRequest) throws Exception;
    List<UserApplicationsResponse> getApplicationsForUser(String userId);
	ApplicationDetailsResponse getApplicationDetails(String applicationId) throws Exception;
	AuthenticationResponse updateApplicationStatus(String applicationId, UpdateStatusRequest request);
}
