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

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
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
import in.tf.nira.manual.verification.dto.CreateAppRequestDTO;
import in.tf.nira.manual.verification.dto.DataShareResponseDto;
import in.tf.nira.manual.verification.dto.DocumentDTO;
import in.tf.nira.manual.verification.dto.OfficerDetailDTO;
import in.tf.nira.manual.verification.dto.PacketDto;
import in.tf.nira.manual.verification.dto.PacketInfo;
import in.tf.nira.manual.verification.dto.PageResponseDto;
import in.tf.nira.manual.verification.dto.SchInterviewDTO;
import in.tf.nira.manual.verification.dto.SearchDto;
import in.tf.nira.manual.verification.dto.StatusResponseDTO;
import in.tf.nira.manual.verification.dto.UpdateStatusRequest;
import in.tf.nira.manual.verification.dto.UserApplicationsResponse;
import in.tf.nira.manual.verification.dto.UserDto;
import in.tf.nira.manual.verification.dto.UserResponse;
import in.tf.nira.manual.verification.dto.UserResponse.Response;
import in.tf.nira.manual.verification.entity.MVSApplication;
import in.tf.nira.manual.verification.entity.MVSApplicationHistory;
import in.tf.nira.manual.verification.entity.OfficerAssignment;
import in.tf.nira.manual.verification.exception.RequestException;
import in.tf.nira.manual.verification.helper.SearchHelper;
import in.tf.nira.manual.verification.listener.Listener;
import in.tf.nira.manual.verification.repository.MVSApplicationHistoryRepo;
import in.tf.nira.manual.verification.repository.MVSApplicationRepo;
import in.tf.nira.manual.verification.repository.OfficerAssignmentRepo;
import in.tf.nira.manual.verification.service.ApplicationService;
import in.tf.nira.manual.verification.util.CryptoCoreUtil;
import in.tf.nira.manual.verification.util.PageUtils;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.DateUtils;

@Service
public class ApplicationServiceImpl implements ApplicationService {
	private static final String PACKET_MANAGER_ID = "mosip.commmons.packetmanager";
    private static final String PACKET_MANAGER_VERSION = "v1";
    private static final String RESPONSE = "response";
    private static final String SCHEMA_JSON = "schemaJson";
	
	@Value("${manual.verification.user.details.url}")
    private String userDetailsUrl;
	
	@Value("${manual.verification.create.packet.url}")
    private String createPacketUrl;
	
	@Value("#{'${manual.verification.officer.roles}'.split(',')}")
    private List<String> officerRoles;
	
	@Value("${manual.verification.data.share.encryption:false}")
	private boolean encryption;
	
	@Value("${manual.verification.latest.schema.url}")
    private String schemaUrl;
	
	@Value("${manual.verification.packet.schema.version:0}")
	private String schemaVersion;
	
	@Value("${manual.verification.email.notification.url}")
    private String emailNotificationUrl;
	
	Map<String, List<OfficerDetailDTO>> officerDetailMap = new HashMap<>();
	
	private Map<String, String> schemajsonValue = null;

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
	
	@Autowired
	Listener listener;
	
	@Autowired
	SearchHelper searchHelper;
	
	@Autowired
	private PageUtils pageUtils;
	
	@PostConstruct
    public void runAtStartup() {
        fetchUsers();
    }
	
	@Override
	public StatusResponseDTO createApplication(CreateAppRequestDTO verifyRequest) throws Exception {
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

		StatusResponseDTO response = new StatusResponseDTO();
		response.setStatus("Success");
		return response;
	}
	
	@Override
	public List<UserApplicationsResponse> getApplicationsForUser(String userId) {
		List<MVSApplication> applications = mVSApplicationRepo.findByAssignedOfficerId(userId);
		List<UserApplicationsResponse> response = new ArrayList<>();
		
		buildUserApplicationsResponse(applications, response);
		
		return response;
	}
	
