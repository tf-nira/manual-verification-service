package in.tf.nira.manual.verification.service.impl;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.tf.nira.manual.verification.constant.CommonConstants;
import in.tf.nira.manual.verification.constant.ErrorCode;
import in.tf.nira.manual.verification.constant.StageCode;
import in.tf.nira.manual.verification.dto.ApplicationDetailsResponse;
import in.tf.nira.manual.verification.dto.CreateAppRequestDTO;
import in.tf.nira.manual.verification.dto.DataShareResponseDto;
import in.tf.nira.manual.verification.dto.DemograhicValue;
import in.tf.nira.manual.verification.dto.DemographicDetailsDTO;
import in.tf.nira.manual.verification.dto.DocumentDTO;
import in.tf.nira.manual.verification.dto.EscalationDetailsDTO;
import in.tf.nira.manual.verification.dto.OfficerDetailDTO;
import in.tf.nira.manual.verification.dto.PacketDto;
import in.tf.nira.manual.verification.dto.PacketInfo;
import in.tf.nira.manual.verification.dto.PageResponseDto;
import in.tf.nira.manual.verification.dto.SMSRequestDTO;
import in.tf.nira.manual.verification.dto.SchInterviewDTO;
import in.tf.nira.manual.verification.dto.SearchDto;
import in.tf.nira.manual.verification.dto.StatusResponseDTO;
import in.tf.nira.manual.verification.dto.UpdateStatusRequest;
import in.tf.nira.manual.verification.dto.UserApplicationsResponse;
import in.tf.nira.manual.verification.entity.MVSApplication;
import in.tf.nira.manual.verification.entity.MVSApplicationHistory;
import in.tf.nira.manual.verification.entity.OfficerAssignment;
import in.tf.nira.manual.verification.exception.ApiNotAccessibleException;
import in.tf.nira.manual.verification.exception.RequestException;
import in.tf.nira.manual.verification.helper.SearchHelper;
import in.tf.nira.manual.verification.listener.Listener;
import in.tf.nira.manual.verification.repository.MVSApplicationHistoryRepo;
import in.tf.nira.manual.verification.repository.MVSApplicationRepo;
import in.tf.nira.manual.verification.repository.OfficerAssignmentRepo;
import in.tf.nira.manual.verification.service.ApplicationService;
import in.tf.nira.manual.verification.service.CbeffUtil;
import in.tf.nira.manual.verification.util.CbeffToBiometricUtil;
import in.tf.nira.manual.verification.util.CryptoCoreUtil;
import in.tf.nira.manual.verification.util.PageUtils;
import in.tf.nira.manual.verification.util.UserDetailUtil;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.DateUtils;

@Service
public class ApplicationServiceImpl implements ApplicationService {
	private static final Logger logger = LoggerFactory.getLogger(ApplicationServiceImpl.class);
	
	private static final String PACKET_MANAGER_ID = "mosip.commmons.packetmanager";
    private static final String PACKET_MANAGER_VERSION = "v1";
    private static final String RESPONSE = "response";
    private static final String SCHEMA_JSON = "schemaJson";
    private static final String SYSTEM = "System";
	/** The cbeffutil. */
	@Autowired
	private CbeffUtil cbeffutil;
	/** The Constant FACE. */
	private static final String FACE = "Face";
	@Value("${manual.verification.user.details.url}")
    private String userDetailsUrl;
	/** The Constant APPLICANT_PHOTO. */
	private static final String APPLICANT_PHOTO = "ApplicantPhoto";
	@Value("${manual.verification.create.packet.url}")
    private String createPacketUrl;
	
	@Value("#{'${manual.verification.officer.roles}'.split(',')}")
    private List<String> officerRoles;

	@Value("#{'${manual.verification.officerAssignment.serviceTypesForLegalOfficer}'.split(',')}")
	private List<String> serviceTypesForLegalOfficer;
	
	@Value("${manual.verification.data.share.encryption:false}")
	private boolean encryption;
	
	@Value("${manual.verification.latest.schema.url}")
    private String schemaUrl;
	
	@Value("${manual.verification.id.repo.url}")
    private String idRepoUrl;
	
	@Value("${manual.verification.email.notification.url}")
    private String emailNotificationUrl;
	
	@Value("${manual.verification.sms.notification.url}")
    private String smsNotificationUrl;
	
	@Value("${manual.verification.document.upload.process}")
	private String documentUploadProcess;
	
	private Map<String, List<OfficerDetailDTO>> officerDetailMap = new HashMap<>();
	
	private Map<String, String> schemajsonValue = null;

	private Map<String, List<OfficerDetailDTO>> districtOfficerMap = new HashMap<>();

	private Map<String, String> districtOfficerAssignment = new HashMap<>();

	@Autowired(required = true)
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate restTemplate;
	
