package in.tf.nira.manual.verification.service;

import java.util.List;

import in.tf.nira.manual.verification.dto.ApplicationDetailsResponse;
import in.tf.nira.manual.verification.dto.CreateAppRequestDTO;
import in.tf.nira.manual.verification.dto.DemographicDetailsDTO;
import in.tf.nira.manual.verification.dto.DocumentDTO;
import in.tf.nira.manual.verification.dto.PageResponseDto;
import in.tf.nira.manual.verification.dto.SchInterviewDTO;
import in.tf.nira.manual.verification.dto.SearchDto;
import in.tf.nira.manual.verification.dto.StatusResponseDTO;
import in.tf.nira.manual.verification.dto.UpdateStatusRequest;
import in.tf.nira.manual.verification.dto.UserApplicationsResponse;

public interface ApplicationService {

	StatusResponseDTO createApplication(CreateAppRequestDTO verifyRequest);
	String getOfficerRoleBasedOnUpdateService(CreateAppRequestDTO verifyRequest);
    List<UserApplicationsResponse> getApplicationsForUser(String userId);
	ApplicationDetailsResponse getApplicationDetails(String applicationId);
	StatusResponseDTO updateApplicationStatus(String applicationId, UpdateStatusRequest request);
	StatusResponseDTO scheduleInterview(String applicationId, SchInterviewDTO request);
	StatusResponseDTO uploadDocuments(String applicationId, DocumentDTO documentDTO);
	PageResponseDto<UserApplicationsResponse> searchApplications(SearchDto request);
	DemographicDetailsDTO getDemographicDetails(String registrationId);
}
