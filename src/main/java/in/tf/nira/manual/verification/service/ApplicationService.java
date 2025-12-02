package in.tf.nira.manual.verification.service;

import java.util.List;
import java.util.Map;

import in.tf.nira.manual.verification.dto.*;

public interface ApplicationService {

	StatusResponseDTO createApplication(CreateAppRequestDTO verifyRequest);
	String getOfficerRoleBasedOnUpdateService(CreateAppRequestDTO verifyRequest);
    List<UserApplicationsResponse> getApplicationsForUser(String userId, Boolean rejectFlag);
	RejectedApplicationResponse getRejectedApplication(String applicationId);
	ApplicationDetailsResponse getApplicationDetails(String applicationId);
	StatusResponseDTO updateApplicationStatus(String applicationId, UpdateStatusRequest request);
	StatusResponseDTO scheduleInterview(String applicationId, SchInterviewDTO request);
	StatusResponseDTO uploadDocuments(String applicationId, DocumentDTO documentDTO);
	PageResponseDto<UserApplicationsResponse> searchApplications(SearchDto request);
	DemographicDetailsDTO getDemographicDetails(String registrationId);
	DocumentResponseDTO fetchDocument(DocumentRequestDTO documentRequest);
	DistrictOfficeResponseDTO getDistrictOfficeByName (String district);
	ConfigResponseDTO getApplicationConfig ();
	DemographicDetailsDTO.Identity fetchMatchedRegIdDemographics(String registartionId);
	String getAssignedOfficerById(String applications);
	StatusResponseDTO updateDemographics(String applicationId, Map<String,Object> modifiedDetailsDTO);
}