	@Qualifier(value = "keycloakRestTemplate")
	@Autowired
	private RestTemplate keycloakRestTemplate;
	
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
	public StatusResponseDTO createApplication(CreateAppRequestDTO verifyRequest) {
		logger.info("Application received for manual verification in mvs with reg id: " + verifyRequest.getRegId());

		String officerRole = "";

		if (verifyRequest.getService().equals("COP") && serviceTypesForLegalOfficer.contains(verifyRequest.getServiceType())) {
			officerRole = CommonConstants.MVS_LEGAL_OFFICER_ROLE;
		} else {
			officerRole = CommonConstants.MVS_OFFICER_ROLE;
		}

		OfficerAssignment officerAssignment = officerAssignmentRepo.findByUserRole(officerRole);
		if(officerAssignment == null) {
			officerAssignment = new OfficerAssignment();
		}
		OfficerDetailDTO selectedOfficer = fetchOfficerForAssignment(officerRole, officerAssignment, null);
		
		if(selectedOfficer != null) {
			logger.info("Assigning application to officer: " + selectedOfficer.getUserId());
			MVSApplication mVSApplication = new MVSApplication();
			mVSApplication.setRegId(verifyRequest.getRegId());
			mVSApplication.setService(verifyRequest.getService());
			mVSApplication.setServiceType(verifyRequest.getServiceType());
			mVSApplication.setReferenceURL(verifyRequest.getReferenceURL());
			mVSApplication.setSource(verifyRequest.getSource());
			mVSApplication.setRefId(verifyRequest.getRefId());
			mVSApplication.setSchemaVersion(verifyRequest.getSchemaVersion());
			mVSApplication.setAssignedOfficerId(selectedOfficer.getUserId());
			mVSApplication.setAssignedOfficerName(selectedOfficer.getUserName());
			mVSApplication.setAssignedOfficerRole(selectedOfficer.getUserRole());
			mVSApplication.setStage(StageCode.ASSIGNED_TO_OFFICER.getStage());
			mVSApplication.setCreatedBy(SYSTEM);
			mVSApplication.setCrDTimes(LocalDateTime.now());
			
			mVSApplicationRepo.save(mVSApplication);
			
			if(officerAssignment.getCrDTimes() == null) {
				officerAssignment.setCreatedBy(SYSTEM);
				officerAssignment.setCrDTimes(LocalDateTime.now());
			}
			else {
				officerAssignment.setUpdatedBy(SYSTEM);
				officerAssignment.setUpdatedTimes(LocalDateTime.now());
			}
			officerAssignmentRepo.save(officerAssignment);
			
			logger.info("Application assigned to officer: " + selectedOfficer.getUserId());
		}

		StatusResponseDTO response = new StatusResponseDTO();
		response.setStatus("Success");
		return response;
	}
	
	@Override
	public List<UserApplicationsResponse> getApplicationsForUser(String userId) {
		logger.info("Fetching applications for user: " + userId);
		List<MVSApplication> applications = mVSApplicationRepo.findByAssignedOfficerId(userId);
		
		if (applications == null || applications.isEmpty()) {
			logger.error("No applications available for the user: " + userId);
			throw new RequestException(ErrorCode.NO_APPS_FOR_USER.getErrorCode(), 
					ErrorCode.NO_APPS_FOR_USER.getErrorMessage());
	    }
		
		return buildUserApplicationsResponse(applications);
	}
	
	@Override
	public PageResponseDto<UserApplicationsResponse> searchApplications(SearchDto dto) {
		logger.info("Search started");
		Page<MVSApplication> page = searchHelper.search(MVSApplication.class, dto);
		logger.info("Search completed, total records found: " + page.getTotalElements());

	    List<UserApplicationsResponse> applicationsResponse = page.getContent() != null
	            ? buildUserApplicationsResponse(page.getContent())
	            : new ArrayList<>();

	    logger.info("Sorting and pagination for searched records");
	    return pageUtils.sortPage(applicationsResponse, dto.getSort(), dto.getPagination(), page.getTotalElements());
	}
	
	@Override
	public ApplicationDetailsResponse getApplicationDetails(String applicationId) {
		logger.info("Fetching application for ID: {}", applicationId);
        
        MVSApplication application = getApplicationById(applicationId);
		
	    return getApplicationDetails(application);
	}

	@Override
	public StatusResponseDTO updateApplicationStatus(String applicationId, UpdateStatusRequest request) {
		logger.info("Updating application status for ID: {}", applicationId);
		
		MVSApplication application = getApplicationById(applicationId);
		
		switch (request.getStatus()) {
			case CommonConstants.APPROVE_STATUS:
				approveApplication(application, request.getComment());
				break;
			case CommonConstants.REJECT_STATUS:
				rejectApplication(application, request.getComment(), request.getCategory());
				break;
			case CommonConstants.ESCALATE_STATUS:
				if (request.getSelectedOfficerLevel() != null && request.getSelectedOfficerLevel().equals(CommonConstants.MVS_LEGAL_OFFICER_ROLE)) {
					escalateApplication(application, CommonConstants.MVS_LEGAL_OFFICER_ROLE,
							StageCode.ASSIGNED_TO_LEGAL_OFFICER.getStage(), request, null);
				}
//				else if(application.getAssignedOfficerRole().equals(CommonConstants.MVS_SUPERVISOR_ROLE) ||
//						(request.getInsufficientDocuments() != null && request.getInsufficientDocuments())) {
//					ApplicationDetailsResponse appResponse = getApplicationDetails(application);
//					String district = getDemoValue(appResponse.getDemographics().get("applicantPlaceOfResidenceDistrict"));
//
//					escalateApplication(application, CommonConstants.MVS_DISTRICT_OFFICER_ROLE,
//							StageCode.ASSIGNED_TO_DISTRICT_OFFICER.getStage(), request, district);
//				}
				else if(request.getSelectedOfficerLevel() != null && request.getSelectedOfficerLevel().equals(CommonConstants.MVS_DISTRICT_OFFICER_ROLE) ||
						(request.getInsufficientDocuments() != null && request.getInsufficientDocuments())) {
					ApplicationDetailsResponse appResponse = getApplicationDetails(application);
					String district = getDemoValue(appResponse.getDemographics().get("applicantPlaceOfResidenceDistrict"));

					escalateApplication(application, CommonConstants.MVS_DISTRICT_OFFICER_ROLE,
							StageCode.ASSIGNED_TO_DISTRICT_OFFICER.getStage(), request, district);
				}
				else if(request.getSelectedOfficerLevel() != null && request.getSelectedOfficerLevel().equals(CommonConstants.MVS_SUPERVISOR_ROLE)) {
					escalateApplication(application, CommonConstants.MVS_SUPERVISOR_ROLE,
							StageCode.ASSIGNED_TO_SUPERVISOR.getStage(), request, null);
				}
				else if(request.getSelectedOfficerLevel() != null && request.getSelectedOfficerLevel().equals(CommonConstants.MVS_EXECUTIVE_DIRECTOR)) {
					escalateApplication(application, CommonConstants.MVS_EXECUTIVE_DIRECTOR,
							StageCode.ASSIGNED_TO_EXECUTIVE_DIRECTOR.getStage(), request, null);
				}
				else {
					logger.error("Application already escalated to highest level");
					throw new RequestException(ErrorCode.ESCALATION_NOT_ALLOWED.getErrorCode(), 
							ErrorCode.ESCALATION_NOT_ALLOWED.getErrorMessage());
				}
				break;
			default:
				throw new RequestException(
	                    ErrorCode.INVALID_STATUS_VALUE.getErrorCode(),
	                    ErrorCode.INVALID_STATUS_VALUE.getErrorMessage()
	            );
		}
		
		logger.info("Application status updated for ID: {}", applicationId);
		
		StatusResponseDTO response = new StatusResponseDTO();
		response.setStatus("Success");
		return response;
	}
	
