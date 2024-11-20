package in.tf.nira.manual.verification.service.impl;

import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.tf.nira.manual.verification.constant.FailureConstants;
import in.tf.nira.manual.verification.constant.StageCode;
import in.tf.nira.manual.verification.dto.ApplicationDetailsResponse;
import in.tf.nira.manual.verification.dto.AuthenticationResponse;
import in.tf.nira.manual.verification.dto.DataShareResponseDto;
import in.tf.nira.manual.verification.dto.OfficerDetailDTO;
import in.tf.nira.manual.verification.dto.UpdateStatusRequest;
import in.tf.nira.manual.verification.dto.UserApplicationsResponse;
import in.tf.nira.manual.verification.dto.UserDto;
import in.tf.nira.manual.verification.dto.UserResponse;
import in.tf.nira.manual.verification.dto.UserResponse.Response;
import in.tf.nira.manual.verification.dto.CreateAppRequestDTO;
import in.tf.nira.manual.verification.entity.MVSApplication;
import in.tf.nira.manual.verification.entity.OfficerAssignment;
import in.tf.nira.manual.verification.exception.RequestException;
import in.tf.nira.manual.verification.repository.MVSApplicationRepo;
import in.tf.nira.manual.verification.repository.OfficerAssignmentRepo;
import in.tf.nira.manual.verification.service.ApplicationService;
import in.tf.nira.manual.verification.util.CryptoCoreUtil;

@Service
public class ApplicationServiceImpl implements ApplicationService {
	
	@Value("${manual.verification.user.details.url}")
    private String userDetailsUrl;
	
	@Value("#{'${app.officer.roles:MVS_OFFICER,MVS_SUPERVISOR,MVS_DISTRICT_OFFICER,MVS_LEGAL_OFFICER}'.split(',')}")
    private List<String> officerRoles;
	
	@Value("${data.share.encryption:false}")
	private boolean encryption;
	
	Map<String, List<OfficerDetailDTO>> officerDetailMap = new HashMap<>();

	@Autowired(required = true)
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate restTemplate;
	
	@Autowired
	CryptoCoreUtil cryptoUtil;
	
	@Autowired
	private OfficerAssignmentRepo officerAssignmentRepo;
	
	@Autowired
	private MVSApplicationRepo mVSApplicationRepo;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@PostConstruct
    public void runAtStartup() {
        fetchUsers();
    }
	
	@Override
	public AuthenticationResponse createApplication(CreateAppRequestDTO verifyRequest) throws Exception {
		OfficerAssignment officerAssignment = officerAssignmentRepo.findByUserRole("MVS_OFFICER");
		if(officerAssignment == null) {
			officerAssignment = new OfficerAssignment();
		}
		OfficerDetailDTO selectedOfficer = fetchOfficerAssignment("MVS_OFFICER", officerAssignment);
		
		if(selectedOfficer != null) {
			MVSApplication mVSApplication = new MVSApplication();
			mVSApplication.setRegId(verifyRequest.getReferenceId());
			mVSApplication.setService(verifyRequest.getService());
			mVSApplication.setServiceType(verifyRequest.getServiceType());
			mVSApplication.setReferenceURL(verifyRequest.getReferenceURL());
			mVSApplication.setAssignedOfficerId(selectedOfficer.getUserId());
			mVSApplication.setAssignedOfficerName(selectedOfficer.getUserName());
			mVSApplication.setAssignedOfficerRole(selectedOfficer.getUserRole());
			mVSApplication.setStage(StageCode.ASSIGNED_TO_OFFICER.getStage());
			mVSApplication.setCreatedBy("");
			mVSApplication.setCrDTimes(LocalDateTime.now());
			
			mVSApplicationRepo.save(mVSApplication);
			
			if(officerAssignment.getCrDTimes() == null) {
				officerAssignment.setCreatedBy("");
				officerAssignment.setCrDTimes(LocalDateTime.now());
			}
			officerAssignmentRepo.save(officerAssignment);
		}
		return null;
	}
	
	@Override
	public List<UserApplicationsResponse> getApplicationsForUser(String userId) {
		List<MVSApplication> applications = mVSApplicationRepo.findByAssignedOfficerId(userId);
		List<UserApplicationsResponse> response = new ArrayList<>();
		
		applications.forEach(app -> {
			UserApplicationsResponse userApp = new UserApplicationsResponse();
			userApp.setApplicationId(app.getRegId());
			userApp.setService(app.getService());
			userApp.setServiceType(app.getServiceType());
			userApp.setCrDTimes(app.getCrDTimes());
			
			response.add(userApp);
		});
		
		return response;
	}
	
