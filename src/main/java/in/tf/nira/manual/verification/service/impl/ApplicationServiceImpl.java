package in.tf.nira.manual.verification.service.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;
import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import in.tf.nira.manual.verification.dto.*;
import org.apache.commons.io.IOUtils;
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
import org.springframework.core.env.Environment;
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

import in.tf.nira.manual.verification.config.ServiceProperties;
import in.tf.nira.manual.verification.constant.CommonConstants;
import in.tf.nira.manual.verification.constant.ErrorCode;
import in.tf.nira.manual.verification.constant.StageCode;
import in.tf.nira.manual.verification.dto.DemographicDetailsDTO.Document;
import in.tf.nira.manual.verification.dto.DemographicDetailsDTO.LanguageValue;
import in.tf.nira.manual.verification.dto.DemographicDetailsDTO.ProofDocument;
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
import in.tf.nira.manual.verification.util.CbeffToBiometricUtil;
import in.tf.nira.manual.verification.util.CountryRegionMapping;
import in.tf.nira.manual.verification.util.CryptoCoreUtil;
import in.tf.nira.manual.verification.util.PageUtils;
import in.tf.nira.manual.verification.util.TemplateGenerator;
import in.tf.nira.manual.verification.util.UserDetailUtil;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.JsonUtils;
import java.io.File;

@Service
public class ApplicationServiceImpl implements ApplicationService {
	private static final Logger logger = LoggerFactory.getLogger(ApplicationServiceImpl.class);
	/** The Constant APPLICANT_PHOTO. */
	private static final String APPLICANT_PHOTO = "ApplicantPhoto";
	/** The Constant FACE. */
	private static final String FACE = "Face";
	
	private static final String PACKET_MANAGER_ID = "mosip.commmons.packetmanager";
    private static final String PACKET_MANAGER_VERSION = "v1";
    private static final String RESPONSE = "response";
    private static final String SCHEMA_JSON = "schemaJson";
    private static final String SYSTEM = "System";
    private static final String ENCODING = "UTF-8";
    
	@Value("${manual.verification.user.details.url}")
    private String userDetailsUrl;
	
	@Value("${manual.verification.create.packet.url}")
    private String createPacketUrl;
	
	@Value("#{'${manual.verification.officer.roles}'.split(',')}")
    private List<String> officerRoles;

	@Value("#{'${manual.verification.officerAssignment.serviceTypesForLegalOfficer}'.split(',')}")
	private List<String> serviceTypesForLegalOfficer;

	@Value("${manual.verification.officerAssignment.updateDOBServiceType}")
	private String updateDOBServiceType;

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
	
	@Value("${manual.verification.default.source:REGISTRATION_CLIENT}")
	private String defaultSource;
	
	@Value("${manual.verification.email.template.code}")
	private String emailTemplateTypeCode;
	
	@Value("${manual.verification.sms.template.code}")
	private String smsTemplateTypeCode;
	
	@Value("${manual.verification.interview.valid.days}")
	private int interviewValidDays;
	
	@Value("${packetmanager.document.fetch.url}")
	private String packetManagerDocumentFetchUrl;

	@Value("${manual.verification.reassignment.days}")
	private int reassignmentDays;

	@Value("${manual-verification.prev.officer.email.template.code}")
	private String prevOfficerEmailTemplateTypeCode;

	@Value("${manual-verification.new.officer.email.template.code}")
	private String newOfficerEmailTemplateTypeCode;
	
	private Map<String, List<OfficerDetailDTO>> officerDetailMap = new HashMap<>();
	
	private Map<String, String> schemajsonValue = null;

	private Map<String, List<OfficerDetailDTO>> districtOfficerMap = new HashMap<>();

	private Map<String, String> districtOfficerAssignment = new HashMap<>();
	
	private Map<String, List<OfficerDetailDTO>> internationalOfficerMap = new HashMap<>();

	private Map<String, String> internationalOfficerAssignment = new HashMap<>();
	
	private Map<String, List<OfficerDetailDTO>> seniorRegistrationOfficerMap = new HashMap<>();

	private Map<String, String> seniorRegistrationOfficerAssignment = new HashMap<>();

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
	
	@Autowired
	private TemplateGenerator templateGenerator;
	
	@Autowired
	private Environment env;
	
	@Autowired
	private CountryRegionMapping countryRegionMapping;
	
	@Autowired
	private ServiceProperties serviceProperties;
	
	@PostConstruct
    public void runAtStartup() {
        fetchUsers();
    }
	