	@Override
	public StatusResponseDTO scheduleInterview(String applicationId, SchInterviewDTO schInterviewDTO) {
		logger.info("Scheduling interview for ID: {}", applicationId);
		
		MVSApplication application = getApplicationById(applicationId);
		
		if(application.getAssignedOfficerRole().equals(CommonConstants.MVS_DISTRICT_OFFICER_ROLE) ||
				application.getAssignedOfficerRole().equals(CommonConstants.MVS_LEGAL_OFFICER_ROLE) ||
				application.getAssignedOfficerRole().equals(CommonConstants.MVS_EXECUTIVE_DIRECTOR)) {
			sendNotification(application, schInterviewDTO.getSubject(), schInterviewDTO.getContent());
			scheduleInterview(application, schInterviewDTO.getDistrict());
		}
		else {
			logger.error("{} not allowed to schedule interview", application.getAssignedOfficerRole());
			throw new RequestException(ErrorCode.SCHEDULE_INTERVIEW_NOT_ALLOWED.getErrorCode(), 
					String.format(ErrorCode.SCHEDULE_INTERVIEW_NOT_ALLOWED.getErrorMessage(), application.getAssignedOfficerRole()));
		}
		
		logger.info("Interview scheduled for ID: {}", applicationId);
		
		StatusResponseDTO response = new StatusResponseDTO();
		response.setStatus("Success");
		return response;
	}
	
	@Override
	public StatusResponseDTO uploadDocuments(String applicationId, DocumentDTO documentDTO) {
		logger.info("Uploading documents for Id: {}", applicationId);
		
		MVSApplication application = getApplicationById(applicationId);
		
		if(application.getAssignedOfficerRole().equals(CommonConstants.MVS_DISTRICT_OFFICER_ROLE)) {
			uploadToPacketManager(application, documentDTO);
			approveApplication(application, "Documents uploaded");
		}
		else {
			logger.error("{} not allowed to upload documents", application.getAssignedOfficerRole());
			throw new RequestException(ErrorCode.DOCUMENT_UPLOAD_NOT_ALLOWED.getErrorCode(), 
					String.format(ErrorCode.DOCUMENT_UPLOAD_NOT_ALLOWED.getErrorMessage(), application.getAssignedOfficerRole()));
		}
		
		logger.info("Documents uploaded for Id: {}", applicationId);
		
		StatusResponseDTO response = new StatusResponseDTO();
		response.setStatus("Success");
		return response;
	}
	
