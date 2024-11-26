package in.tf.nira.manual.verification.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.tf.nira.manual.verification.constant.CommonConstants;
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
import in.tf.nira.manual.verification.entity.MVSApplicationHistory;
import in.tf.nira.manual.verification.entity.OfficerAssignment;
import in.tf.nira.manual.verification.exception.RequestException;
import in.tf.nira.manual.verification.repository.MVSApplicationHistoryRepo;
import in.tf.nira.manual.verification.repository.MVSApplicationRepo;
import in.tf.nira.manual.verification.repository.OfficerAssignmentRepo;
import in.tf.nira.manual.verification.service.ApplicationService;
import in.tf.nira.manual.verification.util.CryptoCoreUtil;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.DateUtils;

@Service
public class ApplicationServiceImpl implements ApplicationService {
	
	@Value("${manual.verification.user.details.url}")
    private String userDetailsUrl;
	
	@Value("#{'${manual.verification.officer.roles}'.split(',')}")
    private List<String> officerRoles;
	
	@Value("${manual.verification.data.share.encryption:false}")
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
	private MVSApplicationHistoryRepo mVSApplicationHistoryRepo;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@PostConstruct
    public void runAtStartup() {
        fetchUsers();
    }
	
	@Override
	public AuthenticationResponse createApplication(CreateAppRequestDTO verifyRequest) throws Exception {
		OfficerAssignment officerAssignment = officerAssignmentRepo.findByUserRole(CommonConstants.MVS_OFFICER_ROLE);
		if(officerAssignment == null) {
			officerAssignment = new OfficerAssignment();
		}
		OfficerDetailDTO selectedOfficer = fetchOfficerAssignment(CommonConstants.MVS_OFFICER_ROLE, officerAssignment);
		
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
				//proper created by name
				officerAssignment.setCreatedBy("");
				officerAssignment.setCrDTimes(LocalDateTime.now());
			}
			else {
				//proper created by name
				officerAssignment.setUpdatedBy("");
				officerAssignment.setUpdatedTimes(LocalDateTime.now());
			}
			officerAssignmentRepo.save(officerAssignment);
		}
		return null;
	}
	
	@Override
	public List<UserApplicationsResponse> getApplicationsForUser(String userId) {
		List<MVSApplication> applications = mVSApplicationRepo.findByAssignedOfficerId(userId);
		List<UserApplicationsResponse> response = new ArrayList<>();
		
		applications.stream().filter(app -> !app.getStage().equals(StageCode.APPROVED.getStage()) && 
				!app.getStage().equals(StageCode.REJECTED.getStage())).forEach(app -> {
			UserApplicationsResponse userApp = new UserApplicationsResponse();
			userApp.setApplicationId(app.getRegId());
			userApp.setService(app.getService());
			userApp.setServiceType(app.getServiceType());
			userApp.setStatus(app.getStage());
			userApp.setCrDTimes(app.getCrDTimes());
			
			if(app.getAssignedOfficerRole().equals(CommonConstants.MVS_SUPERVISOR_ROLE)) {
				userApp.setOfficerEscReason(app.getComments());
			}
			else if(app.getAssignedOfficerRole().equals(CommonConstants.MVS_DISTRICT_OFFICER_ROLE)) {
				userApp.setSupervisorEscReason(app.getComments());
				MVSApplicationHistory appHistory = mVSApplicationHistoryRepo.findByRegIdAndVerifiedOfficerRole(app.getRegId(), CommonConstants.MVS_SUPERVISOR_ROLE);
				
				if(appHistory != null) {
					userApp.setOfficerEscReason(appHistory.getComments());
				}
			}
			
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
		
		if(request.getStatus().equals(CommonConstants.APPROVE_STATUS)) {
			application.setStage(StageCode.APPROVED.getStage());
			application.setComments(request.getComment());
			mVSApplicationRepo.save(application);
		}
		else if(request.getStatus().equals(CommonConstants.REJECT_STATUS)) {
			sendEmail(applicationId, "", "");
			application.setStage(StageCode.REJECTED.getStage());
			application.setComments(request.getComment());
			application.setRejectionCategory(request.getRejectionCategory());
			mVSApplicationRepo.save(application);
		}
		else if(request.getStatus().equals(CommonConstants.ESCALATE_STATUS)) {
			if(application.getAssignedOfficerRole().equals(CommonConstants.MVS_SUPERVISOR_ROLE) || request.getInsufficientDocuments()) {
				escalateApplication(application, CommonConstants.MVS_DISTRICT_OFFICER_ROLE, 
						StageCode.ASSIGNED_TO_DISTRICT_OFFICER.getStage(), request.getComment());
			}
			else if(application.getAssignedOfficerRole().equals(CommonConstants.MVS_OFFICER_ROLE)) {
				escalateApplication(application, CommonConstants.MVS_SUPERVISOR_ROLE, 
						StageCode.ASSIGNED_TO_SUPERVISOR.getStage(), request.getComment());
			}
			else {
				//cannot escalate
			}
		}
		else if(request.getStatus().equals(CommonConstants.SCHEDULE_INTERVIEW_STATUS)) {
			if(application.getAssignedOfficerRole().equals(CommonConstants.MVS_DISTRICT_OFFICER_ROLE) || 
					application.getAssignedOfficerRole().equals(CommonConstants.MVS_LEGAL_OFFICER_ROLE)) {
				sendEmail(applicationId, "", "");
				application.setStage(StageCode.INTERVIEW_SCHEDULED.getStage());
				mVSApplicationRepo.save(application);
			}
			else {
				// invalid status for given role
			}
		}
		else if(request.getStatus().equals(CommonConstants.UPLOAD_DOCUMENTS_STATUS)) {
			if(application.getAssignedOfficerRole().equals(CommonConstants.MVS_DISTRICT_OFFICER_ROLE)) {
				//upload document back to packet in reg proc
				application.setStage(StageCode.APPROVED.getStage());
				application.setComments(request.getComment());
				mVSApplicationRepo.save(application);
			}
			else {
				// invalid status for given role
			}
		}
		return null;
	}
	
	private MVSApplicationHistory getAppHistoryEntity(MVSApplication application) {
		MVSApplicationHistory appHistory = new MVSApplicationHistory();
		appHistory.setRegId(application.getRegId());
		appHistory.setService(application.getService());
		appHistory.setServiceType(application.getServiceType());
		appHistory.setVerifiedOfficerId(application.getAssignedOfficerId());
		appHistory.setVerifiedOfficerName(application.getAssignedOfficerName());
		appHistory.setVerifiedOfficerRole(application.getAssignedOfficerRole());
		appHistory.setStage(application.getStage());
		appHistory.setComments(application.getComments());
		appHistory.setRejectionCategory(application.getRejectionCategory());
		appHistory.setCreatedBy(application.getCreatedBy());
		appHistory.setCrDTimes(LocalDateTime.now());
		return appHistory;
	}
	
	private void escalateApplication(MVSApplication application, String role, String stage, String comment) {
		OfficerAssignment officerAssignment = officerAssignmentRepo.findByUserRole(role);
		if(officerAssignment == null) {
			officerAssignment = new OfficerAssignment();
		}
		OfficerDetailDTO selectedOfficer = fetchOfficerAssignment(role, officerAssignment);
		
		if(selectedOfficer != null) {
			MVSApplicationHistory appHistory = getAppHistoryEntity(application);
			application.setAssignedOfficerId(selectedOfficer.getUserId());
			application.setAssignedOfficerName(selectedOfficer.getUserName());
			application.setAssignedOfficerRole(selectedOfficer.getUserRole());
			application.setStage(stage);
			application.setComments(comment);
			application.setUpdatedBy("");
			application.setUpdatedTimes(LocalDateTime.now());
			
			mVSApplicationRepo.save(application);
			
			if(officerAssignment.getCrDTimes() == null) {
				officerAssignment.setCreatedBy("");
				officerAssignment.setCrDTimes(LocalDateTime.now());
			}
			else {
				//proper created by name
				officerAssignment.setUpdatedBy("");
				officerAssignment.setUpdatedTimes(LocalDateTime.now());
			}
			officerAssignmentRepo.save(officerAssignment);
			mVSApplicationHistoryRepo.save(appHistory);
		}
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
	
	private void sendEmail(String applicationId, String subjectArtifact, String artifact) {
		try {
			ApplicationDetailsResponse appResponse = getApplicationDetails(applicationId);
			
			String mailTo = appResponse.getDemographics().get("email");
			LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
			String apiHost = "";

			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiHost)
			        .queryParam("mailTo", mailTo)
			        .queryParam("mailSubject", subjectArtifact)
			        .queryParam("mailContent", artifact);

			params.add("attachments", null);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);

			HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(params, headers);

			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<ResponseWrapper> responseEntity = restTemplate.exchange(
			        builder.build().toUri(),
			        HttpMethod.POST,
			        requestEntity,
			        ResponseWrapper.class
			);

			ResponseWrapper<?> responseWrapper = responseEntity.getBody();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		//responseDto = mapper.readValue(mapper.writeValueAsString(responseWrapper.getResponse()), ResponseDto.class);

		//return responseDto;
	}
	
	/*
	 * private void sendSMS(RegistrationAdditionalInfoDTO
	 * registrationAdditionalInfoDTO, String templateTypeCode, Map<String, Object>
	 * attributes,String preferedLanguage) throws ApisResourceAccessException,
	 * IOException, JSONException { SmsResponseDto response; SmsRequestDto smsDto =
	 * new SmsRequestDto(); RequestWrapper<SmsRequestDto> requestWrapper = new
	 * RequestWrapper<>(); ResponseWrapper<?> responseWrapper;
	 * 
	 * regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(),
	 * LoggerFileConstant.USERID.toString(), registrationId,
	 * "NotificationUtility::sendSms()::entry"); try { InputStream in =
	 * templateGenerator.getTemplate(templateTypeCode, attributes,
	 * preferedLanguage); String artifact = IOUtils.toString(in, ENCODING);
	 * 
	 * smsDto.setNumber(registrationAdditionalInfoDTO.getPhone());
	 * smsDto.setMessage(artifact);
	 * 
	 * requestWrapper.setId(env.getProperty(SMS_SERVICE_ID));
	 * requestWrapper.setVersion(env.getProperty(REG_PROC_APPLICATION_VERSION));
	 * DateTimeFormatter format =
	 * DateTimeFormatter.ofPattern(env.getProperty(DATETIME_PATTERN)); LocalDateTime
	 * localdatetime = LocalDateTime
	 * .parse(DateUtils.getUTCCurrentDateTimeString(env.getProperty(DATETIME_PATTERN
	 * )), format); requestWrapper.setRequesttime(localdatetime);
	 * requestWrapper.setRequest(smsDto);
	 * 
	 * responseWrapper = (ResponseWrapper<?>)
	 * restClientService.postApi(ApiName.SMSNOTIFIER, "", "", requestWrapper,
	 * ResponseWrapper.class); response =
	 * mapper.readValue(mapper.writeValueAsString(responseWrapper.getResponse()),
	 * SmsResponseDto.class); } catch (TemplateNotFoundException |
	 * TemplateProcessingFailureException e) {
	 * regProcLogger.error(LoggerFileConstant.SESSIONID.toString(),
	 * LoggerFileConstant.REGISTRATIONID.toString(), registrationId,
	 * PlatformErrorMessages.RPR_SMS_TEMPLATE_GENERATION_FAILURE.name() +
	 * e.getMessage() + ExceptionUtils.getStackTrace(e)); throw new
	 * TemplateGenerationFailedException(
	 * PlatformErrorMessages.RPR_SMS_TEMPLATE_GENERATION_FAILURE.getCode(), e); }
	 * catch (ApisResourceAccessException e) {
	 * regProcLogger.error(LoggerFileConstant.SESSIONID.toString(),
	 * LoggerFileConstant.REGISTRATIONID.toString(), registrationId,
	 * PlatformErrorMessages.RPR_PGS_API_RESOURCE_NOT_AVAILABLE.name() +
	 * e.getMessage() + ExceptionUtils.getStackTrace(e)); throw new
	 * ApisResourceAccessException(PlatformErrorMessages.
	 * RPR_PGS_API_RESOURCE_NOT_AVAILABLE.name(), e); } return response; }
	 */
	
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