	@Override
	public StatusResponseDTO createApplication(CreateAppRequestDTO verifyRequest) {
		logger.info("Application received for manual verification in mvs with reg id: " + verifyRequest.getRegId());

		String officerRole = "";

		if (verifyRequest.getService().equals(CommonConstants.UPDATE)) {
			officerRole = getOfficerRoleBasedOnUpdateService(verifyRequest);
		} else {
			officerRole = CommonConstants.MVS_OFFICER_ROLE;
		}

		OfficerAssignment officerAssignment = officerAssignmentRepo.findByUserRole(officerRole);
		if(officerAssignment == null) {
			officerAssignment = new OfficerAssignment();
		}
		OfficerDetailDTO selectedOfficer = fetchOfficerForAssignment(officerRole, officerAssignment, null, null);
		
		if(selectedOfficer != null) {
			logger.info("Assigning application to officer: " + selectedOfficer.getUserId());
			MVSApplication mVSApplication = new MVSApplication();
			mVSApplication.setRegId(verifyRequest.getRegId());
			//mVSApplication.setService(env.getProperty(verifyRequest.getService().replaceAll(" ", "_")));
			mVSApplication.setService(serviceProperties.toDisplay(verifyRequest.getService()));
			mVSApplication.setServiceType(env.getProperty(verifyRequest.getServiceType().replaceAll(" ", "_")));
			mVSApplication.setReferenceURL(verifyRequest.getReferenceURL());
			mVSApplication.setSource(verifyRequest.getSource() != null ? verifyRequest.getSource() : defaultSource);
			mVSApplication.setRefId(verifyRequest.getRefId());
			mVSApplication.setSchemaVersion(verifyRequest.getSchemaVersion());
			mVSApplication.setFoundLink(verifyRequest.getFoundLink());
			mVSApplication.setAgeGroup(verifyRequest.getAgeGroup());;
			mVSApplication.setResDistrict(verifyRequest.getApplicantPlaceOfResidenceDistrict());
			mVSApplication.setAssignedOfficerId(selectedOfficer.getUserId());
			mVSApplication.setAssignedOfficerName(selectedOfficer.getUserName());
			mVSApplication.setAssignedOfficerRole(selectedOfficer.getUserRole());
			mVSApplication.setStage(StageCode.ASSIGNED_TO_OFFICER.getStage());
			mVSApplication.setCreatedBy(SYSTEM);
			mVSApplication.setCrDTimes(LocalDateTime.now());
			mVSApplication.setStatusComment(verifyRequest.getStatusComment());
			
			//set assignedDate
			mVSApplication.setAssignedDate(LocalDateTime.now());
			
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
	public String getOfficerRoleBasedOnUpdateService(CreateAppRequestDTO verifyRequest) {
		try {
			// get demographics from dataShareURL
			ResponseEntity<String> responseEn = restTemplate.exchange(verifyRequest.getReferenceURL(), HttpMethod.GET, null, String.class);
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
			Map<String, String> demographics = dataShareResponse.getIdentity();

			// if change of date of birth -> fetch prev DOB
			if (demographics.get(updateDOBServiceType) != null  && demographics.get(updateDOBServiceType).equals("Y")) {
				DemographicDetailsDTO previousDemographics = getDemographicDetails(demographics.get("NIN"));
				String currentDOB = demographics.get("dateOfBirth");
				String previousDOB = previousDemographics.getIdentity().getDateOfBirth();

				if (!currentDOB.isEmpty() && !previousDOB.isEmpty()) {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

					LocalDate currentDate = LocalDate.parse(currentDOB, formatter);
					LocalDate previousDate = LocalDate.parse(previousDOB, formatter);

					long daysDifference = Math.abs(ChronoUnit.DAYS.between(previousDate, currentDate));

					if (daysDifference > (4 * 365)) return CommonConstants.MVS_LEGAL_OFFICER_ROLE;
				}
			}

			// check for serviceTypesForLegalOfficer
			for (String serviceType: serviceTypesForLegalOfficer) {
				if (demographics.get(serviceType) != null && demographics.get(serviceType).equals("Y")) {
					return CommonConstants.MVS_LEGAL_OFFICER_ROLE;
				}
			}

			return CommonConstants.MVS_OFFICER_ROLE;

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


	@Override
	public List<UserApplicationsResponse> getApplicationsForUser(String userId, Boolean rejectFlag) {
		logger.info("Fetching applications for user: " + userId);

		List<MVSApplication> applications;

		if(rejectFlag) {
			applications = mVSApplicationRepo.getAllRejectedApplications();
		} else {
			applications = mVSApplicationRepo.findByAssignedOfficerId(userId);
		}
		
		if (applications == null || applications.isEmpty()) {
			logger.error("No applications available for the user: " + userId);
			throw new RequestException(ErrorCode.NO_APPS_FOR_USER.getErrorCode(), 
					ErrorCode.NO_APPS_FOR_USER.getErrorMessage());
	    }
		
		return buildUserApplicationsResponse(applications);
	}

	@Override
	public RejectedApplicationResponse getRejectedApplication(String applicationId) {
		logger.info("Fetching rejected application for ID: {}", applicationId);

		MVSApplication application = mVSApplicationRepo.getRejectedApplicationById(applicationId);

		if (application == null) {
			logger.error("No application available for the application id: {}", applicationId);
			throw new RequestException(ErrorCode.INVALID_APP_ID.getErrorCode(),
					ErrorCode.INVALID_APP_ID.getErrorMessage());
		}

		RejectedApplicationResponse response = new RejectedApplicationResponse();
		response.setApplicationId(application.getRegId());
		response.setService(application.getService());
		response.setServiceType(application.getServiceType());
		response.setStatus(application.getStage());
		response.setCrDTimes(application.getCrDTimes());
		response.setRejectionCategory(application.getRejectionCategory());
		response.setRejectionComment(application.getComments());
		response.setLastAssignedOfficerRole(application.getAssignedOfficerRole());
		response.setLastAssignedOfficerId(application.getAssignedOfficerId());
		response.setLastUpdatedTimes(application.getUpdatedTimes());
		response.setStatusComment(application.getStatusComment());
		response.setFoundLink(application.getFoundLink());
		response.setAgeGroup(application.getAgeGroup());

		if (application.getEscalationDetails() != null) {
			application.getEscalationDetails().forEach(esc -> {
				if (CommonConstants.MVS_OFFICER_ROLE.equals(esc.getLevel())) {
					response.setOfficerEscDetails(esc);
				} else if (CommonConstants.MVS_SUPERVISOR_ROLE.equals(esc.getLevel())) {
					response.setSupervisorEscDetails(esc);
				} else if (CommonConstants.MVS_LEGAL_OFFICER_ROLE.equals(esc.getLevel())) {
					response.setLegalEscDetails(esc);
				}
			});
		}

		return response;
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
		
	    return getApplicationDetails(application, true, true);
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
							StageCode.ASSIGNED_TO_LEGAL_OFFICER.getStage(), request, null, null);
				}
				else if(request.getSelectedOfficerLevel() != null && request.getSelectedOfficerLevel().equals(CommonConstants.MVS_DISTRICT_OFFICER_ROLE) ||
						(request.getInsufficientDocuments() != null && request.getInsufficientDocuments())) {
					ApplicationDetailsResponse appResponse = getApplicationDetails(application, false, false);
					String district = getDemoValue(appResponse.getDemographics().get("applicantPlaceOfResidenceDistrict"));
					String nin = appResponse.getDemographics().get("NIN");

					if(district == null && nin != null) {
						DemographicDetailsDTO demographicDetailsDTO = getDemographicDetails(nin);
						district = demographicDetailsDTO.getIdentity().getApplicantPlaceOfResidenceDistrict().get(0).getValue();
					}

					if(district == null && application.getResDistrict() != null) {
						district = application.getResDistrict();
					}

					logger.info("Application ID {} escalating to {} district", applicationId, district);

					escalateApplication(application, CommonConstants.MVS_DISTRICT_OFFICER_ROLE,
							StageCode.ASSIGNED_TO_DISTRICT_OFFICER.getStage(), request, district, null);
				}
				else if(request.getSelectedOfficerLevel() != null && request.getSelectedOfficerLevel().equals(CommonConstants.MVS_SUPERVISOR_ROLE)) {
					escalateApplication(application, CommonConstants.MVS_SUPERVISOR_ROLE,
							StageCode.ASSIGNED_TO_SUPERVISOR.getStage(), request, null, null);
				}
				else if(request.getSelectedOfficerLevel() != null && request.getSelectedOfficerLevel().equals(CommonConstants.MVS_EXECUTIVE_DIRECTOR)) {
					escalateApplication(application, CommonConstants.MVS_EXECUTIVE_DIRECTOR,
							StageCode.ASSIGNED_TO_EXECUTIVE_DIRECTOR.getStage(), request, null, null);
				}
				else if(request.getSelectedOfficerLevel() != null && request.getSelectedOfficerLevel().equals(CommonConstants.MVS_INTERNATIONAL_OFFICER)) {
					ApplicationDetailsResponse appResponse = getApplicationDetails(application, false, false);
					String foreignCountry = getDemoValue(appResponse.getDemographics().get("applicantForeignResidenceCountry"));
					// Determine the region for this country
				    String region = countryRegionMapping.getRegionForCountry(foreignCountry);
				    
				    logger.info("Application ID {} escalating to international officer for country: {} (region: {})", 
				            applicationId, foreignCountry, region);
					escalateApplication(application, CommonConstants.MVS_INTERNATIONAL_OFFICER,
							StageCode.ASSIGNED_TO_MVS_INTERNATIONAL_OFFICER.getStage(), request, null, region);
				}
				else {
					logger.error("Application already escalated to highest level");
					throw new RequestException(ErrorCode.ESCALATION_NOT_ALLOWED.getErrorCode(), 
							ErrorCode.ESCALATION_NOT_ALLOWED.getErrorMessage());
				}
				break;
			case CommonConstants.RECOMMEND_FOR_APPROVAL_STATUS:
				ApplicationDetailsResponse appResponse = getApplicationDetails(application, false, false);
				String district = getDemoValue(appResponse.getDemographics().get("applicantPlaceOfResidenceDistrict"));
				String nin = appResponse.getDemographics().get("NIN");

				if(district == null && nin != null) {
					DemographicDetailsDTO demographicDetailsDTO = getDemographicDetails(nin);
					district = demographicDetailsDTO.getIdentity().getApplicantPlaceOfResidenceDistrict().get(0).getValue();
				}

				if(district == null && application.getResDistrict() != null) {
					district = application.getResDistrict();
				}
				
				if(appResponse.getDemographics() != null &&
				   appResponse.getDemographics().get("residenceStatus") != null &&
				   getDemoValue(appResponse.getDemographics().get("residenceStatus")) != null &&
				   (getDemoValue(appResponse.getDemographics().get("residenceStatus")).equalsIgnoreCase(CommonConstants.OUTSIDE_UGANDA))) {
					district = CommonConstants.INTERNATIONAL_ADDRESS;
				}
				
				logger.info("Application ID {} escalating to {} sro", applicationId, district);

				escalateApplication(application, CommonConstants.MVS_SENIOR_REGISTRATION_OFFICER,
						StageCode.ASSIGNED_TO_MVS_SENIOR_REGISTRATION_OFFICER.getStage(), request, district, null);
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
				application.getAssignedOfficerRole().equals(CommonConstants.MVS_EXECUTIVE_DIRECTOR) ||
				application.getAssignedOfficerRole().equals(CommonConstants.MVS_INTERNATIONAL_OFFICER)) {
			ApplicationDetailsResponse appResponse = getApplicationDetails(application, false, false);
			sendNotification(application, schInterviewDTO, appResponse);
			scheduleInterview(application, schInterviewDTO.getDistrict(), appResponse);
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
		
		if(application.getAssignedOfficerRole().equals(CommonConstants.MVS_DISTRICT_OFFICER_ROLE)
				|| application.getAssignedOfficerRole().equals(CommonConstants.MVS_INTERNATIONAL_OFFICER)
				|| application.getAssignedOfficerRole().equals(CommonConstants.MVS_LEGAL_OFFICER_ROLE)) {
			uploadToPacketManager(application, documentDTO);
			// approveApplication(application, "Documents uploaded");
			 if (documentDTO.getDocuments() != null && !documentDTO.getDocuments().isEmpty()) {
		            // Get all document keys
		            List<String> documentKeys = new ArrayList<>(documentDTO.getDocuments().keySet());
		            
		            // Update the entity with document keys
		            application.setUploadDocList(documentKeys);
		            
		            // Save the updated application
		            application.setUpdatedBy(UserDetailUtil.getLoggedInUserId());
		            application.setUpdatedTimes(LocalDateTime.now());
		            mVSApplicationRepo.save(application);
		            
		            logger.info("Updated uploadDocList with {} document keys", documentKeys.size());
		        }
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
	
	private OfficerDetailDTO fetchOfficerForAssignment(String role, OfficerAssignment officerAssignment, String district, String region) {
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
				List<OfficerDetailDTO> disOfficers = districtOfficerMap.get(district);
				
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
		
		if (CommonConstants.MVS_SENIOR_REGISTRATION_OFFICER.equals(role)) {
			//fetch officer by district
			if (district != null) {
				List<OfficerDetailDTO> seniorRegistrationOfficers = seniorRegistrationOfficerMap.get(district);
				
				if (seniorRegistrationOfficers != null && !seniorRegistrationOfficers.isEmpty()) {

					String nextOfficerId = seniorRegistrationOfficerAssignment.get(district);

					OfficerDetailDTO assignedOfficer = seniorRegistrationOfficers.stream()
							.filter(o -> o.getUserId().equals(nextOfficerId))
							.findFirst()
							.orElse(seniorRegistrationOfficers.get(0));

					int currentIndex = seniorRegistrationOfficers.indexOf(assignedOfficer);
					int newNextOfficerIndex = (currentIndex + 1) % seniorRegistrationOfficers.size();
					seniorRegistrationOfficerAssignment.put(district, seniorRegistrationOfficers.get(newNextOfficerIndex).getUserId());

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
		
		if(CommonConstants.MVS_INTERNATIONAL_OFFICER.equals(role)) {
			//fetch officer by region
			if (region != null) {
				List<OfficerDetailDTO> internationalOfficers = internationalOfficerMap.get(region);
				
				if (internationalOfficers != null && !internationalOfficers.isEmpty()) {

					String nextOfficerId = internationalOfficerAssignment.get(region);

					OfficerDetailDTO assignedOfficer = internationalOfficers.stream()
							.filter(o -> o.getUserId().equals(nextOfficerId))
							.findFirst()
							.orElse(internationalOfficers.get(0));

					int currentIndex = internationalOfficers.indexOf(assignedOfficer);
					int newNextOfficerIndex = (currentIndex + 1) % internationalOfficers.size();
					internationalOfficerAssignment.put(region, internationalOfficers.get(newNextOfficerIndex).getUserId());

					return assignedOfficer;

				} else {
					throw new RequestException(ErrorCode.NO_OFFICER_FOR_REGION.getErrorCode(),
							String.format(ErrorCode.NO_OFFICER_FOR_REGION.getErrorMessage(), region));
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
	    userApp.setStatusComment(app.getStatusComment());
	    userApp.setFoundLink(app.getFoundLink());
	    userApp.setAgeGroup(app.getAgeGroup());
	    
	    if (app.getEscalationDetails() != null) {
	        app.getEscalationDetails().forEach(esc -> {
	            if (CommonConstants.MVS_OFFICER_ROLE.equals(esc.getLevel())) {
	                userApp.setOfficerEscDetails(esc);
	            } else if (CommonConstants.MVS_SUPERVISOR_ROLE.equals(esc.getLevel())) {
	                userApp.setSupervisorEscDetails(esc);
	            } else if (CommonConstants.MVS_LEGAL_OFFICER_ROLE.equals(esc.getLevel())) {
					userApp.setLegalEscDetails(esc);
				} else if (CommonConstants.MVS_DISTRICT_OFFICER_ROLE.equals(esc.getLevel())) {
					//setting the category as the application is Recommended for approval from
					esc.setCategory("Additional documents Uploaded");
					userApp.setDistrictEscDetails(esc);
				}
	        });
	    }
	    
	    return userApp;
	}
	
	private MVSApplication getApplicationById(String applicationId) {
	    try {
	        return mVSApplicationRepo.findById(applicationId)
	            .orElseThrow(() -> {
	                logger.error("Invalid application ID: {}", applicationId);
	                return new RequestException(ErrorCode.INVALID_APP_ID.getErrorCode(),
	                        ErrorCode.INVALID_APP_ID.getErrorMessage());
	            });
	    } catch (RequestException e) {
	    	System.out.println("inside getting data for rejected ");
	            MVSApplication app = mVSApplicationRepo.getRejectedApplicationById(applicationId);
	            if (app != null) {
	                return app;
	            }
	        
	        throw e;
	    }
	}
	
	private ApplicationDetailsResponse getApplicationDetails(MVSApplication application, boolean includeBiometrics, boolean includeDocuments) {
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
	        
	        response = new String(response.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
	        DataShareResponseDto dataShareResponse = objectMapper.readValue(response, DataShareResponseDto.class);
	        
	        Map<String, String> demographicsMap = new HashMap<>(dataShareResponse.getIdentity());
	        
	        String lostCardService = env.getProperty("LOST");
	        String COPService = env.getProperty("UPDATE");
	        String renewalService = env.getProperty("RENEWAL");
	        logger.info("Retrieved LOST card service value from properties: {}", lostCardService);
	        logger.info("Retrieved COP card service value from properties: {}", COPService);
	        logger.info("Retrived RENEWAL service from properties: {}", renewalService);
	        logger.info("Current application service: {}", application.getService());
	        String applicationService = application.getService();
	        if(applicationService != null && !applicationService.trim().isEmpty() && 
	        		(applicationService.equalsIgnoreCase(lostCardService) || 
	        		applicationService.equalsIgnoreCase(COPService)) || 
	        		applicationService.equalsIgnoreCase(renewalService)) {
	        	logger.info("Processing Lost/Replacement or COP or Renewal of card application with ID: {}", application.getRegId());
	        	
	        	String nin = demographicsMap.get(CommonConstants.NIN);
	        	logger.info("Retrieved NIN from demographics: {}", nin);
	        	
	        	if(nin != null) {
	        		try {
	        			logger.info("Fetching previous demographic details from ID repository for NIN");
	        			DemographicDetailsDTO previousDemographics = getDemographicDetails(nin);
	        			
	        			List<LanguageValue> surname = previousDemographics.getIdentity().getSurname();
	        			logger.info("Retrieved surname from ID repository: {}",surname);
	        			
	    	        	List<LanguageValue> givenName = previousDemographics.getIdentity().getGivenName();
	    	        	logger.info("Retrieved givenname from ID repository: {}",givenName);
	    	        	
	    	        	if(surname != null && !surname.isEmpty()) {
	    	        		String surnameJson = objectMapper.writeValueAsString(surname);
	    	        		if(applicationService.equalsIgnoreCase(lostCardService) || 
	    	        				applicationService.equalsIgnoreCase(renewalService)) {
		    	        		demographicsMap.put(CommonConstants.SURNAME, surnameJson);
	    	        		} else if(applicationService.equalsIgnoreCase(COPService)) {
	    	        			demographicsMap.put(CommonConstants.COP_SURNAME_PREVIOUS, surnameJson);
	    	        		}
	    	        	} else {
	    	        		logger.info("Surname is null thus not adding to demographics map");
	    	        	}
	    	        	
	    	        	if(givenName != null && !givenName.isEmpty()) {
	    	        		String givenNameJson = objectMapper.writeValueAsString(givenName);
	    	        		if(applicationService.equalsIgnoreCase(lostCardService) || 
	    	        				applicationService.equalsIgnoreCase(renewalService)) {
	    	        			demographicsMap.put(CommonConstants.GIVEN_NAME, givenNameJson);
	    	        		} else if(applicationService.equalsIgnoreCase(COPService)) {
	    	        			demographicsMap.put(CommonConstants.COP_GIVEN_NAME_PREVIOUS, givenNameJson);
	    	        		}
	    	        	} else {
	    	        		logger.info("Given name is null thus not adding to demographics map");
	    	        	}
	    	        	
	    	        	logger.info("Successfully added surname and given name to demographics for Lost/Replacement or COP card application: {}",
	    	        			application.getRegId());
	    	        	
	    	        	if(applicationService.equalsIgnoreCase(COPService)) {
	    	        		String email = previousDemographics.getIdentity().getEmail();
		        			logger.info("Retrieved email from ID repository: {}",email);
		        			
		    	        	String dateOfBirth = previousDemographics.getIdentity().getDateOfBirth();
		    	        	logger.info("Retrieved dateOfBirth from ID repository: {}",dateOfBirth);
		    	        	
		    	        	String ninPrevious = previousDemographics.getIdentity().getNin();
		    	        	logger.info("Retrieved ninPrevious from ID repository: {}",ninPrevious);
		    	        	
		    	        	String phone = previousDemographics.getIdentity().getPhone();
		    	        	logger.info("Retrieved phone from ID repository: {}",phone); 
		    	        	
		    	        	String homePhoneNumber = previousDemographics.getIdentity().getHomePhoneNumber();
		    	        	logger.info("Retrieved home phone from ID repository: {}",homePhoneNumber); 
		    	        	
		    	        	List<LanguageValue> countryCode = previousDemographics.getIdentity().getCountryCode();
		    	        	logger.info("Retrieved home phone from ID repository: {}",countryCode); 
		    	        		
		    	        	if(email != null && !email.isEmpty()) {
		    	        		demographicsMap.put(CommonConstants.COP_EMAIL_PREVIOUS, email);
		    	        	} else {
		    	        		logger.info("Email is null thus not adding to demographics map");
		    	        	}
		    	        	
		    	        	if(dateOfBirth != null && !dateOfBirth.isEmpty()) {
		    	        		demographicsMap.put(CommonConstants.COP_DATE_OF_BIRTH_PREVIOUS, dateOfBirth);
		    	        	} else {
		    	        		logger.info("Date Of Birth is null thus not adding to demographics map");
		    	        	}
		    	        	
		    	        	if(ninPrevious != null && !ninPrevious.isEmpty()) {
		    	        		demographicsMap.put(CommonConstants.COP_NIN_PREVIOUS, ninPrevious);
		    	        	} else {
		    	        		logger.info("NIN is null thus not adding to demographics map");
		    	        	}
		    	        	
		    	        	if(phone != null && !phone.isEmpty()) {
		    	        		demographicsMap.put(CommonConstants.COP_PHONE_PREVIOUS, phone);
		    	        	} else {
		    	        		logger.info("Phone is null thus not adding to demographics map");
		    	        	}
		    	        	
		    	        	if(homePhoneNumber != null && !homePhoneNumber.isEmpty()) {
		    	        		demographicsMap.put(CommonConstants.COP_HOME_PHONE_NUMBER_PREVIOUS, homePhoneNumber);
		    	        	} else {
		    	        		logger.info("Home phone number is null thus not adding to demographics map");
		    	        	}
		    	        	
		    	        	if(countryCode != null && !countryCode.isEmpty()) {
		    	        		String countryCodeJson = objectMapper.writeValueAsString(countryCode);
		    	        		demographicsMap.put(CommonConstants.COP_COUNTRY_CODE_PREVIOUS, countryCodeJson);
		    	        	} else {
		    	        		logger.info("Country Code is null thus not adding to demographics map");
		    	        	}
		    	        	
	    	        	}
	    	        	
	        		}catch (Exception e) {
	        			logger.error("Error fetching surname and given name from idrepo: {}", e.getMessage());
	        		}
	        	} else {
	        		logger.info("NIN is null for Lost/Replacement or COP card application {}, cannot fetch previous demographics",
	        				application.getRegId());
	        	}
	        }
	        
	        ApplicationDetailsResponse applicationDetailsResponse = new ApplicationDetailsResponse();
	        
			if (dataShareResponse.getBiometrics() != null && includeBiometrics) {
				Map<String, Object> attributes = new HashMap<String, Object>();
				CbeffToBiometricUtil util = new CbeffToBiometricUtil();
				List<String> subtype = new ArrayList<>();
				byte[] photoByte = util.getImageBytes(dataShareResponse.getBiometrics(), FACE, subtype);

				if (photoByte != null) {
					byte[] pngBytes = convertJP2ToPNG(extractFaceImageData(photoByte));
					String data = java.util.Base64.getEncoder().encodeToString(pngBytes);
					attributes.put(APPLICANT_PHOTO, "data:image/png;base64," + data);
					applicationDetailsResponse.setBiometricAttributes(attributes);
				}

				Map<String, List<String>> bioInfo = util.getBiometricsInfo(dataShareResponse.getBiometrics());
				applicationDetailsResponse.setBiometricInfo(bioInfo);
			}
			
			if (dataShareResponse.getDocuments() != null && includeDocuments) {
				Map<String, Object> documents = new HashMap<>();
				dataShareResponse.getDocuments().forEach((key, value) -> {
					documents.put(key, CryptoUtil.decodeURLSafeBase64(value));
				});
				
				applicationDetailsResponse.setDocuments(documents);
			}
			
		    applicationDetailsResponse.setApplicationId(application.getRegId());
		    applicationDetailsResponse.setService(application.getService());
		    applicationDetailsResponse.setServiceType(application.getServiceType());
		    applicationDetailsResponse.setStatusComment(application.getStatusComment());
		    applicationDetailsResponse.setFoundLink(application.getFoundLink());
		    applicationDetailsResponse.setAgeGroup(application.getAgeGroup());
		    applicationDetailsResponse.setDemographics(demographicsMap);
		    applicationDetailsResponse.setUploadDocList(application.getUploadDocList());
		    
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
			MVSResponseDto response = new MVSResponseDto();
			response.setRegId(application.getRegId());
			response.setStatus(StageCode.APPROVED.getStage());
			response.setComment(comment);
			response.setService(serviceProperties.toCode(application.getService()));
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
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		//send back to mvs stage
		logger.info("Notifying mvs stage for rejection");
		try {
			MVSResponseDto response = new MVSResponseDto();
			response.setRegId(application.getRegId());
			response.setStatus(StageCode.REJECTED.getStage());
			response.setComment(comment);
			response.setCategory(rejectionCategory);
			response.setService(serviceProperties.toCode(application.getService()));
			response.setActionDate(LocalDate.now().format(formatter));
			ResponseEntity<Object> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
			listener.sendToQueue(responseEntity, 1);
		} catch (JsonProcessingException | UnsupportedEncodingException e) {
			logger.error("Unable to send response to mvs stage, {}", e);
		}
	}
	
	private void sendNotification(MVSApplication application, SchInterviewDTO schInterviewDTO, ApplicationDetailsResponse appResponse) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		String email = appResponse.getDemographics().get("email");
        String phone = appResponse.getDemographics().get("phone");
        String district = schInterviewDTO.getDistrict() == null ? 
        		getDemoValue(appResponse.getDemographics().get("applicantPlaceOfResidenceDistrict")) : schInterviewDTO.getDistrict();
        
        Map<String, Object> attributes = new HashMap<>();
		attributes.put("APPLICATION_ID", application.getRegId());
		attributes.put("MVS_CR_DATE", application.getCrDTimes().toLocalDate().format(formatter));
		attributes.put("DISTRICT", district);
		attributes.put("INTERVIEW_EXPIRY_DATE", LocalDate.now().plusDays(interviewValidDays).format(formatter));
		attributes.put("REVIEW_CONTENT", schInterviewDTO.getContent());
        
		if (email != null) {
			try {
				sendEmail(email, schInterviewDTO.getSubject(), attributes, emailTemplateTypeCode);
			} catch (Exception ex) {
				logger.error("Failed to send email notification but continuing with interview scheduling: {}", ex.getMessage());			
			}
		} else {
			logger.warn("Email Id not available for the application");
		}
		
		if (phone != null) {
			try {
				sendSMS(phone, attributes);
			} catch (Exception ex) {
				logger.error("Failed to send sms notification but continuing with interview scheduling: {}", ex.getMessage());
			}
		} else {
			logger.warn("Phone number not available for the application");
		}
	}
	
	private void sendEmail(String mailTo, String subject, Map<String, Object> attributes, String emailTemplateTypeCode) {
		logger.info("Sending email notification");
		try {
			InputStream stream = templateGenerator.getTemplate(emailTemplateTypeCode, attributes, "eng");
			String artifact = IOUtils.toString(stream, ENCODING);

			//InputStream subStream = templateGenerator.getTemplate(subjectCode, attributes, "eng");
			//String subjectArtifact = IOUtils.toString(subStream, ENCODING);
			
	        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(emailNotificationUrl)
	                .queryParam("mailTo", mailTo)
	                .queryParam("mailSubject", subject)
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
			UpdateStatusRequest request, String district, String region) {
		logger.info("Escalating application to next level");
		
		OfficerAssignment officerAssignment = null;
		if (!CommonConstants.MVS_DISTRICT_OFFICER_ROLE.equals(roleToAssign) && !CommonConstants.MVS_SENIOR_REGISTRATION_OFFICER.equals(roleToAssign)
				&& !CommonConstants.MVS_INTERNATIONAL_OFFICER.equals(roleToAssign)) {
			officerAssignment = officerAssignmentRepo.findByUserRole(roleToAssign);
		}
		
		if (officerAssignment == null) {
			officerAssignment = new OfficerAssignment();
		}
		
		OfficerDetailDTO selectedOfficer = fetchOfficerForAssignment(roleToAssign, officerAssignment, district, region);
		
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
			
			//set assignedDate
			application.setAssignedDate(LocalDateTime.now());
			
			mVSApplicationRepo.save(application);
			
			if (!CommonConstants.MVS_DISTRICT_OFFICER_ROLE.equals(roleToAssign) && !CommonConstants.MVS_SENIOR_REGISTRATION_OFFICER.equals(roleToAssign)
					&& !CommonConstants.MVS_INTERNATIONAL_OFFICER.equals(roleToAssign)) {
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
		
		//setting the assign date for escalation
		
		if (escalationDetailsCopy != null && !escalationDetailsCopy.isEmpty()) {
		    EscalationDetailsDTO latestEscalation = escalationDetailsCopy.get(escalationDetailsCopy.size() - 1);
		    appHistory.setAssignedDate(latestEscalation.getEscDTimes());
		}else {
		    // If no escalation details -- mvs_officer
		    appHistory.setAssignedDate(application.getCrDTimes());
		}
		
		appHistory.setCreatedBy(application.getCreatedBy());
		appHistory.setCrDTimes(LocalDateTime.now());
		appHistory.setStatusComment(application.getStatusComment());
		appHistory.setReferenceURL(application.getReferenceURL());
		appHistory.setIsDeleted(application.getIsDeleted());
		appHistory.setSource(application.getSource());
		appHistory.setRefId(application.getRefId());
		appHistory.setSchemaVersion(application.getSchemaVersion());
		appHistory.setFoundLink(application.getFoundLink());
		appHistory.setAgeGroup(application.getAgeGroup());
		appHistory.setResDistrict(application.getResDistrict());
		appHistory.setUploadDocList(application.getUploadDocList());
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
	
	
	private void sendSMS(String phone, Map<String, Object> attributes) {
		logger.info("Sending SMS notification");
		
		try {
			InputStream stream = templateGenerator.getTemplate(smsTemplateTypeCode, attributes, "eng");
			String artifact = IOUtils.toString(stream, ENCODING);
			
			SMSRequestDTO smsRequestDTO = new SMSRequestDTO();
			smsRequestDTO.setMessage(artifact);
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
	
	private void scheduleInterview(MVSApplication application, String district, ApplicationDetailsResponse appResponse) {
		UpdateStatusRequest updateRequest = new UpdateStatusRequest();
		updateRequest.setComment("Interview required for further clarifications");

		String officerRole = application.getAssignedOfficerRole();

		if(officerRole.equals(CommonConstants.MVS_DISTRICT_OFFICER_ROLE)) {
			if (district == null) {
				district = getDemoValue(appResponse.getDemographics().get("applicantPlaceOfResidenceDistrict"));
				String nin = appResponse.getDemographics().get("NIN");

				if(district == null && nin != null) {
					DemographicDetailsDTO demographicDetailsDTO = getDemographicDetails(nin);
					district = demographicDetailsDTO.getIdentity().getApplicantPlaceOfResidenceDistrict().get(0).getValue();
				}

				if(district == null && application.getResDistrict() != null) {
					district = application.getResDistrict();
				}
			}

			escalateApplication(application, CommonConstants.MVS_DISTRICT_OFFICER_ROLE,
					StageCode.INTERVIEW_SCHEDULED.getStage(), updateRequest, district, null);
		}
		else {
			application.setStage(StageCode.INTERVIEW_SCHEDULED.getStage());
			application.setUpdatedBy(UserDetailUtil.getLoggedInUserId());
			application.setUpdatedTimes(LocalDateTime.now());

			mVSApplicationRepo.save(application);
		}
	}
	 
	@Scheduled(cron = "${manual.verification.cron.expression:0 0 0/8 * * ?}")
	public void fetchUsers() {
		logger.info("Fetching user details for assignment");
        
		officerRoles.forEach(role -> {
	        try {
	        	List<OfficerDetailDTO> allUserDetails = fetchAllUsers(role);
	        	allUserDetails.sort((o1, o2) -> o1.getUserId().compareTo(o2.getUserId()));
				officerDetailMap.put(role, allUserDetails);
				
				logger.info("{} users fetched for role: {}", allUserDetails.size(), role);
				
				} catch (Exception exc) {
				logger.error("Unable to fetch user details for role: {}, error: {}", role, exc);
			}
		});

		populateMapsForDisOfficers();
		populateMapsForInternationalOfficers();
		populateMapsForSeniorRegistrationOfficers();
		//writeOfficerDetailMapToFile();
	}
	
	private List<OfficerDetailDTO> fetchAllUsers(String role) throws Exception {
		List<OfficerDetailDTO> allUsers = new ArrayList<>();
		
		int first = 0;
		int max = 100;
		boolean hasMore = true;
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		HttpEntity<String> entity =new HttpEntity<>(headers);
		
		while (hasMore) {
			UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(userDetailsUrl)
					.queryParam("first", first)
					.queryParam("max", max);
			
			Map<String, String> pathParams = new HashMap<>();
			pathParams.put("role-name", role);
			
			String finalUrl = uriComponentsBuilder.buildAndExpand(pathParams).toString();
			
			logger.info("Fetching users for role: {}, first: {}, max: {}", role, first, max);
			
			ResponseEntity<String> response = keycloakRestTemplate.exchange(finalUrl, HttpMethod.GET, entity, String.class);
			
			if(response.getBody() != null) {
				JsonNode node = objectMapper.readTree(response.getBody());
				List<OfficerDetailDTO> pageUsers = mapUsersToUserDetailDto(node, role);
				
				logger.info("Retrieved {} user for role: {} (page starting at {})", pageUsers.size(), role, first);
				
				if(pageUsers.isEmpty()) {
					hasMore =false;
				} else {
					allUsers.addAll(pageUsers);
					first += max;
					
					if(pageUsers.size() < max) {
						hasMore = false;
					}
				}
			} else {
				hasMore = false;
			}
			
		}
		
		logger.info("Total users fetched for role {}: {}", role, allUsers.size());
		return allUsers;
	}
	
	private void writeOfficerDetailMapToFile() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File("Officer-detail-map.json"),officerDetailMap);
			logger.info("officer detail map written to Officer-detail-map.json");
			
		} catch (Exception e){
			logger.error("Error occured while writing officer map to file");		}
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
			
			if (userDetails == null || userDetails.isEmpty()) {
		        logger.warn("No District Officers found");
		        return;
		    }
		    
		    logger.info("Found {} District Officers", userDetails.size());
			
			userDetails.forEach(u -> {
				Map<String, String> attributes = u.getAttributes();
				if(attributes == null) {
					logger.info("attributes map is null for user: {}", u.getUserId());
					return;
				}
				
				logger.info("User attributes for {}: {}",u.getUserId(), attributes);
				
				String district = attributes.get("district");
				logger.info("District value for user {}: {}",u.getUserId(), district);
				
				if (district != null) {
					districtOfficerMap.computeIfAbsent(district, k -> new ArrayList<>()).add(u);
					districtOfficerAssignment.putIfAbsent(district, u.getUserId());
				}
				else {
	                logger.error("District not available for the user: {}", u.getUserId());
				}
			});
		}
	
	private void populateMapsForInternationalOfficers() {
		List<OfficerDetailDTO> userDetails = officerDetailMap.get(CommonConstants.MVS_INTERNATIONAL_OFFICER);
		
		if (userDetails == null || userDetails.isEmpty()) {
	        logger.warn("No International Officers found");
	        return;
	    }
	    
	    logger.info("Found {} International Officers", userDetails.size());
		
		userDetails.forEach(u -> {
			Map<String, String> attributes = u.getAttributes();
			
			if(attributes == null) {
				logger.info("attributes map is null for user: {}", u.getUserId());
				return;
			}
			
			logger.info("User attributes for {}: {}",u.getUserId(), attributes);
			
			String region = attributes.get("region");
			logger.info("Region value for user {}: {}",u.getUserId(), region);
			
			if (region != null) {
				internationalOfficerMap.computeIfAbsent(region, k -> new ArrayList<>()).add(u);
				internationalOfficerAssignment.putIfAbsent(region, u.getUserId());
			}
			else {
                logger.error("Region not available for the user: {}", u.getUserId());
			}
		});
	}
	
	private void populateMapsForSeniorRegistrationOfficers() {
	    List<OfficerDetailDTO> userDetails = officerDetailMap.get(CommonConstants.MVS_SENIOR_REGISTRATION_OFFICER);
	    
	    if (userDetails == null || userDetails.isEmpty()) {
	        logger.warn("No Senior Registration Officers found");
	        return;
	    }
	    
	    logger.info("Found {} Senior Registration Officers", userDetails.size());
	    
	    userDetails.forEach(u -> {
	        Map<String, String> attributes = u.getAttributes();
	        
	        if(attributes == null) {
				logger.info("attributes map is null for user: {}", u.getUserId());
				return;
			}
			
			logger.info("User attributes for {}: {}",u.getUserId(), attributes);
	        
	        String district = attributes.get("district");
	        logger.info("District value for user {}: {}",u.getUserId(), district);
	        
	        if (district != null) {
	            seniorRegistrationOfficerMap.computeIfAbsent(district, k -> new ArrayList<>()).add(u);
	            seniorRegistrationOfficerAssignment.putIfAbsent(district, u.getUserId());
	            logger.info("Added Senior Registration Officer {} for district {}", u.getUserId(), district);
	        } else {
	            logger.error("District not available for the Senior Registration Officer: {}", u.getUserId());
	        }
	    });
	}
	
	public DemographicDetailsDTO getDemographicDetails(String nin) {
		logger.info("Fetching demographic data from id repo: {}");

		String handle = nin.toLowerCase() + "@nin";
		String url = idRepoUrl + handle;

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).queryParam("type", "all")
				.queryParam("idType", "handle");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(null, headers);

		try {
			ResponseEntity<ResponseWrapper<DemographicDetailsDTO>> responseEntity = restTemplate.exchange(
					builder.build().toUri(), HttpMethod.GET, entity,
					new ParameterizedTypeReference<ResponseWrapper<DemographicDetailsDTO>>() {
					});
			if (responseEntity.getBody() == null) {
				logger.error("Failed to get details from idrepo. Status code: " + responseEntity.getStatusCodeValue());
				throw new RequestException(ErrorCode.IDREPO_FETCH_FAILED.getErrorCode(),
						ErrorCode.IDREPO_FETCH_FAILED.getErrorMessage() + "with status code: "
								+ responseEntity.getStatusCodeValue());
			}

			ResponseWrapper<DemographicDetailsDTO> responseWrapper = responseEntity.getBody();

			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				logger.error("IdRepo fetch failed: {}", responseWrapper.getErrors().get(0));
				throw new RequestException(ErrorCode.INVALID_IDREPO_RESPONSE.getErrorCode(),
						ErrorCode.INVALID_IDREPO_RESPONSE.getErrorMessage() + "with error: "
								+ responseWrapper.getErrors().get(0));
			}

			List<Document> docs = responseWrapper.getResponse().getDocuments();
			System.out.println("Doc List : "+docs);
			if (docs != null && responseWrapper.getResponse().getIdentity() != null) {
				Map<String, ProofDocument> proofDocMap = new HashMap<>();
				// get all ProofDocument fields
				Field[] fields = DemographicDetailsDTO.Identity.class.getDeclaredFields();
				for (Field field : fields) {
					// Check if the field is a ProofDocument
					if (field.getType() == DemographicDetailsDTO.ProofDocument.class) {
						try {
							field.setAccessible(true);
							ProofDocument proofDoc = (ProofDocument) field
									.get(responseWrapper.getResponse().getIdentity());

							// If the ProofDocument exists, add it to the map using its field name
							if (proofDoc != null) {
								String fieldName = field.getName();
								proofDocMap.put(fieldName, proofDoc);
								System.out.println("Added to map: " + fieldName + " -> " + proofDoc);
							}
						} catch (IllegalAccessException e) {
							logger.warn("Could not access field: " + field.getName(), e);
						}
					}
				}

				docs.forEach(doc -> {
					if (!doc.getCategory().equals("individualBiometrics")) {
						System.out.println("Doc  : " + doc);
						doc.setValue(CryptoUtil.decodeURLSafeBase64(doc.getValue().toString()));
						String category = doc.getCategory();
						ProofDocument proofDoc = proofDocMap.get(category);

						if (proofDoc != null && proofDoc.getFormat() != null) {
							doc.setFormat(proofDoc.getFormat());
							System.out.println("Set format for " + category + " to " + proofDoc.getFormat());
						} else {
							System.out.println("No matching proof document found for category: " + category);
						}

					}
				});

			}

			return responseWrapper.getResponse();
		} catch (RestClientException e) {
			logger.error("Failed to get details from idrepo, {}", e);
			throw new RequestException(ErrorCode.IDREPO_FETCH_FAILED.getErrorCode(),
					ErrorCode.IDREPO_FETCH_FAILED.getErrorMessage() + "with error: " + e.getMessage());
		}
	}
	
	
	private byte[] extractFaceImageData(byte[] decodedBioValue) {

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
	
	private byte[] convertJP2ToPNG(byte[] jp2Bytes) throws IOException {
		try (ByteArrayInputStream jp2InputStream = new ByteArrayInputStream(jp2Bytes)) {
			BufferedImage image = ImageIO.read(jp2InputStream);
			if (image == null) {
				throw new IOException("Failed to decode JP2 image");
			}

			try (ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream()) {
				boolean writeSuccess = ImageIO.write(image, "png", pngOutputStream);
				if (!writeSuccess) {
					throw new IOException("Failed to encode image to PNG");
				}
				return pngOutputStream.toByteArray();
			}
		}
	}
	
	@Override
	public DocumentResponseDTO fetchDocument(DocumentRequestDTO documentRequest) {
	    try {
	        List<DocumentResponseDTO.DocumentInfo> documentsList = new ArrayList<>();
	        
	        // Process each document name in the list
	        for (String documentName : documentRequest.getDocumentNames()) {
	            in.tf.nira.manual.verification.dto.Document document = getDocument(
	                documentRequest.getId(),
	                documentName,
	                documentRequest.getSource(),
	                documentRequest.getProcess()
	            );
	            
	            DocumentResponseDTO.DocumentInfo documentInfo = new DocumentResponseDTO.DocumentInfo();
	            documentInfo.setDocumentName(documentName);
	            documentInfo.setDocument(document.getDocument());
	            documentInfo.setValue(document.getValue());
	            documentInfo.setType(document.getType());
	            documentInfo.setFormat(document.getFormat());
	            
	            documentsList.add(documentInfo);
	        }
	        
	        DocumentResponseDTO response = new DocumentResponseDTO();
	        response.setDocuments(documentsList);
	        
	        return response;
	    } catch (Exception e) {
	        throw new RequestException(ErrorCode.DOCUMENT_FETCH_ERROR.getErrorCode(),
	                String.format("Error fetching document: %s", e.getMessage()));
	    }
	}

	protected in.tf.nira.manual.verification.dto.Document getDocument(String id, String documentName, String source, String process) 
	        throws JsonProcessingException, IOException, io.mosip.kernel.core.util.exception.JsonProcessingException {
	    PacketDocumentRequestDto fieldDto = new PacketDocumentRequestDto(id, documentName, source, process);

	    RequestWrapper<PacketDocumentRequestDto> request = new RequestWrapper<>();
	    request.setId(CommonConstants.PACKET_MANAGER_REQUEST_VERSION); 
	    request.setVersion(CommonConstants.PACKET_MANAGER_REQUEST_VERSION);
	    request.setRequesttime(DateUtils.getUTCCurrentDateTime());
	    request.setRequest(fieldDto);

	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    HttpEntity<RequestWrapper<PacketDocumentRequestDto>> httpEntity = new HttpEntity<>(request, headers);

	    ResponseEntity<ResponseWrapper> responseEntity = restTemplate.exchange(
	    	packetManagerDocumentFetchUrl,
	        HttpMethod.POST,
	        httpEntity,
	        ResponseWrapper.class
	    );

	    ResponseWrapper<in.tf.nira.manual.verification.dto.Document> response = responseEntity.getBody();

	    if (response.getErrors() != null && response.getErrors().size() > 0) {
	        ServiceError errorDTO = response.getErrors().iterator().next();
	        logger.error("Service error occurred: {}", errorDTO);
	        for (ServiceError error : response.getErrors()) {
	            logger.error("Error details: {}", error);
	        }
	    }

	    in.tf.nira.manual.verification.dto.Document document = objectMapper.readValue(JsonUtils.javaObjectToJsonString(response.getResponse()), in.tf.nira.manual.verification.dto.Document.class);

	    return document;
	}

	@Scheduled(cron = "${manual.verification.officer.reassignment.cron.expression:0 0 0 * * ?}")
	public void executeScheduledJobs() {
		logger.info("Started scheduled jobs");
		
		//job1 the officer reassignment job
		try {
			officerReassignment();
		} catch (Exception e) {
			logger.error("Error in officer reassignment job: {}", e.getMessage(), e);
		}
		
		//job2 expired interview date job
		try {
			dropExpiredInterviewApplications();
		} catch (Exception e) {
			logger.error("Error in expired interview applicaions job: {}", e.getMessage(), e);
		}
		
		logger.info("Completed all scheduled jobs");
	}
	
	private void officerReassignment() {
		logger.info("Checking applications for re-assignment");

		LocalDateTime dateThreshold = LocalDateTime.now().minusDays(reassignmentDays);
		List<OfficerDetailDTO> officers = officerDetailMap.get(CommonConstants.MVS_OFFICER_ROLE);
		List<MVSApplication> applications = mVSApplicationRepo.findRecordsOlderThanXDays(dateThreshold);

		Map<OfficerDetailDTO, Integer> prevOfficerInfo = new HashMap<>();
		Set<OfficerDetailDTO> newOfficerInfo = new HashSet<>();

		applications.forEach(application -> {
			logger.info("Re-assigning application {}", application.getRegId());

			MVSApplicationHistory appHistory = getAppHistoryEntity(application);
			mVSApplicationHistoryRepo.save(appHistory);

			OfficerDetailDTO prevOfficer = officers.stream()
											.filter(officer -> officer.getUserId().equals(application.getAssignedOfficerId()))
											.findFirst()
											.orElse(null);
			prevOfficerInfo.merge(prevOfficer, 1, Integer::sum);

			OfficerAssignment officerAssignment = officerAssignmentRepo.findByUserRole(CommonConstants.MVS_OFFICER_ROLE);
			OfficerDetailDTO selectedOfficer = fetchOfficerForAssignment(CommonConstants.MVS_OFFICER_ROLE, officerAssignment, null, null);

			if (selectedOfficer.getUserId().equals(application.getAssignedOfficerId())) {
				int currentIndex = officers.indexOf(selectedOfficer);
				selectedOfficer = officers.get((currentIndex + 1) % officers.size());
				officerAssignment.setUserId(selectedOfficer.getUserId());
			}

			application.setAssignedOfficerId(selectedOfficer.getUserId());
			application.setAssignedOfficerName(selectedOfficer.getUserName());
			application.setAssignedOfficerRole(selectedOfficer.getUserRole());
			application.setUpdatedBy(SYSTEM);
			application.setUpdatedTimes(LocalDateTime.now());

			if(officerAssignment.getCrDTimes() == null) {
				officerAssignment.setCreatedBy(SYSTEM);
				officerAssignment.setCrDTimes(LocalDateTime.now());
			}
			else {
				officerAssignment.setUpdatedBy(SYSTEM);
				officerAssignment.setUpdatedTimes(LocalDateTime.now());
			}
			officerAssignmentRepo.save(officerAssignment);
			mVSApplicationRepo.save(application);

			newOfficerInfo.add(selectedOfficer);

			logger.info("Application {} re-assigned to {}", application.getRegId(), application.getAssignedOfficerId());
		});

		prevOfficerInfo.forEach(this::sendNotificationToPrevAssignedOfficer);
		newOfficerInfo.forEach(this::sendNotificationToNewAssignedOfficer);
	}

	private void sendNotificationToPrevAssignedOfficer(OfficerDetailDTO officer, Integer count) {
		String email = officer.getEmail();

		Map<String, Object> attributes = new HashMap<>();
		attributes.put("PREV_OFFICER_NAME", officer.getUserName());
		attributes.put("APPLICATIONS_COUNT", count);
		attributes.put("TIME_PERIOD", reassignmentDays);

		if (email != null) {
			sendEmail(email, "Application Reassignment Notification", attributes, prevOfficerEmailTemplateTypeCode);
		} else {
			logger.warn("Email Id not available for the previous officer of reassignment");
		}
	}

	private void sendNotificationToNewAssignedOfficer(OfficerDetailDTO officer) {
		String email = officer.getEmail();

		Map<String, Object> attributes = new HashMap<>();
		attributes.put("NEW_OFFICER_NAME", officer.getUserName());

		if (email != null) {
			sendEmail(email, "Urgent: Application Reassigned to You", attributes, newOfficerEmailTemplateTypeCode);
		} else {
			logger.warn("Email Id not available for the new officer of reassignment");
		}
	}
	
	public void dropExpiredInterviewApplications() {
		logger.info("Checking for expired interview applications");
		
		LocalDateTime expiryThreshold = LocalDateTime.now().minusDays(interviewValidDays);
		
		List<MVSApplication> expiredApplications = mVSApplicationRepo.findInterviewExpiredApplications(StageCode.INTERVIEW_SCHEDULED.getStage(), expiryThreshold);
		
		logger.info("Found {} applications with expired interview dates", expiredApplications.size());
		
		for(MVSApplication application : expiredApplications) {
			logger.info("Processing expired interview application: {}", application.getRegId());
			
			MVSApplicationHistory applicationHistory = getAppHistoryEntity(application);
			mVSApplicationHistoryRepo.save(applicationHistory);
			
			rejectApplication(application, "Application rejected due to expired interview date", "EXPIRED_INTERVIEW");
			
			logger.info("Application {} rejected due to expired interview date", application.getRegId());
			
		}
		
		logger.info("Completed processing applications with expired interview dates");;
	}
}