	private OfficerDetailDTO fetchOfficerForAssignment(String role, OfficerAssignment officerAssignment, String district) {
		if (officerDetailMap == null || officerDetailMap.isEmpty()) {
			fetchUsers();
		}
		
		logger.info("Fetching officer with role: {} for assignment", role);
		
		List<OfficerDetailDTO> officers = officerDetailMap.get(role);
		
		if (officers == null || officers.isEmpty()) {
			logger.error("No Officer available for assignment, for role: " + role);
			throw new RequestException(ErrorCode.OFFICER_FOR_ROLE_NOT_AVAILABLE.getErrorCode(),
					String.format(ErrorCode.OFFICER_FOR_ROLE_NOT_AVAILABLE.getErrorMessage(), role));
	    }
		
		if (CommonConstants.MVS_DISTRICT_OFFICER_ROLE.equals(role)) {
			//fetch officer by district
			if (district != null) {
				List<OfficerDetailDTO>disOfficers = districtOfficerMap.get(district);
				
				if (disOfficers != null && !disOfficers.isEmpty()) {

					String nextOfficerId = districtOfficerAssignment.get(district);

					OfficerDetailDTO assignedOfficer = disOfficers.stream()
							.filter(o -> o.getUserId().equals(nextOfficerId))
							.findFirst()
							.orElse(disOfficers.get(0));

					int currentIndex = disOfficers.indexOf(assignedOfficer);
					int newNextOfficerIndex = (currentIndex + 1) % disOfficers.size();
					districtOfficerAssignment.put(district, disOfficers.get(newNextOfficerIndex).getUserId());

					return assignedOfficer;

				} else {
					throw new RequestException(ErrorCode.NO_OFFICER_FOR_DISTRICT.getErrorCode(),
							String.format(ErrorCode.NO_OFFICER_FOR_DISTRICT.getErrorMessage(), district));
				}
			} else {
				throw new RequestException(ErrorCode.DISTRICT_NOT_PRESENT.getErrorCode(),
						ErrorCode.DISTRICT_NOT_PRESENT.getErrorMessage());
			}
		}
		
		Optional<OfficerDetailDTO> optionalOff;
		if (officerAssignment.getUserId() == null) {
			optionalOff = officers.stream().findFirst();
			officerAssignment.setId(UUID.randomUUID().toString());
			officerAssignment.setUserRole(role);
		}
		else {
			String userId = officerAssignment.getUserId();
			optionalOff = officers.stream().filter(officer -> officer.getUserId().equals(userId)).findFirst();
		}
		
		if (optionalOff.isPresent()) {
			OfficerDetailDTO selectedOfficer = optionalOff.get();
			int currentIndex = officers.indexOf(selectedOfficer);
			
			OfficerDetailDTO nextOfficer = officers.get((currentIndex + 1) % officers.size());
			officerAssignment.setUserId(nextOfficer.getUserId());
			return selectedOfficer;
		}
		else {
			logger.error("No Officer available for assignment");
			throw new RequestException(ErrorCode.OFFICER_FOR_ID_NOT_AVAILABLE.getErrorCode(),
					ErrorCode.OFFICER_FOR_ID_NOT_AVAILABLE.getErrorMessage());
		}
	}

	private List<UserApplicationsResponse> buildUserApplicationsResponse(List<MVSApplication> applications) {
	    return applications.stream()
	            .map(this::mapToUserApplicationsResponse)
	            .collect(Collectors.toList());
	}
	
	private UserApplicationsResponse mapToUserApplicationsResponse(MVSApplication app) {
	    UserApplicationsResponse userApp = new UserApplicationsResponse();
	    userApp.setApplicationId(app.getRegId());
	    userApp.setService(app.getService());
	    userApp.setServiceType(app.getServiceType());
	    userApp.setStatus(app.getStage());
	    userApp.setCrDTimes(app.getCrDTimes());
	    
	    if (app.getEscalationDetails() != null) {
	        app.getEscalationDetails().forEach(esc -> {
	            if (CommonConstants.MVS_OFFICER_ROLE.equals(esc.getLevel())) {
	                userApp.setOfficerEscDetails(esc);
	            } else if (CommonConstants.MVS_SUPERVISOR_ROLE.equals(esc.getLevel())) {
	                userApp.setSupervisorEscDetails(esc);
	            } else if (CommonConstants.MVS_LEGAL_OFFICER_ROLE.equals(esc.getLevel())) {
					userApp.setLegalEscDetails(esc);
				}
	        });
	    }
	    
	    return userApp;
	}
	
	private MVSApplication getApplicationById(String applicationId) {
		return mVSApplicationRepo.findById(applicationId).orElseThrow(() -> {
			logger.error("Invalid application ID: {}", applicationId);
			return new RequestException(ErrorCode.INVALID_APP_ID.getErrorCode(),
					ErrorCode.INVALID_APP_ID.getErrorMessage());
		});
	}
	
	private ApplicationDetailsResponse getApplicationDetails(MVSApplication application) {
		try {
	        logger.info("Fetching application details, data share URL: {}", application.getReferenceURL());
	        
	        ResponseEntity<String> responseEn = restTemplate.exchange(application.getReferenceURL(), HttpMethod.GET, null, String.class);
	        String response = responseEn.getBody();

	        if (response == null) {
	            throw new RequestException(ErrorCode.DATA_SHARE_FETCH_FAILED.getErrorCode(), 
	                    ErrorCode.DATA_SHARE_FETCH_FAILED.getErrorMessage() + " with status code: " + responseEn.getStatusCodeValue());
	        }

	        handleResponseErrors(response);
	        
	        if (encryption) {
	        	logger.info("Decrypting response from data share");
	            response = cryptoUtil.decrypt(response);
	        }
	        
	        DataShareResponseDto dataShareResponse = objectMapper.readValue(response, DataShareResponseDto.class);

	        ApplicationDetailsResponse applicationDetailsResponse = new ApplicationDetailsResponse();
	        //set photo private boolean setApplicantPhoto
	        if(dataShareResponse.getBiometrics()!=null) {
	        	 Map<String, Object> attributes= new HashMap<String, Object>();
	        	CbeffToBiometricUtil util = new CbeffToBiometricUtil(cbeffutil);
				List<String> subtype = new ArrayList<>();
				//value=dataShareResponse.getBiometrics()
				byte[] photoByte = util.getImageBytes(dataShareResponse.getBiometrics(), FACE, subtype);
				if (photoByte != null) {
					String data = java.util.Base64.getEncoder().encodeToString(extractFaceImageData(photoByte));
					attributes.put(APPLICANT_PHOTO, "data:image/png;base64," + data);
					applicationDetailsResponse.setBiometricAttributes(attributes);
				}
	        }
		    applicationDetailsResponse.setApplicationId(application.getRegId());
		    applicationDetailsResponse.setService(application.getService());
		    applicationDetailsResponse.setServiceType(application.getServiceType());
		    applicationDetailsResponse.setDemographics(dataShareResponse.getIdentity());
		    applicationDetailsResponse.setDocuments(dataShareResponse.getDocuments());
		    
		    logger.info("Successfully fetched application details for ID: {}", application.getRegId());
		    return applicationDetailsResponse;
	    } catch (HttpClientErrorException ex) {
	        logger.error("Invalid data share url: {}", ex.getLocalizedMessage(), ex);
	        throw new RequestException(ErrorCode.DATA_SHARE_FETCH_FAILED.getErrorCode(), ErrorCode.DATA_SHARE_FETCH_FAILED.getErrorMessage());
	    } catch (URISyntaxException | IllegalArgumentException ex) {
	        logger.error("Invalid data share url syntax: {}", ex.getLocalizedMessage(), ex);
	        throw new RequestException(ErrorCode.DATA_SHARE_FETCH_FAILED.getErrorCode(), ErrorCode.DATA_SHARE_FETCH_FAILED.getErrorMessage());
	    } catch (Exception ex) {
	        if (ex instanceof RequestException) {
	            throw new RequestException(((RequestException) ex).getErrors());
	        } else {
	        	logger.error("Unexpected error occurred: {}", ex.getLocalizedMessage(), ex);
	            throw new RequestException(ErrorCode.DATA_SHARE_FETCH_FAILED.getErrorCode(), ErrorCode.DATA_SHARE_FETCH_FAILED.getErrorMessage());
	        }
	    }
	}
	