	@Override
	public ApplicationDetailsResponse getApplicationDetails(String applicationId) throws Exception {
		try {
			Optional<MVSApplication> optional = mVSApplicationRepo.findById(applicationId);
			MVSApplication application = optional.get();
			
			//logger.info("Fetching CBEFF for reference URL-" + CBEFF_URL);
			ResponseEntity<String> responseEn = restTemplate.exchange(application.getReferenceURL(), HttpMethod.GET, null, String.class);
			//logger.info("CBEFF response-" + cbeffResp);
			String response = responseEn.getBody();
			//logger.info("CBEFF Data-" + cbeff);
			
			try {
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(response);
				JSONArray errors = (JSONArray) json.get("errors");
				if(errors != null) {
					for (Iterator it = errors.iterator(); it.hasNext();) {
						JSONObject error = (JSONObject) it.next();
						String errorCode = ((String) error.get("errorCode")).trim();
						String message = ((String) error.get("message")).trim();
						//logger.info(String.format("ErrorCode[%s], ErrorMessage[%s],", errorCode, message));
						throw new RequestException(errorCode);
					}
				}
			} catch (RequestException ex) {
				if (ex.getReasonConstant().equalsIgnoreCase("DAT-SER-006"))
					throw new RequestException(FailureConstants.DATA_SHARE_URL_EXPIRED);
				else
					throw new RequestException(FailureConstants.UNEXPECTED_ERROR);
			} catch (Exception ex) {
				// ex.printStackTrace();
			}
			
			if (encryption) {
				response = cryptoUtil.decrypt(response);
			}
			
			DataShareResponseDto dataShareResponse = objectMapper.readValue(response, DataShareResponseDto.class);
			
			ApplicationDetailsResponse applicationDetailsResponse = new ApplicationDetailsResponse();
			applicationDetailsResponse.setApplicationId(applicationId);
			applicationDetailsResponse.setService(application.getService());
			applicationDetailsResponse.setServiceType(application.getServiceType());
			applicationDetailsResponse.setDemographics(dataShareResponse.getIdentity());
			applicationDetailsResponse.setDocuments(dataShareResponse.getDocuments());
			
			return applicationDetailsResponse;
		} catch (HttpClientErrorException ex) {
			ex.printStackTrace();
			//logger.error("issue with httpclient URL " + ex.getLocalizedMessage());
			throw new RequestException(FailureConstants.UNABLE_TO_FETCH_BIOMETRIC_DETAILS);
		} catch (URISyntaxException | IllegalArgumentException ex) {
			ex.printStackTrace();
			//logger.error("issue with httpclient URL Syntax " + ex.getLocalizedMessage());
			throw new RequestException(FailureConstants.UNABLE_TO_FETCH_BIOMETRIC_DETAILS);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}
	
	@Override
	public AuthenticationResponse updateApplicationStatus(String applicationId, UpdateStatusRequest request) {
		Optional<MVSApplication> optional = mVSApplicationRepo.findById(applicationId);
		MVSApplication application = optional.get();
		
		if(request.getStatus().equals("APPROVE")) {
			application.setStage(StageCode.APPROVED.getStage());
			//comment
			mVSApplicationRepo.save(application);
		}
		else if(request.getStatus().equals("REJECT")) {
			application.setStage(StageCode.REJECTED.getStage());
			//comment and rejection cat
			mVSApplicationRepo.save(application);
		}
		else if(request.getStatus().equals("ESCALATE")) {
			if(application.getAssignedOfficerRole().equals("MVS_OFFICER")) {
				OfficerAssignment officerAssignment = officerAssignmentRepo.findByUserRole("MVS_SUPERVISOR");
				if(officerAssignment == null) {
					officerAssignment = new OfficerAssignment();
				}
				OfficerDetailDTO selectedOfficer = fetchOfficerAssignment("MVS_SUPERVISOR", officerAssignment);
				
				if(selectedOfficer != null) {
					application.setAssignedOfficerId(selectedOfficer.getUserId());
					application.setAssignedOfficerName(selectedOfficer.getUserName());
					application.setAssignedOfficerRole(selectedOfficer.getUserRole());
					application.setStage(StageCode.ASSIGNED_TO_SUPERVISOR.getStage());
					application.setUpdatedBy("");
					application.setUpdatedTimes(LocalDateTime.now());
					
					mVSApplicationRepo.save(application);
					
					if(officerAssignment.getCrDTimes() == null) {
						officerAssignment.setCreatedBy("");
						officerAssignment.setCrDTimes(LocalDateTime.now());
					}
					officerAssignmentRepo.save(officerAssignment);
				}
			}
			else if(application.getAssignedOfficerRole().equals("MVS_SUPERVISOR")) {
				OfficerAssignment officerAssignment = officerAssignmentRepo.findByUserRole("MVS_DISTRICT_OFFICER");
				if(officerAssignment == null) {
					officerAssignment = new OfficerAssignment();
				}
				OfficerDetailDTO selectedOfficer = fetchOfficerAssignment("MVS_DISTRICT_OFFICER", officerAssignment);
				
				if(selectedOfficer != null) {
					application.setAssignedOfficerId(selectedOfficer.getUserId());
					application.setAssignedOfficerName(selectedOfficer.getUserName());
					application.setAssignedOfficerRole(selectedOfficer.getUserRole());
					application.setStage(StageCode.ASSIGNED_TO_DISTRICT_OFFICER.getStage());
					application.setUpdatedBy("");
					application.setUpdatedTimes(LocalDateTime.now());
					
					mVSApplicationRepo.save(application);
					
					if(officerAssignment.getCrDTimes() == null) {
						officerAssignment.setCreatedBy("");
						officerAssignment.setCrDTimes(LocalDateTime.now());
					}
					officerAssignmentRepo.save(officerAssignment);
				}
			}
			else {
				//cannot escalate
			}
		}
		else if(request.getStatus().equals("SCHEDULE_INTERVIEW")) {
			if(application.getAssignedOfficerRole().equals("MVS_DISTRICT_OFFICER") || 
					application.getAssignedOfficerRole().equals("MVS_LEGAL_OFFICER")) {
				application.setStage(StageCode.INTERVIEW_SCHEDULED.getStage());
				mVSApplicationRepo.save(application);
			}
			else {
				// invalid status for given role
			}
		}
		return null;
	}
	
	private OfficerDetailDTO fetchOfficerAssignment(String role, OfficerAssignment officerAssignment) {
		if(officerDetailMap == null || officerDetailMap.isEmpty()) {
			fetchUsers();
		}
		
		List<OfficerDetailDTO> officers = officerDetailMap.get(role);
		
		if (officers == null || officers.isEmpty()) {
	        //throw ("No officers available for role: " + role);
			return null;
	    }
		
		Optional<OfficerDetailDTO> optionalOff;
		if(officerAssignment.getUserId() == null) {
			optionalOff = officers.stream().findFirst();
			officerAssignment.setId(UUID.randomUUID().toString());
			officerAssignment.setUserRole(role);
		}
		else {
			String userId = officerAssignment.getUserId();
			optionalOff = officers.stream().filter(officer -> officer.getUserId().equals(userId)).findFirst();
		}
		
		if(optionalOff.isPresent()) {
			OfficerDetailDTO selectedOfficer = optionalOff.get();
			int currentIndex = officers.indexOf(selectedOfficer);
			
			OfficerDetailDTO nextOfficer = officers.get((currentIndex + 1) % officers.size());
			officerAssignment.setUserId(nextOfficer.getUserId());
			return selectedOfficer;
		}
		else {
			//throw ("No officer found for the current assignment.");
			return null;
		}
	}
	
	@Scheduled(cron = "0 0/3 * * * ?")
	public void fetchUsers() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);

		officerRoles.forEach(role -> {
			String url = String.format(userDetailsUrl + "?roleName=" + role);
	        try {
				ResponseEntity<UserResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, UserResponse.class);
				
				if(response.getBody() != null && response.getBody().getResponse() != null) {
					List<UserDto> users = ((Response) response.getBody().getResponse()).getMosipUserDtoList();
				    
				    List<OfficerDetailDTO> userDetails = new ArrayList<OfficerDetailDTO>();
				    
				    users.forEach(user -> {
				    	OfficerDetailDTO userDetail = new OfficerDetailDTO();
				    	userDetail.setId(user.getRid());
				    	userDetail.setUserId(user.getUserId());
				    	userDetail.setUserName(user.getName());
				    	userDetail.setUserRole(user.getRole());
				    	userDetails.add(userDetail);
				    });
				    
				    userDetails.sort((o1, o2) -> o1.getUserId().compareTo(o2.getUserId()));
				    officerDetailMap.put(role, userDetails);
				}
			} catch (RestClientException e) {
				//log error
			} catch (Exception exc) {
				//log error
			}
		});
	}
}