	@Override
	public ApplicationDetailsResponse getApplicationDetails(String applicationId) {
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
						throw new RequestException(errorCode, message);
					}
				}
			} catch (RequestException ex) {
				if (ex.getErrorCode().equalsIgnoreCase("DAT-SER-006"))
					throw new RequestException(FailureConstants.DATA_SHARE_URL_EXPIRED, "");
				else
					throw new RequestException(FailureConstants.UNEXPECTED_ERROR, "");
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
			throw new RequestException(FailureConstants.UNABLE_TO_FETCH_BIOMETRIC_DETAILS, "");
		} catch (URISyntaxException | IllegalArgumentException ex) {
			ex.printStackTrace();
			//logger.error("issue with httpclient URL Syntax " + ex.getLocalizedMessage());
			throw new RequestException(FailureConstants.UNABLE_TO_FETCH_BIOMETRIC_DETAILS, "");
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RequestException(((RequestException) ex).getErrors());
			//throw ex;
		}
	}
	
	@Override
	public StatusResponseDTO updateApplicationStatus(String applicationId, UpdateStatusRequest request) {
		Optional<MVSApplication> optional = mVSApplicationRepo.findById(applicationId);
		MVSApplication application = optional.get();
		
		if(request.getStatus().equals(CommonConstants.APPROVE_STATUS)) {
			application.setStage(StageCode.APPROVED.getStage());
			application.getComments().put(application.getAssignedOfficerRole(), request.getComment());
			application.setComments(application.getComments());
			mVSApplicationRepo.save(application);
		}
		else if(request.getStatus().equals(CommonConstants.REJECT_STATUS)) {
			sendEmail(applicationId, "", "");
			application.setStage(StageCode.REJECTED.getStage());
			application.getComments().put(application.getAssignedOfficerRole(), request.getComment());
			application.setComments(application.getComments());
			application.setRejectionCategory(request.getRejectionCategory());
			mVSApplicationRepo.save(application);
		}
		else if(request.getStatus().equals(CommonConstants.ESCALATE_STATUS)) {
			if(application.getAssignedOfficerRole().equals(CommonConstants.MVS_SUPERVISOR_ROLE) || (request.getInsufficientDocuments() != null && request.getInsufficientDocuments())) {
				escalateApplication(application, CommonConstants.MVS_DISTRICT_OFFICER_ROLE, 
						StageCode.ASSIGNED_TO_DISTRICT_OFFICER.getStage(), CommonConstants.MVS_SUPERVISOR_ROLE, request.getComment());
			}
			else if(application.getAssignedOfficerRole().equals(CommonConstants.MVS_OFFICER_ROLE)) {
				escalateApplication(application, CommonConstants.MVS_SUPERVISOR_ROLE, 
						StageCode.ASSIGNED_TO_SUPERVISOR.getStage(), CommonConstants.MVS_OFFICER_ROLE, request.getComment());
			}
			else {
				//cannot escalate
			}
		}
		
		StatusResponseDTO response = new StatusResponseDTO();
		response.setStatus("Success");
		return response;
	}
	
	@Override
	public StatusResponseDTO scheduleInterview(String applicationId, SchInterviewDTO schInterviewDTO) {
		Optional<MVSApplication> optional = mVSApplicationRepo.findById(applicationId);
		MVSApplication application = optional.get();
		StatusResponseDTO response = new StatusResponseDTO();
		
		if(application.getAssignedOfficerRole().equals(CommonConstants.MVS_DISTRICT_OFFICER_ROLE) || 
				application.getAssignedOfficerRole().equals(CommonConstants.MVS_LEGAL_OFFICER_ROLE)) {
			sendEmail(applicationId, schInterviewDTO.getSubject(), schInterviewDTO.getContent());
			//sendSMS();
			application.setStage(StageCode.INTERVIEW_SCHEDULED.getStage());
			mVSApplicationRepo.save(application);
			
			response.setStatus("Success");
		}
		else {
			response.setStatus("Failure");
		}
		
		return response;
	}
	
	@Override
	public StatusResponseDTO uploadDocuments(String applicationId, DocumentDTO documentDTO) {
		Optional<MVSApplication> applicationOpp = mVSApplicationRepo.findById(applicationId);
		MVSApplication application = applicationOpp.get();
		
		StatusResponseDTO response = new StatusResponseDTO();
		if(application.getAssignedOfficerRole().equals(CommonConstants.MVS_DISTRICT_OFFICER_ROLE)) {
			uploadToPacketManager(application, documentDTO);
			application.setStage(StageCode.APPROVED.getStage());
			mVSApplicationRepo.save(application);
			
			response.setStatus("Success");
		}
		else {
			response.setStatus("Failure");
		}
		
		return response;
	}
	
	@Override
	public PageResponseDto<UserApplicationsResponse> searchApplications(SearchDto dto) {
		List<UserApplicationsResponse> applicationsResponse = new ArrayList<>();
		PageResponseDto<UserApplicationsResponse> pageDto = new PageResponseDto<>();
		
		Page<MVSApplication> page = searchHelper.search(MVSApplication.class, dto);
		if (page.getContent() != null && !page.getContent().isEmpty()) {
			buildUserApplicationsResponse(page.getContent(), applicationsResponse);
			pageDto = pageUtils.sortPage(applicationsResponse, dto.getSort(), dto.getPagination(), page.getTotalElements());
		}
		return pageDto;
	}
	
	private void uploadToPacketManager(MVSApplication application, DocumentDTO documentDTO) {
		PacketDto packetDto = new PacketDto();
    	packetDto.setId(application.getRegId());
    	packetDto.setSource("REGISTRATION_CLIENT");
    	packetDto.setProcess("MVS_DOC");
    	packetDto.setRefId("10002_10034");
    	packetDto.setSchemaVersion(schemaVersion);
    	packetDto.setSchemaJson(getSchemaJson());
    	
    	List<Map<String, String>> audits = new ArrayList<>();
    	Map<String, String> audit = new HashMap<>();
    	audit.put("id", application.getRegId());
    	audits.add(audit);
    	packetDto.setAudits(audits);
    	
    	packetDto.setDocuments(documentDTO.getDocuments());
    	
    	RequestWrapper<PacketDto> request = new RequestWrapper<>();
        request.setId(PACKET_MANAGER_ID);
        request.setVersion(PACKET_MANAGER_VERSION);
        request.setRequesttime(DateUtils.getUTCCurrentDateTime());
        request.setRequest(packetDto);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RequestWrapper<PacketDto>> httpEntity = new HttpEntity<>(request, headers);
        
        ResponseEntity<ResponseWrapper<List<PacketInfo>>> response = restTemplate.exchange(createPacketUrl, HttpMethod.PUT, httpEntity, 
        		new ParameterizedTypeReference<ResponseWrapper<List<PacketInfo>>>() {});
        
	}
	
	private String getSchemaJson() {
		if (schemajsonValue != null && !schemajsonValue.isEmpty() && schemajsonValue.get(schemaVersion) != null)
			return schemajsonValue.get(schemaVersion);
			
		String url = schemaUrl + "?schemaVersion=" + schemaVersion;
		ResponseEntity<String> responseSchemaJson = null;
		try {
			responseSchemaJson = restTemplate.exchange(url, HttpMethod.GET, null,
				 String.class);
		} catch (Exception e) {
			//throw new ApiNotAccessibleException("Could not fetch schemajson with version : " + version);
		}

		//if (responseSchemaJson == null)
			//throw new ApiNotAccessibleException("Could not fetch schemajson with version : " + version);

		String responseString = null;
		try {
			JSONObject jsonObject = new JSONObject(responseSchemaJson.getBody());
			JSONObject respObj = (JSONObject) jsonObject.get(RESPONSE);
			responseString = respObj != null ? (String) respObj.get(SCHEMA_JSON) : null;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		if (responseString != null) {
			if (schemajsonValue == null) {
				schemajsonValue = new HashMap<>();
				schemajsonValue.put(schemaVersion, responseString);
			} else
				schemajsonValue.put(schemaVersion, responseString);
		}

		return schemajsonValue.get(schemaVersion);
	}
	
	private void buildUserApplicationsResponse(List<MVSApplication> applications, List<UserApplicationsResponse> applicationsResponse) {
		applications.stream().filter(app -> !app.getStage().equals(StageCode.APPROVED.getStage()) && 
				!app.getStage().equals(StageCode.REJECTED.getStage())).forEach(app -> {
			UserApplicationsResponse userApp = new UserApplicationsResponse();
			userApp.setApplicationId(app.getRegId());
			userApp.setService(app.getService());
			userApp.setServiceType(app.getServiceType());
			userApp.setStatus(app.getStage());
			userApp.setCrDTimes(app.getCrDTimes());
			userApp.setOfficerEscReason(app.getComments().get(CommonConstants.MVS_OFFICER_ROLE));
			userApp.setSupervisorEscReason(app.getComments().get(CommonConstants.MVS_SUPERVISOR_ROLE));
			
			applicationsResponse.add(userApp);
		});
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
	
	private void escalateApplication(MVSApplication application, String assignedRole, String stage, String role, String comment) {
		OfficerAssignment officerAssignment = officerAssignmentRepo.findByUserRole(assignedRole);
		if(officerAssignment == null) {
			officerAssignment = new OfficerAssignment();
		}
		OfficerDetailDTO selectedOfficer = fetchOfficerAssignment(assignedRole, officerAssignment);
		
		if(selectedOfficer != null) {
			MVSApplicationHistory appHistory = getAppHistoryEntity(application);
			application.setAssignedOfficerId(selectedOfficer.getUserId());
			application.setAssignedOfficerName(selectedOfficer.getUserName());
			application.setAssignedOfficerRole(selectedOfficer.getUserRole());
			application.setStage(stage);
			application.getComments().put(role, comment);
			application.setComments(application.getComments());
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
			
			String mailTo = "abc@gmail.com";//appResponse.getDemographics().get("email");
			LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();

			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(emailNotificationUrl)
			        .queryParam("mailTo", mailTo)
			        .queryParam("mailSubject", subjectArtifact)
			        .queryParam("mailContent", artifact);

			params.add("attachments", null);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);

			HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(params, headers);

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