	private String getDemoValue(String demoString) {
		if(demoString == null)
			return null;
		
		String value = null;
		
		try {
			List<DemograhicValue> demoValues = objectMapper.readValue(demoString, new TypeReference<List<DemograhicValue>>() {});
			value = demoValues.get(0).getValue();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		return value;
	}
	
	private void handleResponseErrors(String response) {
		try {
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(response);
			JSONArray errors = (JSONArray) json.get("errors");

			if (errors != null) {
			    for (Object errorObj : errors) {
			        JSONObject error = (JSONObject) errorObj;
			        String errorCode = ((String) error.get("errorCode")).trim();
			        String message = ((String) error.get("message")).trim();
			        logger.error("Error while fetching data share url, ErrorCode[{}], ErrorMessage[{}]", errorCode, message);
			        
			        throw new RequestException(errorCode, message);
			    }
			}
		} catch (RequestException ex) {
			throw ex;
		} catch (Exception ex) {
			// ex.printStackTrace();
		}
	}
	
	private void approveApplication(MVSApplication application, String comment) {
		logger.info("Approving application");
		application.setStage(StageCode.APPROVED.getStage());
		application.setComments(comment);
		application.setIsDeleted(true);
		application.setUpdatedBy(UserDetailUtil.getLoggedInUserId());
		application.setUpdatedTimes(LocalDateTime.now());
		mVSApplicationRepo.save(application);
		
		//send back to mvs stage
		logger.info("Notifying mvs stage for approval");
		try {
			StatusResponseDTO response = new StatusResponseDTO();
			response.setStatus(StageCode.APPROVED.getStage());
			ResponseEntity<Object> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
			listener.sendToQueue(responseEntity, 1);
		} catch (JsonProcessingException | UnsupportedEncodingException e) {
			logger.error("Unable to send response to mvs stage, {}", e);
		}
	}
	
	private void rejectApplication(MVSApplication application, String comment, String rejectionCategory) {
		logger.info("Rejecting application");
		application.setStage(StageCode.REJECTED.getStage());
		application.setComments(comment);
		application.setRejectionCategory(rejectionCategory);
		application.setIsDeleted(true);
		application.setUpdatedBy(UserDetailUtil.getLoggedInUserId());
		application.setUpdatedTimes(LocalDateTime.now());
		mVSApplicationRepo.save(application);
		
		//send back to mvs stage
		logger.info("Notifying mvs stage for rejection");
		try {
			StatusResponseDTO response = new StatusResponseDTO();
			response.setStatus(StageCode.REJECTED.getStage());
			ResponseEntity<Object> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
			listener.sendToQueue(responseEntity, 1);
		} catch (JsonProcessingException | UnsupportedEncodingException e) {
			logger.error("Unable to send response to mvs stage, {}", e);
		}
	}
	
	private void sendNotification(MVSApplication application, String subject, String content) {
		ApplicationDetailsResponse appResponse = getApplicationDetails(application);
        String email = getDemoValue(appResponse.getDemographics().get("email"));
        String phone = getDemoValue(appResponse.getDemographics().get("phone"));
        
		if (email != null) {
			sendEmail(email, subject, content);
		} else {
			logger.warn("Email Id not available for the application");
		}
		
		if (phone != null) {
			sendSMS(phone, content);
		} else {
			logger.warn("Phone number not available for the application");
		}
	}
	
	private void sendEmail(String mailTo, String subjectArtifact, String artifact) {
		logger.info("Sending email notification");
		try {
	        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(emailNotificationUrl)
	                .queryParam("mailTo", mailTo)
	                .queryParam("mailSubject", subjectArtifact)
	                .queryParam("mailContent", artifact);

	        LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
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

	        if (responseEntity.getBody() == null) {
	            logger.error("Failed to send email notification. Status code: " + responseEntity.getStatusCodeValue());
	            throw new RequestException(ErrorCode.EMAIL_NOTIFICATION_FAILED.getErrorCode(),
						ErrorCode.EMAIL_NOTIFICATION_FAILED.getErrorMessage() + "with status code: " + responseEntity.getStatusCodeValue());
	        }

	        ResponseWrapper<?> responseWrapper = responseEntity.getBody();

	        if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
	        	logger.error("Email notification error: {}", responseWrapper.getErrors().get(0));
	            throw new RequestException(ErrorCode.FAILED_EMAIL_NOTIFICATION_RESPONSE.getErrorCode(),
						ErrorCode.FAILED_EMAIL_NOTIFICATION_RESPONSE.getErrorMessage() + "with error: " + responseWrapper.getErrors().get(0));
	        }

	        logger.info("Email sent successfully");

	    } catch (RequestException ex) {
	        throw ex;
	    } catch (Exception ex) {
	    	logger.error("Failed to send email notification, {}", ex);
            throw new RequestException(ErrorCode.EMAIL_NOTIFICATION_FAILED.getErrorCode(),
					ErrorCode.EMAIL_NOTIFICATION_FAILED.getErrorMessage() + "with error: " + ex.getMessage());
	    }
	}
	
	private void escalateApplication(MVSApplication application, String roleToAssign, String stage,
			UpdateStatusRequest request, String district) {
		logger.info("Escalating application to next level");
		
		OfficerAssignment officerAssignment = null;
		if (!CommonConstants.MVS_DISTRICT_OFFICER_ROLE.equals(roleToAssign)) {
			officerAssignment = officerAssignmentRepo.findByUserRole(roleToAssign);
		}
		
		if (officerAssignment == null) {
			officerAssignment = new OfficerAssignment();
		}
		
		OfficerDetailDTO selectedOfficer = fetchOfficerForAssignment(roleToAssign, officerAssignment, district);
		
		if(selectedOfficer != null) {
			String assignedRole = application.getAssignedOfficerRole();
			MVSApplicationHistory appHistory = getAppHistoryEntity(application);
			application.setAssignedOfficerId(selectedOfficer.getUserId());
			application.setAssignedOfficerName(selectedOfficer.getUserName());
			application.setAssignedOfficerRole(selectedOfficer.getUserRole());
			application.setStage(stage);
			
			List<EscalationDetailsDTO> escDetails = application.getEscalationDetails();
			
			if (escDetails == null) {
				escDetails = new ArrayList<>();
			}
			
			EscalationDetailsDTO escDto = new EscalationDetailsDTO();
			escDto.setLevel(assignedRole);
			escDto.setCategory(request.getCategory());
			escDto.setComment(request.getComment());
			escDto.setEscDTimes(LocalDateTime.now());
			escDto.setEscBy(appHistory.getVerifiedOfficerId());
			
			escDetails.add(escDto);
			application.setEscalationDetails(escDetails);
			
			application.setUpdatedBy(UserDetailUtil.getLoggedInUserId());
			application.setUpdatedTimes(LocalDateTime.now());
			
			mVSApplicationRepo.save(application);
			
			if (!CommonConstants.MVS_DISTRICT_OFFICER_ROLE.equals(roleToAssign)) {
				if (officerAssignment.getCrDTimes() == null) {
					officerAssignment.setCreatedBy(SYSTEM);
					officerAssignment.setCrDTimes(LocalDateTime.now());
				} else {
					officerAssignment.setUpdatedBy(SYSTEM);
					officerAssignment.setUpdatedTimes(LocalDateTime.now());
				}
				officerAssignmentRepo.save(officerAssignment);
			}
			
			mVSApplicationHistoryRepo.save(appHistory);
			
			logger.info("Application assigned to officer: " + selectedOfficer.getUserId());
		}
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
		
		List<EscalationDetailsDTO> escalationDetailsCopy = application.getEscalationDetails() != null
				? application.getEscalationDetails().stream()
						.map(escDetail -> new EscalationDetailsDTO(escDetail))
						.collect(Collectors.toList()) : null; 
		appHistory.setEscalationDetails(escalationDetailsCopy);
		
		appHistory.setCreatedBy(application.getCreatedBy());
		appHistory.setCrDTimes(LocalDateTime.now());
		return appHistory;
	}
	
	private void uploadToPacketManager(MVSApplication application, DocumentDTO documentDTO) {
		logger.info("Creating packet for uploading documents");
		PacketDto packetDto = new PacketDto();
    	packetDto.setId(application.getRegId());
    	packetDto.setSource(application.getSource());
    	packetDto.setProcess(documentUploadProcess);
    	packetDto.setRefId(application.getRefId());
    	packetDto.setSchemaVersion(application.getSchemaVersion());
    	packetDto.setSchemaJson(getSchemaJson(application.getSchemaVersion()));
    	
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
        
        try {
            ResponseEntity<ResponseWrapper<List<PacketInfo>>> responseEntity = restTemplate.exchange(
                createPacketUrl, 
                HttpMethod.PUT, 
                httpEntity, 
                new ParameterizedTypeReference<ResponseWrapper<List<PacketInfo>>>() {}
            );

            if (responseEntity.getBody() == null) {
	            logger.error("Failed to upload documents. Status code: " + responseEntity.getStatusCodeValue());
	            throw new RequestException(ErrorCode.PACKET_MANAGER_UPLOAD_FAILED.getErrorCode(),
						ErrorCode.PACKET_MANAGER_UPLOAD_FAILED.getErrorMessage() + "with status code: " + responseEntity.getStatusCodeValue());
	        }

	        ResponseWrapper<List<PacketInfo>> responseWrapper = responseEntity.getBody();

	        if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
	        	logger.error("Document upload error: {}", responseWrapper.getErrors().get(0));
	            throw new RequestException(ErrorCode.INVALID_PACKET_MANAGER_RESPONSE.getErrorCode(),
						ErrorCode.INVALID_PACKET_MANAGER_RESPONSE.getErrorMessage() + "with error: " + responseWrapper.getErrors().get(0));
	        }

	        logger.info("Documents uploaded successfully");
        } catch (RestClientException e) {
        	logger.error("Failed to upload packet to Packet Manager, {}", e);
        	throw new RequestException(ErrorCode.PACKET_MANAGER_UPLOAD_FAILED.getErrorCode(),
					ErrorCode.PACKET_MANAGER_UPLOAD_FAILED.getErrorMessage() + "with error: " + e.getMessage());
        }
        
	}
	
	private String getSchemaJson(String schemaVersion) {
		logger.info("Fetching schema for version: {}", schemaVersion);
		if (schemajsonValue != null && !schemajsonValue.isEmpty() && schemajsonValue.get(schemaVersion) != null)
			return schemajsonValue.get(schemaVersion);
			
		String url = schemaUrl + "?schemaVersion=" + schemaVersion;
		ResponseEntity<String> responseSchemaJson = null;
		try {
			responseSchemaJson = restTemplate.exchange(url, HttpMethod.GET, null,
				 String.class);
		} catch (Exception e) {
			throw new ApiNotAccessibleException("Could not fetch schemajson with version : " + schemaVersion);
		}

		if (responseSchemaJson == null)
			throw new ApiNotAccessibleException("Could not fetch schemajson with version : " + schemaVersion);

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
		
		logger.info("Successfully fetched schema for version: {}", schemaVersion);

		return schemajsonValue.get(schemaVersion);
	}
	
	
	private void sendSMS(String phone, String message) {
		logger.info("Sending SMS notification");
		
		try {
			SMSRequestDTO smsRequestDTO = new SMSRequestDTO();
			smsRequestDTO.setMessage(message);
			smsRequestDTO.setNumber(phone);
			RequestWrapper<SMSRequestDTO> req = new RequestWrapper<>();
			req.setRequest(smsRequestDTO);
			
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);

	        HttpEntity<RequestWrapper<SMSRequestDTO>> requestEntity = new HttpEntity<>(req, headers);

	        ResponseEntity<ResponseWrapper> responseEntity = restTemplate.exchange(
	        		smsNotificationUrl,
	                HttpMethod.POST,
	                requestEntity,
	                ResponseWrapper.class
	        );

	        if (responseEntity.getBody() == null) {
	            logger.error("Failed to send sms notification. Status code: " + responseEntity.getStatusCodeValue());
	            throw new RequestException(ErrorCode.SMS_NOTIFICATION_FAILED.getErrorCode(),
						ErrorCode.SMS_NOTIFICATION_FAILED.getErrorMessage() + "with status code: " + responseEntity.getStatusCodeValue());
	        }

	        ResponseWrapper<?> responseWrapper = responseEntity.getBody();

	        if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
	        	logger.error("SMS notification error: {}", responseWrapper.getErrors().get(0));
	            throw new RequestException(ErrorCode.FAILED_SMS_NOTIFICATION_RESPONSE.getErrorCode(),
						ErrorCode.FAILED_SMS_NOTIFICATION_RESPONSE.getErrorMessage() + "with error: " + responseWrapper.getErrors().get(0));
	        }

	        logger.info("SMS sent successfully");

	    } catch (RequestException ex) {
	        throw ex;
	    } catch (Exception ex) {
	    	logger.error("Failed to send sms notification, {}", ex);
            throw new RequestException(ErrorCode.SMS_NOTIFICATION_FAILED.getErrorCode(),
					ErrorCode.SMS_NOTIFICATION_FAILED.getErrorMessage() + "with error: " + ex.getMessage());
	    }
	}
	
	private void scheduleInterview(MVSApplication application, String district) {
		UpdateStatusRequest updateRequest = new UpdateStatusRequest();
		updateRequest.setComment("Interview required for further clarifications");

		String officerRole = application.getAssignedOfficerRole();

		if(officerRole.equals(CommonConstants.MVS_DISTRICT_OFFICER_ROLE)) {
			if (district == null) {
				ApplicationDetailsResponse appResponse = getApplicationDetails(application);
				district = getDemoValue(appResponse.getDemographics().get("applicantPlaceOfResidenceDistrict"));
			}

			escalateApplication(application, CommonConstants.MVS_DISTRICT_OFFICER_ROLE,
					StageCode.INTERVIEW_SCHEDULED.getStage(), updateRequest, district);
		}
		else {
			application.setStage(StageCode.INTERVIEW_SCHEDULED.getStage());
			application.setUpdatedBy(UserDetailUtil.getLoggedInUserId());
			application.setUpdatedTimes(LocalDateTime.now());

			mVSApplicationRepo.save(application);
		}
	}
	 
	@Scheduled(cron = "${manual.verification.cron.expression:0 0/3 * * * ?}")
	public void fetchUsers() {
		logger.info("Fetching user details for assignment");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(userDetailsUrl);
        Map<String, String> pathParams = new HashMap<>();

		officerRoles.forEach(role -> {
			pathParams.put("role-name", role);
			
	        try {
				ResponseEntity<String> response = keycloakRestTemplate.exchange(uriComponentsBuilder.buildAndExpand(pathParams).toString(),
						HttpMethod.GET, entity, String.class);
				
				if(response.getBody() != null) {
					JsonNode node = objectMapper.readTree(response.getBody());
				    List<OfficerDetailDTO> userDetails = mapUsersToUserDetailDto(node, role);
					
				    userDetails.sort((o1, o2) -> o1.getUserId().compareTo(o2.getUserId()));
				    officerDetailMap.put(role, userDetails);
				    logger.info("{} users fetched for role: {}", userDetails.size(), role);
				}
			} catch (RestClientException e) {
				logger.error("Unable to fetch user details for role: {}, error: {}", role, e);
			} catch (Exception exc) {
				logger.error("Unable to fetch user details for role: {}, error: {}", role, exc);
			}
		});

		populateMapsForDisOfficers();
	}
	
	private List<OfficerDetailDTO> mapUsersToUserDetailDto(JsonNode node, String roleName) {
		List<OfficerDetailDTO> officerDetailDTOs = new ArrayList<>();
		if (node == null) {
			logger.error("response from openid is null >>");
			return officerDetailDTOs;
		}

		for (JsonNode jsonNode : node) {
			OfficerDetailDTO officerDetailDTO = new OfficerDetailDTO();
			String username = jsonNode.get("username").textValue();
			officerDetailDTO.setUserId(username);
			officerDetailDTO.setEmail(jsonNode.hasNonNull("email") ? jsonNode.get("email").textValue() : null);
			officerDetailDTO.setUserName(String.format("%s %s",
					(jsonNode.hasNonNull("firstName") ? jsonNode.get("firstName").textValue() : ""),
					(jsonNode.hasNonNull("lastName") ? jsonNode.get("lastName").textValue() : "")));
			officerDetailDTO.setUserRole(roleName);
			
			if (jsonNode.hasNonNull("attributes")) {
				JsonNode attributeNodes = jsonNode.get("attributes");
				
				Map<String, String> attributes = new HashMap<>();
				attributeNodes.fields().forEachRemaining(entry -> {
					attributes.put(entry.getKey(), entry.getValue().get(0).textValue());
				});
				
				officerDetailDTO.setAttributes(attributes);
			}
			
			officerDetailDTOs.add(officerDetailDTO);
		}

		return officerDetailDTOs;
	}
	
	private void populateMapsForDisOfficers() {
		List<OfficerDetailDTO> userDetails = officerDetailMap.get(CommonConstants.MVS_DISTRICT_OFFICER_ROLE);

		userDetails.forEach(u -> {
			Map<String, String> attributes = u.getAttributes();
			String district = attributes.get("district");

			if (district != null) {
				districtOfficerMap.computeIfAbsent(district, k -> new ArrayList<>()).add(u);
				districtOfficerAssignment.putIfAbsent(district, u.getUserId());
			}
			else {
                logger.error("District not available for the user: {}", u.getUserId());
			}
		});
	}
	
	private int getApplicationCount(String userId) {
		return mVSApplicationRepo.countByAssignedOfficerId(userId);
	}
	
	public DemographicDetailsDTO getDemographicDetails(String registrationId) {
		logger.info("Fetching demographic data from id repo: {}");
		
		String url = idRepoUrl + registrationId;
		logger.info("Fetching demographic data from URL: {}", url);
		ResponseEntity<DemographicDetailsDTO> response = null;
		try {
			System.out.println("malay:: reponse"+restTemplate.exchange(url, HttpMethod.GET, null,
					DemographicDetailsDTO.class));
			response = restTemplate.exchange(url, HttpMethod.GET, null,
					DemographicDetailsDTO.class);
			System.out.println("malay:: reponse"+response);
		} catch (Exception e) {
			throw new ApiNotAccessibleException("Could not fetch demographic data from id repo: {} " );
		}

		return response.getBody();
		
	}
	public byte[] extractFaceImageData(byte[] decodedBioValue) {

		try (DataInputStream din = new DataInputStream(new ByteArrayInputStream(decodedBioValue))) {

			byte[] format = new byte[4];
			din.read(format, 0, 4);
			byte[] version = new byte[4];
			din.read(version, 0, 4);
			int recordLength = din.readInt();
			short numberofRepresentionRecord = din.readShort();
			byte certificationFlag = din.readByte();
			byte[] temporalSequence = new byte[2];
			din.read(temporalSequence, 0, 2);
			int representationLength = din.readInt();
			byte[] representationData = new byte[representationLength - 4];
			din.read(representationData, 0, representationData.length);
			try (DataInputStream rdin = new DataInputStream(new ByteArrayInputStream(representationData))) {
				byte[] captureDetails = new byte[14];
				rdin.read(captureDetails, 0, 14);
				byte noOfQualityBlocks = rdin.readByte();
				if (noOfQualityBlocks > 0) {
					byte[] qualityBlocks = new byte[noOfQualityBlocks * 5];
					rdin.read(qualityBlocks, 0, qualityBlocks.length);
				}
				short noOfLandmarkPoints = rdin.readShort();
				byte[] facialInformation = new byte[15];
				rdin.read(facialInformation, 0, 15);
				if (noOfLandmarkPoints > 0) {
					byte[] landmarkPoints = new byte[noOfLandmarkPoints * 8];
					rdin.read(landmarkPoints, 0, landmarkPoints.length);
				}
				byte faceType = rdin.readByte();
				byte imageDataType = rdin.readByte();
				byte[] otherImageInformation = new byte[9];
				rdin.read(otherImageInformation, 0, otherImageInformation.length);
				int lengthOfImageData = rdin.readInt();

				byte[] image = new byte[lengthOfImageData];
				rdin.read(image, 0, lengthOfImageData);

				return image;
			}
		} catch (Exception ex) {
			return null;
		}
	}

}
